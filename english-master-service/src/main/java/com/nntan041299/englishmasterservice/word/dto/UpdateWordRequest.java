package com.nntan041299.englishmasterservice.word.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Partial update for a word. Every field is optional and, when present, replaces that
 * field only — omitted fields are left untouched. Meaning edits are just one part of what
 * can be updated on a word; new word-level fields can be added here later without
 * changing the endpoint contract.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWordRequest {

    private String text;

    @Valid
    private List<UpdateMeaningRequest> meanings;
}
