package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.LearningTracking;
import com.nntan041299.englishmasterservice.word.entity.PracticeOption;

public record AnswerPracticeResponse(
        boolean correct,
        PracticeOption correctAnswer,
        LearningTracking newLearningTracking
) {}
