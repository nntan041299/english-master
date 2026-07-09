package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.LearningLevel;
import com.nntan041299.englishmasterservice.word.entity.PracticeOption;

public record AnswerPracticeResponse(
        boolean correct,
        PracticeOption correctAnswer,
        LearningLevel newLevel
) {}
