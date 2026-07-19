package com.nntan041299.englishmasterservice.translation.dto;

public record TranslationFeedbackResponse(
        Long submissionId,
        boolean correct,
        String feedback,
        String suggestedTranslation) {}
