package com.nntan041299.englishmasterservice.writing.dto;

import java.util.List;

public record WritingFeedbackResponse(
        Long submissionId,
        String overallFeedback,
        Integer score,
        List<WritingIssueResponse> issues) {}
