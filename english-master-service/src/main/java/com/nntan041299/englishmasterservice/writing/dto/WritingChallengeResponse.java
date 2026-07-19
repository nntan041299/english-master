package com.nntan041299.englishmasterservice.writing.dto;

import com.nntan041299.englishmasterservice.writing.entity.WritingLevel;

public record WritingChallengeResponse(
        Long id,
        WritingLevel level,
        String title,
        String prompt) {}
