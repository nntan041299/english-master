package com.nntan041299.englishmasterservice.translation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitTranslationRequest(
        @NotNull Long challengeId,
        @NotBlank String translation) {}
