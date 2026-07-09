package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.PracticeOption;
import jakarta.validation.constraints.NotNull;

public record AnswerPracticeRequest(
        @NotNull Long wordId,
        @NotNull Long practiceId,
        @NotNull PracticeOption selectedOption
) {}
