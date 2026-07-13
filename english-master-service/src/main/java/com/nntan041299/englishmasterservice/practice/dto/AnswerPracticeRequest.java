package com.nntan041299.englishmasterservice.practice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AnswerPracticeRequest(
        @NotNull Long wordId,
        @NotNull Long practiceId,
        @NotEmpty List<String> selectedOptionIds
) {}
