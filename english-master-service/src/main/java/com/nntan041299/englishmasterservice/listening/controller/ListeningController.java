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

    /** Generates a challenge at the current user's own language level. */
    @GetMapping("/challenge")
    public ResponseEntity<ListeningChallengeResponse> getChallenge() {
        return ResponseEntity.ok(listeningService.generateChallenge());
    }

    /** Streams back the challenge's sentence synthesized as MP3 audio. */
    @GetMapping(value = "/{challengeId}/audio", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getAudio(@PathVariable Long challengeId) {
        byte[] audio = listeningService.synthesizeAudio(challengeId);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(audio);
    }

    @PostMapping("/submit")
    public ResponseEntity<ListeningFeedbackResponse> submit(@Valid @RequestBody SubmitListeningRequest request) {
        return ResponseEntity.ok(listeningService.submit(request));
    }
}
