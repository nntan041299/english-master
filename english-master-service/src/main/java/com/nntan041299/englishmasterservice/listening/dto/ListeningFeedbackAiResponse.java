package com.nntan041299.englishmasterservice.listening.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ListeningFeedbackAiResponse(
        Boolean correct,
        String feedback) {}
