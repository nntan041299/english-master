package com.nntan041299.englishmasterservice.listening.repository;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListeningChallengeRepository extends JpaRepository<ListeningChallenge, Long> {

    @Query("""
            SELECT c FROM ListeningChallenge c
            WHERE c.level = :level
              AND NOT EXISTS (
                  SELECT 1 FROM ListeningSubmission s
                  WHERE s.challenge = c AND s.user.id = :userId
              )
            ORDER BY c.id ASC
            """)
    List<ListeningChallenge> findAvailableForUser(
            @Param("level") LanguageLevel level, @Param("userId") Long userId, Pageable pageable);

    /** Counts challenges at this level that have no submission yet, from any user. */
    @Query("""
            SELECT COUNT(c) FROM ListeningChallenge c
            WHERE c.level = :level
              AND NOT EXISTS (SELECT 1 FROM ListeningSubmission s WHERE s.challenge = c)
            """)
    long countUnsubmittedByLevel(@Param("level") LanguageLevel level);
}
