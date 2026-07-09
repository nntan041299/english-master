package com.nntan041299.englishmasterservice.word.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WordMeaningPracticeAiResponse(
        int index,
        List<PracticeItem> practices
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PracticeItem(
            String option1,
            String option2,
            String option3,
            String option4,
            String correctAnswer
    ) {}
}
