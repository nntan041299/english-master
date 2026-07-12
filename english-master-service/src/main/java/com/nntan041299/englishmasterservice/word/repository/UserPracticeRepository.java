package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.UserPractice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPracticeRepository extends JpaRepository<UserPractice, Long> {

    Optional<UserPractice> findByUserIdAndPracticeId(Long userId, Long practiceId);

    @Query("""
            SELECT up FROM UserPractice up
            JOIN FETCH up.practice p
            JOIN FETCH p.meaning m
            JOIN FETCH m.word w
            WHERE up.user.id = :userId
            AND (
                up.lastPracticedAt IS NULL
                OR (up.learningTracking = 'TRACKING1' AND up.lastPracticedAt <= :cutoffTracking1)
                OR (up.learningTracking = 'TRACKING2' AND up.lastPracticedAt <= :cutoffTracking2)
                OR (up.learningTracking = 'TRACKING3' AND up.lastPracticedAt <= :cutoffTracking3)
                OR (up.learningTracking = 'TRACKING4' AND up.lastPracticedAt <= :cutoffTracking4)
                OR (up.learningTracking = 'TRACKING5' AND up.lastPracticedAt <= :cutoffTracking5)
            )
            """)
    List<UserPractice> findDueByUserId(
            @Param("userId") Long userId,
            @Param("cutoffTracking1") LocalDateTime cutoffTracking1,
            @Param("cutoffTracking2") LocalDateTime cutoffTracking2,
            @Param("cutoffTracking3") LocalDateTime cutoffTracking3,
            @Param("cutoffTracking4") LocalDateTime cutoffTracking4,
            @Param("cutoffTracking5") LocalDateTime cutoffTracking5);
}
