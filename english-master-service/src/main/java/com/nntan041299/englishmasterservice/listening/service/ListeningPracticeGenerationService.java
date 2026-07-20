package com.nntan041299.englishmasterservice.listening.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiModelsExhaustedException;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeAiResponse;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import com.nntan041299.englishmasterservice.listening.repository.ListeningChallengeRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates listening challenges (sentence + synthesized voice) in the background so they're ready
 * to serve on demand, instead of calling the AI synchronously when a user opens the Listening page.
 *
 * <p>Voice generation is the scarce resource here (limited by the free tier), so each configured
 * Gemini TTS model is capped individually (see {@link com.nntan041299.englishmasterservice.ai.gemini.GeminiService}).
 * Once every configured model is over quota, {@code synthesizeSpeechWav} throws and this job simply
 * stops generating new challenges until quotas reset.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningPracticeGenerationService {

    /** Target sentence length (in words) to generate, same for every CEFR level. */
    private static final int SENTENCE_MIN_WORDS = 8;
    private static final int SENTENCE_MAX_WORDS = 22;

    /** Each level keeps at most this many challenges with no submission yet, from any user. */
    private static final int MAX_UNSUBMITTED_PER_LEVEL = 5;

    /** How long to back off after a voice-generation failure before the job runs again. */
    private static final Duration VOICE_AI_ERROR_BACKOFF = Duration.ofMinutes(1);

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final ListeningChallengeRepository challengeRepository;

    /** Set after a voice-generation failure; the job skips runs until this instant passes. */
    private volatile Instant retryAfter = Instant.MIN;

    @Transactional
    public void generate() {
        if (Instant.now().isBefore(retryAfter)) {
            log.debug("listening_practice_generation skipped reason=backing_off_after_voice_ai_error retry_after={}",
                    retryAfter);
            return;
        }

        Optional<LanguageLevel> levelNeedingMore = pickLevelNeedingMore();
        if (levelNeedingMore.isEmpty()) {
            log.info("listening_practice_generation skipped reason=all_levels_have_enough_unsubmitted max={}",
                    MAX_UNSUBMITTED_PER_LEVEL);
            return;
        }
        LanguageLevel level = levelNeedingMore.get();

        String prompt = aiPromptManager.get(AiPromptKey.LISTENING_CHALLENGE_GENERATION)
                .formatted(level.name(), SENTENCE_MIN_WORDS, SENTENCE_MAX_WORDS);

        ListeningChallengeAiResponse aiResponse;
        try {
            aiResponse = aiService.generateContent(prompt, ListeningChallengeAiResponse.class);
        } catch (AiModelsExhaustedException ex) {
            retryAfter = Instant.now().plus(VOICE_AI_ERROR_BACKOFF);
            log.warn("listening_practice_generation sentence_ai_models_exhausted error={} retry_after={}",
                    ex.getMessage(), retryAfter);
            return;
        } catch (Exception ex) {
            log.error("listening_practice_generation sentence_ai_error error={}", ex.getMessage(), ex);
            return;
        }

        byte[] voiceData;
        try {
            voiceData = aiService.synthesizeSpeechWav(aiResponse.sentence());
        } catch (AiModelsExhaustedException ex) {
            retryAfter = Instant.now().plus(VOICE_AI_ERROR_BACKOFF);
            log.warn("listening_practice_generation voice_ai_models_exhausted error={} retry_after={}",
                    ex.getMessage(), retryAfter);
            return;
        } catch (Exception ex) {
            retryAfter = Instant.now().plus(VOICE_AI_ERROR_BACKOFF);
            log.error("listening_practice_generation voice_ai_error error={} retry_after={}",
                    ex.getMessage(), retryAfter, ex);
            return;
        }

        ListeningChallenge challenge = challengeRepository.save(ListeningChallenge.builder()
                .level(level)
                .sentence(aiResponse.sentence())
                .voiceData(voiceData)
                .build());

        log.info("listening_practice_generated level={} challenge={}", level, challenge.getId());
    }

    /**
     * Finds the first CEFR level whose pool of never-submitted challenges is below
     * {@link #MAX_UNSUBMITTED_PER_LEVEL}, i.e. still needs more generated. Empty if every level
     * already has enough.
     */
    private Optional<LanguageLevel> pickLevelNeedingMore() {
        return Arrays.stream(LanguageLevel.values())
                .filter(level -> challengeRepository.countUnsubmittedByLevel(level) < MAX_UNSUBMITTED_PER_LEVEL)
                .findFirst();
    }
}
