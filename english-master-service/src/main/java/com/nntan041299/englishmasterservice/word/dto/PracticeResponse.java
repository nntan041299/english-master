package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.LearningTracking;
import com.nntan041299.englishmasterservice.word.entity.PracticeOption;
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
