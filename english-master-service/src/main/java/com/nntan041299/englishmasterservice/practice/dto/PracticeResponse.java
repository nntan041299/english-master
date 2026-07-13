package com.nntan041299.englishmasterservice.practice.dto;

import com.nntan041299.englishmasterservice.practice.entity.LearningTracking;
import com.nntan041299.englishmasterservice.practice.entity.PracticeOption;
import java.util.List;

public record PracticeResponse(
        Long wordId,
        String word,
        LearningTracking learningTracking,
        Long practiceId,
        String partOfSpeech,
        String meaning,
        String question,
        List<PracticeOption> options,
        List<String> correctAnswer
) {}
