package com.nntan041299.englishmasterservice.listening.controller;

import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningFeedbackResponse;
import com.nntan041299.englishmasterservice.listening.dto.SubmitListeningRequest;
import com.nntan041299.englishmasterservice.listening.service.ListeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/listening")
@RequiredArgsConstructor
public class ListeningController {

    private final ListeningService listeningService;

    /**
     * Returns a challenge at the current user's own language level from the pre-generated pool, or
     * 204 No Content if none are available yet.
     */
    @GetMapping("/challenge")
    public ResponseEntity<ListeningChallengeResponse> getChallenge() {
        return listeningService.getChallenge()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Streams back the challenge's pre-synthesized WAV audio. No "produces" restriction here — the
     * frontend's axios client sends {@code Accept: application/json} by default, which would fail
     * content negotiation against a fixed audio/wav mapping. The content type is set explicitly on
     * the response instead.
     */
    @GetMapping("/{challengeId}/audio")
    public ResponseEntity<byte[]> getAudio(@PathVariable Long challengeId) {
        byte[] audio = listeningService.getAudio(challengeId);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/wav"))
                .body(audio);
    }

    @PostMapping("/submit")
    public ResponseEntity<ListeningFeedbackResponse> submit(@Valid @RequestBody SubmitListeningRequest request) {
        return ResponseEntity.ok(listeningService.submit(request));
    }
}
