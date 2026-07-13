package com.nntan041299.englishmasterservice.practice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nntan041299.englishmasterservice.practice.entity.PracticeOption;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PracticeAiResponse(
        int index,
        List<PracticeItem> practices
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PracticeItem(
            String question,
            List<PracticeOption> options,
            List<String> correctAnswer
    ) {}
}
