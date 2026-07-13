package com.nntan041299.englishmasterservice.practice.repository;

import com.nntan041299.englishmasterservice.practice.entity.LearningTracking;
import com.nntan041299.englishmasterservice.practice.entity.UserPractice;
import com.nntan041299.englishmasterservice.word.repository.WordAvgPoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPracticeRepository extends JpaRepository<UserPractice, Long> {

    Optional<UserPractice> findByUserIdAndPracticeId(Long userId, Long practiceId);

    @Query("""
            SELECT m.word.id AS wordId, AVG(up.learningTracking) AS avgPoint
            FROM UserPractice up
            JOIN up.practice p
            JOIN p.meaning m
            WHERE up.user.id = :userId AND m.word.id IN :wordIds
            GROUP BY m.word.id
            """)
    List<WordAvgPoint> findAvgPointByUserIdAndWordIds(@Param("userId") Long userId, @Param("wordIds") List<Long> wordIds);

    @Query("""
            SELECT m.word.id AS wordId, AVG(up.learningTracking) AS avgPoint
            FROM UserPractice up
            JOIN up.practice p
            JOIN p.meaning m
            WHERE up.user.id = :userId
            GROUP BY m.word.id
            """)
    List<WordAvgPoint> findAvgPointByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT up FROM UserPractice up
            JOIN FETCH up.practice p
            JOIN FETCH p.meaning m
            JOIN FETCH m.word w
            WHERE up.user.id = :userId AND up.learningTracking != :finished
            """)
    List<UserPractice> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("finished") LearningTracking finished);
}
