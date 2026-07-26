package com.nntan041299.englishmasterservice.practice.repository;

import com.nntan041299.englishmasterservice.practice.entity.UserPracticeResult;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPracticeResultRepository extends JpaRepository<UserPracticeResult, Long> {

    long countByUserId(Long userId);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN r.correct = true THEN 1 ELSE 0 END), 0) AS correctCount,
                   COUNT(r) AS totalCount
            FROM UserPracticeResult r
            WHERE r.user.id = :userId AND r.createdAt BETWEEN :start AND :end
            """)
    PracticeStatBucket findStatsByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
