package com.nntan041299.englishmasterservice.translation.dto;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.translation.entity.TranslationDirection;

public record TranslationChallengeResponse(
        Long id,
        TranslationDirection direction,
        LanguageLevel level,
        String sourceText) {}
