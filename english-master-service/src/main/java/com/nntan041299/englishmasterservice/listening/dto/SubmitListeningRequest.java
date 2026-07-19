package com.nntan041299.englishmasterservice.listening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitListeningRequest(
        @NotNull Long challengeId,
        @NotBlank String transcript) {}
