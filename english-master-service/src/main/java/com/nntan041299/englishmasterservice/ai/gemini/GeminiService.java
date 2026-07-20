package com.nntan041299.englishmasterservice.ai.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.WavEncoder;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiContent;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiGenerationConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiInlineData;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiPart;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiPrebuiltVoiceConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiRequest;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiResponse;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiSpeechConfig;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiVoiceConfig;
import com.nntan041299.englishmasterservice.listening.entity.ListeningVoiceGenerationStats;
import com.nntan041299.englishmasterservice.listening.repository.ListeningVoiceGenerationStatsRepository;
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
    private final ListeningVoiceGenerationStatsRepository voiceGenerationStatsRepository;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    /**
     * Comma-separated {@code model:dailyLimit:rpmLimit} entries, tried in order until one has quota
     * left. Both limits are checked against counters persisted in {@link ListeningVoiceGenerationStats}
     * so usage survives server restarts; the daily counter resets when the calendar date changes and
     * the RPM counter resets once its one-minute window elapses.
     */
    @Value("${gemini.tts-models}")
    private String ttsModelsConfig;

    @Value("${gemini.tts-voice-name}")
    private String ttsVoiceName;

    private List<String> ttsModels;
    private Map<String, Integer> ttsModelDailyLimits;
    private Map<String, Integer> ttsModelRpmLimits;

    @PostConstruct
    void init() {
        Map<String, Integer> dailyLimits = new LinkedHashMap<>();
        Map<String, Integer> rpmLimits = new LinkedHashMap<>();
        for (String entry : ttsModelsConfig.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(":", 3);
            if (parts.length != 3) {
                throw new IllegalStateException(
                        "gemini.tts-models entry must be in 'model:dailyLimit:rpmLimit' format, got: " + trimmed);
            }
            String modelName = parts[0].trim();
            dailyLimits.put(modelName, Integer.parseInt(parts[1].trim()));
            rpmLimits.put(modelName, Integer.parseInt(parts[2].trim()));
        }
        if (dailyLimits.isEmpty()) {
            throw new IllegalStateException("gemini.tts-models must configure at least one model");
        }
        ttsModels = List.copyOf(dailyLimits.keySet());
        ttsModelDailyLimits = dailyLimits;
        ttsModelRpmLimits = rpmLimits;
    }

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(prompt).build()))))
                .build();

        GeminiResponse response = geminiClient.generateContent(model, apiKey, request);

        String text = response.getCandidates().getFirst()
                .getContent()
                .getParts().getFirst()
                .getText();

        text = text.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "").trim();

        log.info("GeminiService.generateContent prompt={} response={}", prompt, text);

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
        for (String candidateModel : ttsModels) {
            int dailyLimit = ttsModelDailyLimits.get(candidateModel);
            ListeningVoiceGenerationStats stats = findOrCreateStats(candidateModel);
            resetDailyCountIfElapsed(stats);
            if (stats.getRequestCount() >= dailyLimit) {
                log.info("GeminiService.synthesizeSpeechWav model_skipped model={} reason=daily_quota_reached count={} limit={}",
                        candidateModel, stats.getRequestCount(), dailyLimit);
                voiceGenerationStatsRepository.save(stats);
                continue;
            }

            int rpmLimit = ttsModelRpmLimits.get(candidateModel);
            resetRpmWindowIfElapsed(stats);
            if (stats.getRpmCount() >= rpmLimit) {
                log.info("GeminiService.synthesizeSpeechWav model_skipped model={} reason=rpm_limit_reached limit={}",
                        candidateModel, rpmLimit);
                voiceGenerationStatsRepository.save(stats);
                continue;
            }

            try {
                byte[] wav = callSynthesizeSpeechWav(candidateModel, text);
                stats.setRequestCount(stats.getRequestCount() + 1);
                stats.setRpmCount(stats.getRpmCount() + 1);
                voiceGenerationStatsRepository.save(stats);
                return wav;
            } catch (FeignException.TooManyRequests ex) {
                log.warn("GeminiService.synthesizeSpeechWav model_rate_limited model={}", candidateModel);
                stats.setRequestCount(dailyLimit);
                stats.setRpmCount(rpmLimit);
                voiceGenerationStatsRepository.save(stats);
            }
        }

        throw new IllegalStateException("All configured Gemini TTS models are rate-limited or over quota");
    }

    private byte[] callSynthesizeSpeechWav(String ttsModel, String text) {
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(text).build()))))
                .generationConfig(GeminiGenerationConfig.builder()
                        .responseModalities(List.of("AUDIO"))
                        .speechConfig(new GeminiSpeechConfig(
                                new GeminiVoiceConfig(new GeminiPrebuiltVoiceConfig(ttsVoiceName))))
                        .build())
                .build();

        GeminiResponse response = geminiBetaClient.generateContent(ttsModel, apiKey, request);

        GeminiInlineData audio = response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getInlineData();

        byte[] pcm = Base64.getDecoder().decode(audio.getData());
        int sampleRate = parseSampleRate(audio.getMimeType());

        log.info("GeminiService.synthesizeSpeechWav model={} text_length={} mime_type={} sample_rate={}",
                ttsModel, text.length(), audio.getMimeType(), sampleRate);

        return WavEncoder.pcm16ToWav(pcm, sampleRate, MONO);
    }

    private ListeningVoiceGenerationStats findOrCreateStats(String candidateModel) {
        return voiceGenerationStatsRepository.findByModel(candidateModel)
                .orElseGet(() -> {
                    ListeningVoiceGenerationStats stats = new ListeningVoiceGenerationStats();
                    stats.setModel(candidateModel);
                    stats.setRequestCount(0);
                    stats.setRequestCountDate(LocalDate.now());
                    stats.setRpmWindowStart(LocalDateTime.now());
                    stats.setRpmCount(0);
                    return voiceGenerationStatsRepository.save(stats);
                });
    }

    private void resetDailyCountIfElapsed(ListeningVoiceGenerationStats stats) {
        LocalDate today = LocalDate.now();
        if (!today.equals(stats.getRequestCountDate())) {
            stats.setRequestCountDate(today);
            stats.setRequestCount(0);
        }
    }

    private void resetRpmWindowIfElapsed(ListeningVoiceGenerationStats stats) {
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
}
