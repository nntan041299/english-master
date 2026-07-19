package com.nntan041299.englishmasterservice.writing.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.writing.dto.SubmitWritingRequest;
import com.nntan041299.englishmasterservice.writing.dto.WritingChallengeAiResponse;
import com.nntan041299.englishmasterservice.writing.dto.WritingChallengeResponse;
import com.nntan041299.englishmasterservice.writing.dto.WritingFeedbackAiResponse;
import com.nntan041299.englishmasterservice.writing.dto.WritingFeedbackResponse;
import com.nntan041299.englishmasterservice.writing.dto.WritingIssueResponse;
import com.nntan041299.englishmasterservice.writing.entity.WritingChallenge;
import com.nntan041299.englishmasterservice.writing.entity.WritingIssue;
import com.nntan041299.englishmasterservice.writing.entity.WritingIssueType;
import com.nntan041299.englishmasterservice.writing.entity.WritingSubmission;
import com.nntan041299.englishmasterservice.writing.repository.WritingChallengeRepository;
import com.nntan041299.englishmasterservice.writing.repository.WritingSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingService {

    private static final String ISSUE_TYPES = Arrays.stream(WritingIssueType.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final CurrentUserProvider currentUserProvider;
    private final WritingChallengeRepository challengeRepository;
    private final WritingSubmissionRepository submissionRepository;

    /** Generates a fresh AI writing challenge for the current user at their own language level and stores it. */
    @Transactional
    public WritingChallengeResponse generateChallenge() {
        User user = currentUserProvider.getCurrentUser();
        LanguageLevel level = user.getLanguageLevel();

        String prompt = aiPromptManager.get(AiPromptKey.WRITING_CHALLENGE_GENERATION).formatted(level.name());
        WritingChallengeAiResponse aiResponse = aiService.generateContent(prompt, WritingChallengeAiResponse.class);

        WritingChallenge challenge = challengeRepository.save(WritingChallenge.builder()
                .user(user)
                .level(level)
                .title(aiResponse.title())
                .prompt(aiResponse.prompt())
                .build());

        log.info("writing_challenge_generated user={} level={} challenge={}", user.getId(), level, challenge.getId());
        return new WritingChallengeResponse(
                challenge.getId(), challenge.getLevel(), challenge.getTitle(), challenge.getPrompt());
    }

    /** Sends the user's writing to the AI for feedback, persists the submission and returns the feedback. */
    @Transactional
    public WritingFeedbackResponse submit(SubmitWritingRequest request) {
        User user = currentUserProvider.getCurrentUser();

        WritingChallenge challenge = challengeRepository.findByIdAndUserId(request.challengeId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Writing challenge not found: " + request.challengeId()));

        String prompt = aiPromptManager.get(AiPromptKey.WRITING_FEEDBACK)
                .formatted(challenge.getLevel().name(), challenge.getPrompt(), request.text(), ISSUE_TYPES);
        WritingFeedbackAiResponse aiResponse = aiService.generateContent(prompt, WritingFeedbackAiResponse.class);

        List<WritingIssue> issues = toIssues(aiResponse);

        WritingSubmission submission = submissionRepository.save(WritingSubmission.builder()
                .challenge(challenge)
                .user(user)
                .text(request.text())
                .overallFeedback(aiResponse.overallFeedback())
                .score(aiResponse.score())
                .issues(issues)
                .build());

        log.info("writing_submission_scored user={} challenge={} submission={} score={} issues={}",
                user.getId(), challenge.getId(), submission.getId(), submission.getScore(), issues.size());

        return new WritingFeedbackResponse(
                submission.getId(),
                submission.getOverallFeedback(),
                submission.getScore(),
                issues.stream().map(WritingIssueResponse::from).toList());
    }

    private List<WritingIssue> toIssues(WritingFeedbackAiResponse ai) {
        if (ai.issues() == null) {
            return List.of();
        }
        return ai.issues().stream()
                .filter(issue -> issue.original() != null && !issue.original().isBlank())
                .map(issue -> new WritingIssue(
                        issue.original(),
                        issue.suggestion(),
                        issue.explanation(),
                        issue.type() == null ? WritingIssueType.OTHER : issue.type()))
                .toList();
    }
}
