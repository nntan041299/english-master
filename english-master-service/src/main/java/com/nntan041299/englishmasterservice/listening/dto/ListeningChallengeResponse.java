package com.nntan041299.englishmasterservice.listening.dto;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;

public record ListeningChallengeResponse(
        Long id,
        LanguageLevel level,
        String sentence) {}
