package com.nntan041299.englishmasterservice.translation.controller;

import com.nntan041299.englishmasterservice.translation.dto.SubmitTranslationRequest;
import com.nntan041299.englishmasterservice.translation.dto.TranslationChallengeResponse;
import com.nntan041299.englishmasterservice.translation.dto.TranslationFeedbackResponse;
import com.nntan041299.englishmasterservice.translation.entity.TranslationDirection;
import com.nntan041299.englishmasterservice.translation.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    /** Generates a challenge at the current user's own language level, in the requested direction. */
    @GetMapping("/challenge")
    public ResponseEntity<TranslationChallengeResponse> getChallenge(
            @RequestParam(name = "direction", defaultValue = "EN_TO_VI") TranslationDirection direction) {
        return ResponseEntity.ok(translationService.generateChallenge(direction));
    }

    @PostMapping("/submit")
    public ResponseEntity<TranslationFeedbackResponse> submit(@Valid @RequestBody SubmitTranslationRequest request) {
        return ResponseEntity.ok(translationService.submit(request));
    }
}
