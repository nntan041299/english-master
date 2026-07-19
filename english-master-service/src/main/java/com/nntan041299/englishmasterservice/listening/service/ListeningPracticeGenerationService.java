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
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates listening challenges (sentence + synthesized voice) in the background so they're ready
 * to serve on demand, instead of calling the AI synchronously when a user opens the Listening page.
 *
 * <p>Voice generation is the scarce resource here (limited by the free tier), so it is hard-capped at
 * {@link #MAX_VOICE_REQUESTS} lifetime calls, tracked in {@link ListeningVoiceGenerationStats}. Once
 * the cap is reached, this simply stops generating new challenges.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningPracticeGenerationService {

    static final int MAX_VOICE_REQUESTS = 2;

    /** Target sentence length (in words) to generate per CEFR level, so difficulty scales with level. */
    private static final Map<LanguageLevel, SentenceLength> SENTENCE_LENGTH_BY_LEVEL = new EnumMap<>(Map.of(
            LanguageLevel.A1, new SentenceLength(4, 6),
            LanguageLevel.A2, new SentenceLength(6, 9),
            LanguageLevel.B1, new SentenceLength(8, 12),
            LanguageLevel.B2, new SentenceLength(10, 15),
            LanguageLevel.C1, new SentenceLength(12, 18),
            LanguageLevel.C2, new SentenceLength(15, 22)));

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final ListeningChallengeRepository challengeRepository;
    private final ListeningVoiceGenerationStatsRepository statsRepository;

    @Transactional
    public void generate() {
        ListeningVoiceGenerationStats stats = statsRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("listening_voice_generation_stats row is missing"));

        if (stats.getRequestCount() >= MAX_VOICE_REQUESTS) {
            log.info("listening_practice_generation skipped reason=voice_request_limit_reached count={}",
                    stats.getRequestCount());
            return;
        }

        LanguageLevel level = pickLevel();
        SentenceLength length = SENTENCE_LENGTH_BY_LEVEL.get(level);

        String prompt = aiPromptManager.get(AiPromptKey.LISTENING_CHALLENGE_GENERATION)
                .formatted(level.name(), length.min(), length.max());

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

    /** Rotates through CEFR levels so the pool gets some coverage across levels, not just one. */
    private LanguageLevel pickLevel() {
        LanguageLevel[] levels = LanguageLevel.values();
        long total = challengeRepository.count();
        return levels[(int) (total % levels.length)];
    }

    /** Target sentence length band (in words) for a CEFR level. */
    private record SentenceLength(int min, int max) {}
}
