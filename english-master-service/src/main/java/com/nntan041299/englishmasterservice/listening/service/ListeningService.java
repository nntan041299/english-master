package com.nntan041299.englishmasterservice.listening.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningFeedbackAiResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningFeedbackResponse;
import com.nntan041299.englishmasterservice.listening.dto.SubmitListeningRequest;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import com.nntan041299.englishmasterservice.listening.entity.ListeningSubmission;
import com.nntan041299.englishmasterservice.listening.repository.ListeningChallengeRepository;
import com.nntan041299.englishmasterservice.listening.repository.ListeningSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningService {

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final CurrentUserProvider currentUserProvider;
    private final ListeningChallengeRepository challengeRepository;
    private final ListeningSubmissionRepository submissionRepository;

    /**
     * Returns a listening challenge for the current user at their own language level, picked from the
     * pool generated ahead of time by {@link ListeningPracticeGenerationService}, skipping ones this
     * user already answered. Returns empty if none are available yet.
     */
    @Transactional(readOnly = true)
    public Optional<ListeningChallengeResponse> getChallenge() {
        User user = currentUserProvider.getCurrentUser();
        LanguageLevel level = user.getLanguageLevel();

        List<ListeningChallenge> available =
                challengeRepository.findAvailableForUser(level, user.getId(), PageRequest.of(0, 1));
        if (available.isEmpty()) {
            log.debug("listening_challenge_unavailable user={} level={}", user.getId(), level);
            return Optional.empty();
        }

        return Optional.of(toResponse(available.getFirst()));
    }

    /** Returns the raw stored audio for a challenge (pre-synthesized by the generation job). */
    @Transactional(readOnly = true)
    public byte[] getAudio(Long challengeId) {
        ListeningChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Listening challenge not found: " + challengeId));
        return challenge.getVoiceData();
    }

    /** Sends the user's transcript to the AI for evaluation and persists the submission. */
    @Transactional
    public ListeningFeedbackResponse submit(SubmitListeningRequest request) {
        User user = currentUserProvider.getCurrentUser();

        ListeningChallenge challenge = challengeRepository.findById(request.challengeId())
                .orElseThrow(() -> new EntityNotFoundException("Listening challenge not found: " + request.challengeId()));

        String prompt = aiPromptManager.get(AiPromptKey.LISTENING_FEEDBACK)
                .formatted(challenge.getLevel().name(), challenge.getSentence(), request.transcript());
        ListeningFeedbackAiResponse aiResponse =
                aiService.generateContent(prompt, ListeningFeedbackAiResponse.class);

        boolean correct = Boolean.TRUE.equals(aiResponse.correct());

        ListeningSubmission submission = submissionRepository.save(ListeningSubmission.builder()
                .challenge(challenge)
                .user(user)
                .transcript(request.transcript())
                .correct(correct)
                .feedback(aiResponse.feedback())
                .build());

        log.info("listening_submission_scored user={} challenge={} submission={} correct={}",
                user.getId(), challenge.getId(), submission.getId(), correct);

        return new ListeningFeedbackResponse(
                submission.getId(), correct, submission.getFeedback(), challenge.getSentence());
    }

    private ListeningChallengeResponse toResponse(ListeningChallenge challenge) {
        return new ListeningChallengeResponse(challenge.getId(), challenge.getLevel(), challenge.getSentence());
    }
}
