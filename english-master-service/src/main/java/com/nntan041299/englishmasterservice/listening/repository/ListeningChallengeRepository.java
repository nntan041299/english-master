package com.nntan041299.englishmasterservice.listening.repository;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListeningChallengeRepository extends JpaRepository<ListeningChallenge, Long> {

    Optional<ListeningChallenge> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT c FROM ListeningChallenge c
            WHERE c.user.id = :userId
              AND c.level = :level
              AND NOT EXISTS (SELECT 1 FROM ListeningSubmission s WHERE s.challenge = c)
            ORDER BY c.id DESC
            """)
    List<ListeningChallenge> findUnsubmittedByUserIdAndLevel(
            @Param("userId") Long userId, @Param("level") LanguageLevel level, Pageable pageable);
}
