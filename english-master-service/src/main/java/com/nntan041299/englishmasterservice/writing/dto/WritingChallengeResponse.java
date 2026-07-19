package com.nntan041299.englishmasterservice.writing.dto;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;

public record WritingChallengeResponse(
        Long id,
        LanguageLevel level,
        int minWords,
        int maxWords,
        String title,
        String prompt) {}
