package com.nntan041299.englishmasterservice.writing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WritingChallengeAiResponse(
        String title,
        String prompt) {}
