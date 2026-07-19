package com.nntan041299.englishmasterservice.listening.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeAiResponse;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import com.nntan041299.englishmasterservice.listening.entity.ListeningVoiceGenerationStats;
import com.nntan041299.englishmasterservice.listening.repository.ListeningChallengeRepository;
import com.nntan041299.englishmasterservice.listening.repository.ListeningVoiceGenerationStatsRepository;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates listening challenges (sentence + synthesized voice) in the background so they're ready
 * to serve on demand, instead of calling the AI synchronously when a user opens the Listening page.
 *
 * <p>Voice generation is the scarce resource here (limited by the free tier), so it is hard-capped at
 * {@link #maxVoiceRequests} lifetime calls, tracked in {@link ListeningVoiceGenerationStats}. Once the
 * cap is reached, this simply stops generating new challenges. The cap defaults to 0 (generation
 * effectively disabled) until explicitly raised via configuration.
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

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final ListeningChallengeRepository challengeRepository;
    private final ListeningVoiceGenerationStatsRepository statsRepository;

    @Value("${listening.practice.generation.max-voice-requests}")
    private int maxVoiceRequests;

    @Transactional
    public void generate() {
        ListeningVoiceGenerationStats stats = statsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("listening_voice_generation_stats row is missing"));

        if (stats.getRequestCount() >= maxVoiceRequests) {
            log.info("listening_practice_generation skipped reason=voice_request_limit_reached count={}",
                    stats.getRequestCount());
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
        } catch (Exception ex) {
            log.error("listening_practice_generation sentence_ai_error error={}", ex.getMessage(), ex);
            return;
        }

        byte[] voiceData;
        try {
            voiceData = aiService.synthesizeSpeechWav(aiResponse.sentence());
        } catch (Exception ex) {
            log.error("listening_practice_generation voice_ai_error error={}", ex.getMessage(), ex);
            return;
        }

        stats.setRequestCount(stats.getRequestCount() + 1);
        statsRepository.save(stats);

        ListeningChallenge challenge = challengeRepository.save(ListeningChallenge.builder()
                .level(level)
                .sentence(aiResponse.sentence())
                .voiceData(voiceData)
                .build());

        log.info("listening_practice_generated level={} challenge={} voice_requests_used={}",
                level, challenge.getId(), stats.getRequestCount());
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
