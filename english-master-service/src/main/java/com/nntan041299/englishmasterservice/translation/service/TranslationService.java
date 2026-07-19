package com.nntan041299.englishmasterservice.translation.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.translation.dto.SubmitTranslationRequest;
import com.nntan041299.englishmasterservice.translation.dto.TranslationChallengeAiResponse;
import com.nntan041299.englishmasterservice.translation.dto.TranslationChallengeResponse;
import com.nntan041299.englishmasterservice.translation.dto.TranslationFeedbackAiResponse;
import com.nntan041299.englishmasterservice.translation.dto.TranslationFeedbackResponse;
import com.nntan041299.englishmasterservice.translation.entity.TranslationChallenge;
import com.nntan041299.englishmasterservice.translation.entity.TranslationDirection;
import com.nntan041299.englishmasterservice.translation.entity.TranslationSubmission;
import com.nntan041299.englishmasterservice.translation.repository.TranslationChallengeRepository;
import com.nntan041299.englishmasterservice.translation.repository.TranslationSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    /** Target sentence length (in words) to generate, same for every CEFR level. */
    private static final int SENTENCE_MIN_WORDS = 8;
    private static final int SENTENCE_MAX_WORDS = 22;

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final CurrentUserProvider currentUserProvider;
    private final TranslationChallengeRepository challengeRepository;
    private final TranslationSubmissionRepository submissionRepository;

    /**
     * Returns a translation challenge for the current user at their own language level, in the
     * requested direction. Reuses an existing challenge in that direction/level the user hasn't
     * answered yet, if one exists, instead of spending an AI call generating one that would just
     * replace it unused.
     */
    @Transactional
    public TranslationChallengeResponse generateChallenge(TranslationDirection direction) {
        User user = currentUserProvider.getCurrentUser();
        LanguageLevel level = user.getLanguageLevel();

        List<TranslationChallenge> unsubmitted = challengeRepository.findUnsubmittedByUserIdAndDirectionAndLevel(
                user.getId(), direction, level, PageRequest.of(0, 1));
        if (!unsubmitted.isEmpty()) {
            TranslationChallenge existing = unsubmitted.getFirst();
            log.info("translation_challenge_reused user={} direction={} level={} challenge={}",
                    user.getId(), direction, level, existing.getId());
            return toResponse(existing);
        }

        String sourceLanguage = sourceLanguageOf(direction);

        String prompt = aiPromptManager.get(AiPromptKey.TRANSLATION_CHALLENGE_GENERATION)
                .formatted(sourceLanguage, level.name(), SENTENCE_MIN_WORDS, SENTENCE_MAX_WORDS);
        TranslationChallengeAiResponse aiResponse =
                aiService.generateContent(prompt, TranslationChallengeAiResponse.class);

        TranslationChallenge challenge = challengeRepository.save(TranslationChallenge.builder()
                .user(user)
                .direction(direction)
                .level(level)
                .sourceText(aiResponse.sourceText())
                .build());

        log.info("translation_challenge_generated user={} direction={} level={} challenge={}",
                user.getId(), direction, level, challenge.getId());
        return toResponse(challenge);
    }

    /** Sends the user's translation to the AI for evaluation and persists the submission. */
    @Transactional
    public TranslationFeedbackResponse submit(SubmitTranslationRequest request) {
        User user = currentUserProvider.getCurrentUser();

        TranslationChallenge challenge = challengeRepository.findByIdAndUserId(request.challengeId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Translation challenge not found: " + request.challengeId()));

        String sourceLanguage = sourceLanguageOf(challenge.getDirection());
        String targetLanguage = targetLanguageOf(challenge.getDirection());

        String prompt = aiPromptManager.get(AiPromptKey.TRANSLATION_FEEDBACK)
                .formatted(
                        challenge.getLevel().name(),
                        sourceLanguage,
                        targetLanguage,
                        challenge.getSourceText(),
                        request.translation());
        TranslationFeedbackAiResponse aiResponse =
                aiService.generateContent(prompt, TranslationFeedbackAiResponse.class);

        boolean correct = Boolean.TRUE.equals(aiResponse.correct());

        TranslationSubmission submission = submissionRepository.save(TranslationSubmission.builder()
                .challenge(challenge)
                .user(user)
                .userTranslation(request.translation())
                .correct(correct)
                .feedback(aiResponse.feedback())
                .suggestedTranslation(aiResponse.suggestedTranslation())
                .build());

        log.info("translation_submission_scored user={} challenge={} submission={} correct={}",
                user.getId(), challenge.getId(), submission.getId(), correct);

        return new TranslationFeedbackResponse(
                submission.getId(), correct, submission.getFeedback(), submission.getSuggestedTranslation());
    }

    private String sourceLanguageOf(TranslationDirection direction) {
        return direction == TranslationDirection.EN_TO_VI ? "English" : "Vietnamese";
    }

    private String targetLanguageOf(TranslationDirection direction) {
        return direction == TranslationDirection.EN_TO_VI ? "Vietnamese" : "English";
    }

    private TranslationChallengeResponse toResponse(TranslationChallenge challenge) {
        return new TranslationChallengeResponse(
                challenge.getId(), challenge.getDirection(), challenge.getLevel(), challenge.getSourceText());
    }
}
