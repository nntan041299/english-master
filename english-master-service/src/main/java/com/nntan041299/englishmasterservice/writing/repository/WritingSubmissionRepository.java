package com.nntan041299.englishmasterservice.writing.repository;

import com.nntan041299.englishmasterservice.writing.entity.WritingSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, Long> {

    @Query(
            value = "SELECT s FROM WritingSubmission s JOIN FETCH s.challenge WHERE s.user.id = :userId",
            countQuery = "SELECT COUNT(s) FROM WritingSubmission s WHERE s.user.id = :userId")
    Page<WritingSubmission> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
