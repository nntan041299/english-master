package com.nntan041299.englishmasterservice.ai.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.SpeechAIService;
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
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link AIService} / {@link SpeechAIService} implementation backed by the Gemini Generative
 * Language API (generativelanguage.googleapis.com) — no separate Google Cloud product involved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements AIService, SpeechAIService {

    private static final Pattern SAMPLE_RATE_PATTERN = Pattern.compile("rate=(\\d+)");
    private static final int DEFAULT_SAMPLE_RATE = 24000;
    private static final int MONO = 1;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.tts-model}")
    private String ttsModel;

    @Value("${gemini.tts-voice-name}")
    private String ttsVoiceName;

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(prompt).build()))))
                .build();

        GeminiResponse response = geminiClient.generateContent(model, apiKey, request);

        String text = response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
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
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(new GeminiContent(List.of(GeminiPart.builder().text(text).build()))))
                .generationConfig(GeminiGenerationConfig.builder()
                        .responseModalities(List.of("AUDIO"))
                        .speechConfig(new GeminiSpeechConfig(
                                new GeminiVoiceConfig(new GeminiPrebuiltVoiceConfig(ttsVoiceName))))
                        .build())
                .build();

        GeminiResponse response = geminiClient.generateContent(ttsModel, apiKey, request);

        GeminiInlineData audio = response.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getInlineData();

        byte[] pcm = Base64.getDecoder().decode(audio.getData());
        int sampleRate = parseSampleRate(audio.getMimeType());

        log.info("GeminiService.synthesizeSpeechWav text_length={} mime_type={} sample_rate={}",
                text.length(), audio.getMimeType(), sampleRate);

        return WavEncoder.pcm16ToWav(pcm, sampleRate, MONO);
    }

    private int parseSampleRate(String mimeType) {
        if (mimeType == null) {
            return DEFAULT_SAMPLE_RATE;
        }
        Matcher matcher = SAMPLE_RATE_PATTERN.matcher(mimeType);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : DEFAULT_SAMPLE_RATE;
    }
}
