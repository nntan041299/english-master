package com.nntan041299.englishmasterservice.writing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nntan041299.englishmasterservice.writing.entity.WritingIssueType;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WritingFeedbackAiResponse(
        String overallFeedback,
        Integer score,
        List<Issue> issues) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Issue(
            String original,
            String suggestion,
            String explanation,
            WritingIssueType type) {}
}
