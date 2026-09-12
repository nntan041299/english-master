package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.Word;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByUserIdAndText(Long userId, String text);

    long countByUserId(Long userId);

    // Step 1: paginate by ID only (no collection fetch — DB does the pagination)
    Page<Word> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT w FROM Word w WHERE w.user.id = :userId AND LOWER(w.text) LIKE %:keyword%")
    Page<Word> findByUserIdAndTextContaining(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    // Step 2: fetch meanings for the paginated IDs
    @Query("SELECT w FROM Word w LEFT JOIN FETCH w.meanings WHERE w.id IN :ids")
    List<Word> findByIdsWithMeanings(@Param("ids") List<Long> ids);
}
