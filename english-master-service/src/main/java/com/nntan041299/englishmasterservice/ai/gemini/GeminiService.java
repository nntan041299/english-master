package com.nntan041299.englishmasterservice.ai.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiContent;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiPart;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiRequest;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link AIService} implementation backed by the Gemini Generative Language API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService implements AIService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiContent(
                        List.of(new GeminiPart(prompt))
                ))
        );

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
}
