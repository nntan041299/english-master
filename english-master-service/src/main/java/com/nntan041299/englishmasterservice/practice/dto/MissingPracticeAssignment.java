package com.nntan041299.englishmasterservice.practice.dto;

import com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource;

/**
 * A candidate practice that a user is missing: the user has saved the word behind {@code meaningId}
 * but has no practice yet for the {@code (meaningId, creationSource)} pair. One candidate is emitted
 * per existing practice in a missing group, so the assignment job can pick one at random.
 */
public record MissingPracticeAssignment(
        Long userId,
        Long practiceId,
        Long meaningId,
        PracticeCreationSource creationSource) {}
