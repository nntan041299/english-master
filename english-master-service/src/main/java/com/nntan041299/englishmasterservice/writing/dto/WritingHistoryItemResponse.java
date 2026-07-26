package com.nntan041299.englishmasterservice.writing.dto;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.writing.entity.WritingSubmission;
import java.time.LocalDateTime;
import java.util.List;

public record WritingHistoryItemResponse(
        Long submissionId,
        Long challengeId,
        LanguageLevel level,
        String title,
        String prompt,
        String text,
        String overallFeedback,
        Integer score,
        List<WritingIssueResponse> issues,
        LocalDateTime submittedAt) {

    public static WritingHistoryItemResponse from(WritingSubmission submission) {
        return new WritingHistoryItemResponse(
                submission.getId(),
                submission.getChallenge().getId(),
                submission.getChallenge().getLevel(),
                submission.getChallenge().getTitle(),
                submission.getChallenge().getPrompt(),
                submission.getText(),
                submission.getOverallFeedback(),
                submission.getScore(),
                submission.getIssues().stream().map(WritingIssueResponse::from).toList(),
                submission.getCreatedAt());
    }
}
