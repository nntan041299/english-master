package com.nntan041299.englishmasterservice.writing.entity;

/**
 * Category of a writing issue flagged by the AI. Used by the UI to colour-code highlights.
 */
public enum WritingIssueType {
    GRAMMAR,
    SPELLING,
    PUNCTUATION,
    VOCABULARY,
    WORD_ORDER,
    STYLE,
    CLARITY,
    OTHER
}
