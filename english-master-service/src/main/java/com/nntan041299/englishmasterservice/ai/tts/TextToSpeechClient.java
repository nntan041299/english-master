package com.nntan041299.englishmasterservice.ai.tts;

import com.nntan041299.englishmasterservice.ai.tts.dto.TextToSpeechRequest;
import com.nntan041299.englishmasterservice.ai.tts.dto.TextToSpeechResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the Google Cloud Text-to-Speech API.
 */
@FeignClient(name = "google-tts", url = "${google.tts.base-url}")
public interface TextToSpeechClient {

    @PostMapping("/text:synthesize")
    TextToSpeechResponse synthesize(@RequestParam("key") String apiKey, @RequestBody TextToSpeechRequest body);
}
