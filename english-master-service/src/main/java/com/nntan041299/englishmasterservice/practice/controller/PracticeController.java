package com.nntan041299.englishmasterservice.practice.controller;

import com.nntan041299.englishmasterservice.practice.dto.AnswerPracticeRequest;
import com.nntan041299.englishmasterservice.practice.dto.AnswerPracticeResponse;
import com.nntan041299.englishmasterservice.practice.dto.PracticeResponse;
import com.nntan041299.englishmasterservice.practice.service.PracticeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/practices")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    @GetMapping
    public ResponseEntity<List<PracticeResponse>> getDuePractices() {
        return ResponseEntity.ok(practiceService.getDuePractices());
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerPracticeResponse> answerPractice(@Valid @RequestBody AnswerPracticeRequest request) {
        return ResponseEntity.ok(practiceService.answerPractice(request));
    }
}
