package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.LearningTracking;
import java.util.List;

public record AnswerPracticeResponse(
        boolean correct,
        List<String> correctAnswer,
        LearningTracking newLearningTracking
) {}
