package com.nntan041299.englishmasterservice.translation.repository;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.translation.entity.TranslationChallenge;
import com.nntan041299.englishmasterservice.translation.entity.TranslationDirection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TranslationChallengeRepository extends JpaRepository<TranslationChallenge, Long> {

    Optional<TranslationChallenge> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT c FROM TranslationChallenge c
            WHERE c.user.id = :userId
              AND c.direction = :direction
              AND c.level = :level
              AND NOT EXISTS (SELECT 1 FROM TranslationSubmission s WHERE s.challenge = c)
            ORDER BY c.id DESC
            """)
    List<TranslationChallenge> findUnsubmittedByUserIdAndDirectionAndLevel(
            @Param("userId") Long userId,
            @Param("direction") TranslationDirection direction,
            @Param("level") LanguageLevel level,
            Pageable pageable);
}
