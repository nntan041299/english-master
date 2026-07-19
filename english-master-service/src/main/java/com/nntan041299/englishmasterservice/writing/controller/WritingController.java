package com.nntan041299.englishmasterservice.writing.controller;

import com.nntan041299.englishmasterservice.writing.dto.SubmitWritingRequest;
import com.nntan041299.englishmasterservice.writing.dto.WritingChallengeResponse;
import com.nntan041299.englishmasterservice.writing.dto.WritingFeedbackResponse;
import com.nntan041299.englishmasterservice.writing.service.WritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
public class WritingController {

    private final WritingService writingService;

    /** Generates a challenge at the current user's own language level (set on their account). */
    @GetMapping("/challenge")
    public ResponseEntity<WritingChallengeResponse> getChallenge() {
        return ResponseEntity.ok(writingService.generateChallenge());
    }

    @PostMapping("/submit")
    public ResponseEntity<WritingFeedbackResponse> submit(@Valid @RequestBody SubmitWritingRequest request) {
        return ResponseEntity.ok(writingService.submit(request));
    }
}
