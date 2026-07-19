package com.nntan041299.englishmasterservice.writing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single mistake or improvement the AI found, stored as JSON on {@link WritingSubmission}.
 *
 * @param original    the exact snippet from the user's text (so the UI can locate and highlight it)
 * @param suggestion  the improved/corrected replacement for {@code original}
 * @param explanation short reason the original is wrong or can be improved
 * @param type        category used for colour-coding the highlight
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WritingIssue(
        String original,
        String suggestion,
        String explanation,
        WritingIssueType type) {}
