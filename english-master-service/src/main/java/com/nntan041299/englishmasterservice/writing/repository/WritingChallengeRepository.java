package com.nntan041299.englishmasterservice.writing.repository;

import com.nntan041299.englishmasterservice.writing.entity.WritingChallenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WritingChallengeRepository extends JpaRepository<WritingChallenge, Long> {

    Optional<WritingChallenge> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT c FROM WritingChallenge c
            WHERE c.user.id = :userId
              AND NOT EXISTS (SELECT 1 FROM WritingSubmission s WHERE s.challenge = c)
            ORDER BY c.id DESC
            """)
    List<WritingChallenge> findUnsubmittedByUserId(@Param("userId") Long userId, Pageable pageable);
}
