package com.nntan041299.englishmasterservice.word.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Partial update for one of a word's meanings, nested inside {@link UpdateWordRequest}.
 * Every field but {@code id} is optional and, when present, replaces that field only —
 * omitted fields are left untouched. New editable fields (e.g. partOfSpeech, ipa,
 * categories) can be added here without changing the endpoint contract.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeaningRequest {

    @NotNull(message = "Meaning id is required")
    private Long id;

    private String meaning;
}
