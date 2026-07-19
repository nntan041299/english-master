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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingService {

    private static final String ISSUE_TYPES = Arrays.stream(WritingIssueType.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    /**
     * Target word count band per CEFR level. Defined here (not client input, not persisted) so it can
     * be recomputed from a challenge's stored {@link LanguageLevel} at any time, including when the
     * user later submits their writing.
     */
    private static final Map<LanguageLevel, WordRange> WORD_RANGES_BY_LEVEL = new EnumMap<>(Map.of(
            LanguageLevel.A1, new WordRange(20, 40),
            LanguageLevel.A2, new WordRange(40, 60),
            LanguageLevel.B1, new WordRange(60, 90),
            LanguageLevel.B2, new WordRange(90, 130),
            LanguageLevel.C1, new WordRange(130, 180),
            LanguageLevel.C2, new WordRange(180, 250)));

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final CurrentUserProvider currentUserProvider;
    private final WritingChallengeRepository challengeRepository;
    private final WritingSubmissionRepository submissionRepository;

    /**
     * Returns a writing challenge for the current user to answer. Reuses an existing challenge at the
     * user's current language level that they haven't submitted an answer for yet, if one exists,
     * instead of spending an AI call generating one that would just replace it unused. Only generates a
     * fresh one via the AI when no such unanswered challenge exists (including when the user's level
     * has changed since their last unanswered challenge was generated).
     */
    @Transactional
    public WritingChallengeResponse generateChallenge() {
        User user = currentUserProvider.getCurrentUser();
        LanguageLevel level = user.getLanguageLevel();

        List<WritingChallenge> unsubmitted = challengeRepository.findUnsubmittedByUserIdAndLevel(
                user.getId(), level, PageRequest.of(0, 1));
        if (!unsubmitted.isEmpty()) {
            WritingChallenge existing = unsubmitted.getFirst();
            log.info("writing_challenge_reused user={} level={} challenge={}", user.getId(), level, existing.getId());
            return toResponse(existing);
        }

        WordRange range = wordRangeFor(level);

        String prompt = aiPromptManager.get(AiPromptKey.WRITING_CHALLENGE_GENERATION)
                .formatted(level.name(), range.min(), range.max());
        WritingChallengeAiResponse aiResponse = aiService.generateContent(prompt, WritingChallengeAiResponse.class);

        WritingChallenge challenge = challengeRepository.save(WritingChallenge.builder()
                .user(user)
                .level(level)
                .title(aiResponse.title())
                .prompt(aiResponse.prompt())
                .build());

        log.info("writing_challenge_generated user={} level={} min_words={} max_words={} challenge={}",
                user.getId(), level, range.min(), range.max(), challenge.getId());
        return toResponse(challenge);
    }

    /** Sends the user's writing to the AI for feedback, persists the submission and returns the feedback. */
    @Transactional
    public WritingFeedbackResponse submit(SubmitWritingRequest request) {
        User user = currentUserProvider.getCurrentUser();

        WritingChallenge challenge = challengeRepository.findByIdAndUserId(request.challengeId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Writing challenge not found: " + request.challengeId()));

        WordRange range = wordRangeFor(challenge.getLevel());
        int wordCount = countWords(request.text());
        if (wordCount < range.min() || wordCount > range.max()) {
            throw new IllegalArgumentException(
                    "Your response has %d words; this challenge requires between %d and %d words."
                            .formatted(wordCount, range.min(), range.max()));
        }

        String prompt = aiPromptManager.get(AiPromptKey.WRITING_FEEDBACK)
                .formatted(
                        challenge.getLevel().name(),
                        range.min(),
                        range.max(),
                        challenge.getPrompt(),
                        request.text(),
                        ISSUE_TYPES,
                        wordCount);
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

    private WritingChallengeResponse toResponse(WritingChallenge challenge) {
        WordRange range = wordRangeFor(challenge.getLevel());
        return new WritingChallengeResponse(
                challenge.getId(), challenge.getLevel(), range.min(), range.max(), challenge.getTitle(), challenge.getPrompt());
    }

    private WordRange wordRangeFor(LanguageLevel level) {
        return WORD_RANGES_BY_LEVEL.get(level);
    }

    /** Counts words the same way the UI does: whitespace-separated tokens, ignoring empty ones. */
    private int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(text.trim().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .count();
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

    /** Target word count band for a CEFR level. */
    private record WordRange(int min, int max) {}
}
