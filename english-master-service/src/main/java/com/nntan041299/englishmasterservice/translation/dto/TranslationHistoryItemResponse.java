package com.nntan041299.englishmasterservice.translation.dto;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.translation.entity.TranslationDirection;
import com.nntan041299.englishmasterservice.translation.entity.TranslationSubmission;
import java.time.LocalDateTime;

public record TranslationHistoryItemResponse(
        Long submissionId,
        Long challengeId,
        TranslationDirection direction,
        LanguageLevel level,
        String sourceText,
        String userTranslation,
        boolean correct,
        String feedback,
        String suggestedTranslation,
        LocalDateTime submittedAt) {

    public static TranslationHistoryItemResponse from(TranslationSubmission submission) {
        return new TranslationHistoryItemResponse(
                submission.getId(),
                submission.getChallenge().getId(),
                submission.getChallenge().getDirection(),
                submission.getChallenge().getLevel(),
                submission.getChallenge().getSourceText(),
                submission.getUserTranslation(),
                submission.isCorrect(),
                submission.getFeedback(),
                submission.getSuggestedTranslation(),
                submission.getCreatedAt());
    }
}
