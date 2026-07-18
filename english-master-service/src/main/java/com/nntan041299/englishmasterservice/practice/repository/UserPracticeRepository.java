package com.nntan041299.englishmasterservice.practice.repository;

import com.nntan041299.englishmasterservice.practice.dto.MissingPracticeAssignment;
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

    /**
     * Finds the practices a user is missing per creation source. For every word a user has saved,
     * each of its meanings should have one assigned practice per creation source. This returns every
     * practice belonging to a {@code (user, meaning, creationSource)} group where the user has no
     * assignment yet, so the caller can pick one candidate at random per group.
     */
    @Query("""
            SELECT new com.nntan041299.englishmasterservice.practice.dto.MissingPracticeAssignment(
                uw.user.id, p.id, m.id, p.creationSource)
            FROM UserWord uw
            JOIN uw.word w
            JOIN w.meanings m
            JOIN Practice p ON p.meaning = m
            WHERE NOT EXISTS (
                SELECT 1 FROM UserPractice up
                WHERE up.user.id = uw.user.id
                  AND up.practice.meaning = m
                  AND up.practice.creationSource = p.creationSource
            )
            """)
    List<MissingPracticeAssignment> findMissingAssignments();

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
