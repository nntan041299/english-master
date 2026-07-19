package com.nntan041299.englishmasterservice.writing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitWritingRequest(
        @NotNull Long challengeId,
        @NotBlank String text) {}
