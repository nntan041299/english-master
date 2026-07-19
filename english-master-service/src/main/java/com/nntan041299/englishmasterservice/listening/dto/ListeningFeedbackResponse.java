package com.nntan041299.englishmasterservice.listening.dto;

public record ListeningFeedbackResponse(
        Long submissionId,
        boolean correct,
        String feedback,
        String sentence) {}
