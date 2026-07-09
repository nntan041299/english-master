package com.nntan041299.englishmasterservice.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AiPromptKey {

    WORD_ENRICHMENT("word.enrichment.prompt"),
    WORD_MEANING_PRACTICE_GENERATION("word.meaning.practice.generation.prompt");

    private final String propertyKey;
}
