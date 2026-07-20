package com.nntan041299.englishmasterservice.ai.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.WavEncoder;
import com.nntan041299.englishmasterservice.ai.entity.AiLimitStats;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiContent;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiGenerationConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiInlineData;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiPart;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiPrebuiltVoiceConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiRequest;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiResponse;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiSpeechConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiVoiceConfig;
import com.nntan041299.englishmasterservice.ai.repository.AiLimitStatsRepository;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link AIService} implementation backed by the Gemini Generative Language API
 * (generativelanguage.googleapis.com) — no separate Google Cloud product involved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements AIService {

    private static final Pattern SAMPLE_RATE_PATTERN = Pattern.compile("rate=(\\d+)");
    private static final int DEFAULT_SAMPLE_RATE = 24000;
    private static final int MONO = 1;

    private final GeminiClient geminiClient;
    private final GeminiBetaClient geminiBetaClient;
    private final ObjectMapper objectMapper;
    private final AiLimitStatsRepository aiLimitStatsRepository;

    @Value("${gemini.api-key}")
    private String apiKey;

    /**
     * Comma-separated {@code model:dailyLimit:rpmLimit} entries, tried in order until one has quota
     * left. Both limits are checked against counters persisted in {@link AiLimitStats} so usage
     * survives server restarts; the daily counter resets when the calendar date changes and the RPM
     * counter resets once its one-minute window elapses.
     */
    @Value("${gemini.v1.models}")
    private String modelsConfig;

    @Value("${gemini.v1beta.tts-models}")
    private String ttsModelsConfig;

    @Value("${gemini.v1beta.tts-voice-name}")
    private String ttsVoiceName;

    private ModelLimits models;
    private ModelLimits ttsModels;

    @PostConstruct
    void init() {
        models = ModelLimits.parse("gemini.v1.models", modelsConfig);
        ttsModels = ModelLimits.parse("gemini.v1beta.tts-models", ttsModelsConfig);
    }

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        String text = withModelFallback(models, "generateContent", candidateModel -> {
            GeminiRequest request = GeminiRequest.builder()
                    .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(prompt).build()))))
                    .build();

            GeminiResponse response = geminiClient.generateContent(candidateModel, apiKey, request);

            String rawText = response.getCandidates().getFirst()
                    .getContent()
                    .getParts().getFirst()
                    .getText();

            String cleaned = rawText.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "").trim();
            log.info("GeminiService.generateContent model={} prompt={} response={}", candidateModel, prompt, cleaned);
            return cleaned;
        });

        if (responseType.equals(String.class)) {
            return responseType.cast(text);
        }

        try {
            return objectMapper.readValue(text, responseType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Gemini response into " + responseType.getSimpleName(), ex);
        }
    }

    @Override
    public byte[] synthesizeSpeechWav(String text) {
        return withModelFallback(ttsModels, "synthesizeSpeechWav", candidateModel -> {
            GeminiRequest request = GeminiRequest.builder()
                    .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(text).build()))))
                    .generationConfig(GeminiGenerationConfig.builder()
                            .responseModalities(List.of("AUDIO"))
                            .speechConfig(new GeminiSpeechConfig(
                                    new GeminiVoiceConfig(new GeminiPrebuiltVoiceConfig(ttsVoiceName))))
                            .build())
                    .build();

            GeminiResponse response = geminiBetaClient.generateContent(candidateModel, apiKey, request);

            GeminiInlineData audio = response.getCandidates().get(0)
                    .getContent()
                    .getParts().get(0)
                    .getInlineData();

            byte[] pcm = Base64.getDecoder().decode(audio.getData());
            int sampleRate = parseSampleRate(audio.getMimeType());

            log.info("GeminiService.synthesizeSpeechWav model={} text_length={} mime_type={} sample_rate={}",
                    candidateModel, text.length(), audio.getMimeType(), sampleRate);

            return WavEncoder.pcm16ToWav(pcm, sampleRate, MONO);
        });
    }

    /**
     * Calls {@code call} with each of {@code limits}' models in order, skipping any model whose
     * daily quota or RPM window is already exhausted (per {@link AiLimitStats}), and falling back to
     * the next model if the provider itself returns 429. Returns the first successful result.
     */
    private <T> T withModelFallback(ModelLimits limits, String operation, ModelCall<T> call) {
        for (String candidateModel : limits.models()) {
            int dailyLimit = limits.dailyLimits().get(candidateModel);
            AiLimitStats stats = findOrCreateStats(candidateModel);
            resetDailyCountIfElapsed(stats);
            if (stats.getRequestPerDateCount() >= dailyLimit) {
                log.info("GeminiService.{} model_skipped model={} reason=daily_quota_reached count={} limit={}",
                        operation, candidateModel, stats.getRequestPerDateCount(), dailyLimit);
                aiLimitStatsRepository.save(stats);
                continue;
            }

            int rpmLimit = limits.rpmLimits().get(candidateModel);
            resetRpmWindowIfElapsed(stats);
            if (stats.getRpmCount() >= rpmLimit) {
                log.info("GeminiService.{} model_skipped model={} reason=rpm_limit_reached limit={}",
                        operation, candidateModel, rpmLimit);
                aiLimitStatsRepository.save(stats);
                continue;
            }

            try {
                T result = call.call(candidateModel);
                stats.setRequestPerDateCount(stats.getRequestPerDateCount() + 1);
                stats.setRpmCount(stats.getRpmCount() + 1);
                aiLimitStatsRepository.save(stats);
                return result;
            } catch (FeignException.TooManyRequests ex) {
                log.warn("GeminiService.{} model_rate_limited model={}", operation, candidateModel);
                stats.setRequestPerDateCount(dailyLimit);
                stats.setRpmCount(rpmLimit);
                aiLimitStatsRepository.save(stats);
            }
        }

        throw new IllegalStateException(
                "All configured Gemini models for " + operation + " are rate-limited or over quota");
    }

    private AiLimitStats findOrCreateStats(String candidateModel) {
        return aiLimitStatsRepository.findByModel(candidateModel)
                .orElseGet(() -> {
                    AiLimitStats stats = new AiLimitStats();
                    stats.setModel(candidateModel);
                    stats.setRequestPerDateCount(0);
                    stats.setRequestCountDate(LocalDate.now());
                    stats.setRpmWindowStart(LocalDateTime.now());
                    stats.setRpmCount(0);
                    return aiLimitStatsRepository.save(stats);
                });
    }

    private void resetDailyCountIfElapsed(AiLimitStats stats) {
        LocalDate today = LocalDate.now();
        if (!today.equals(stats.getRequestCountDate())) {
            stats.setRequestCountDate(today);
            stats.setRequestPerDateCount(0);
        }
    }

    private void resetRpmWindowIfElapsed(AiLimitStats stats) {
        LocalDateTime now = LocalDateTime.now();
        if (stats.getRpmWindowStart() == null
                || Duration.between(stats.getRpmWindowStart(), now).toSeconds() >= 60) {
            stats.setRpmWindowStart(now);
            stats.setRpmCount(0);
        }
    }

    private int parseSampleRate(String mimeType) {
        if (mimeType == null) {
            return DEFAULT_SAMPLE_RATE;
        }
        Matcher matcher = SAMPLE_RATE_PATTERN.matcher(mimeType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : DEFAULT_SAMPLE_RATE;
    }

    @FunctionalInterface
    private interface ModelCall<T> {
        T call(String model);
    }

    /** Parsed {@code model:dailyLimit:rpmLimit} config for a Gemini endpoint. */
    private record ModelLimits(List<String> models, Map<String, Integer> dailyLimits, Map<String, Integer> rpmLimits) {

        static ModelLimits parse(String property, String config) {
            Map<String, Integer> dailyLimits = new LinkedHashMap<>();
            Map<String, Integer> rpmLimits = new LinkedHashMap<>();
            for (String entry : config.split(",")) {
                String trimmed = entry.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(":", 3);
                if (parts.length != 3) {
                    throw new IllegalStateException(
                            property + " entry must be in 'model:dailyLimit:rpmLimit' format, got: " + trimmed);
                }
                String modelName = parts[0].trim();
                dailyLimits.put(modelName, Integer.parseInt(parts[1].trim()));
                rpmLimits.put(modelName, Integer.parseInt(parts[2].trim()));
            }
            if (dailyLimits.isEmpty()) {
                throw new IllegalStateException(property + " must configure at least one model");
            }
            return new ModelLimits(List.copyOf(dailyLimits.keySet()), dailyLimits, rpmLimits);
        }
    }
}
