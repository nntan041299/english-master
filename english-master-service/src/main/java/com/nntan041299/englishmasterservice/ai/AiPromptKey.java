package com.nntan041299.englishmasterservice.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AiPromptKey {

    WORD_ENRICHMENT("word.enrichment.prompt"),
    WORD_TRANSLATION_PRACTICE_GENERATION("word.translation.practice.generation.prompt"),
    SYNONYM_PRACTICE_GENERATION("synonym.practice.generation.prompt"),
    WRITING_CHALLENGE_GENERATION("writing.challenge.generation.prompt"),
    WRITING_FEEDBACK("writing.feedback.prompt");

    private final String propertyKey;
}
