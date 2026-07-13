package com.nntan041299.englishmasterservice.practice.dto;

import com.nntan041299.englishmasterservice.practice.entity.LearningTracking;
import java.util.List;

public record AnswerPracticeResponse(
        boolean correct,
        List<String> correctAnswer,
        LearningTracking newLearningTracking
) {}
