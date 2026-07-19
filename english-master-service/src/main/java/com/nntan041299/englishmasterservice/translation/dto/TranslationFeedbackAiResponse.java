package com.nntan041299.englishmasterservice.translation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TranslationFeedbackAiResponse(
        Boolean correct,
        String feedback,
        String suggestedTranslation) {}
