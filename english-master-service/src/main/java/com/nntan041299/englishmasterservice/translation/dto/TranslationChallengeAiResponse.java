package com.nntan041299.englishmasterservice.translation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TranslationChallengeAiResponse(String sourceText) {}
