package com.nntan041299.englishmasterservice.translation.repository;

import com.nntan041299.englishmasterservice.translation.entity.TranslationSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TranslationSubmissionRepository extends JpaRepository<TranslationSubmission, Long> {

    @Query(
            value = "SELECT s FROM TranslationSubmission s JOIN FETCH s.challenge WHERE s.user.id = :userId",
            countQuery = "SELECT COUNT(s) FROM TranslationSubmission s WHERE s.user.id = :userId")
    Page<TranslationSubmission> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
