package com.nntan041299.englishmasterservice.ai.gemini;

import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiRequest;
import com.nntan041299.englishmasterservice.ai.gemini.dto.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the Gemini Generative Language API.
 */
@FeignClient(name = "gemini", url = "${gemini.v1.base-url}")
public interface GeminiClient {

    @PostMapping("/models/{model}:generateContent")
    GeminiResponse generateContent(@PathVariable("model") String model,
                                    @RequestParam("key") String apiKey,
                                    @RequestBody GeminiRequest body);
}
