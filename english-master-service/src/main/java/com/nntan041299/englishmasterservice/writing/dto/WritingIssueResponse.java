package com.nntan041299.englishmasterservice.writing.dto;

import com.nntan041299.englishmasterservice.writing.entity.WritingIssue;
import com.nntan041299.englishmasterservice.writing.entity.WritingIssueType;

public record WritingIssueResponse(
        String original,
        String suggestion,
        String explanation,
        WritingIssueType type) {

    public static WritingIssueResponse from(WritingIssue issue) {
        return new WritingIssueResponse(
                issue.original(), issue.suggestion(), issue.explanation(), issue.type());
    }
}
