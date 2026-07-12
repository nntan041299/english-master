package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.LearningTracking;
import com.nntan041299.englishmasterservice.word.entity.PracticeOption;

public record PracticeResponse(
        Long wordId,
        String word,
        LearningTracking learningTracking,
        Long practiceId,
        String partOfSpeech,
        String meaning,
        String option1,
        String option2,
        String option3,
        String option4,
        PracticeOption correctAnswer
) {}
