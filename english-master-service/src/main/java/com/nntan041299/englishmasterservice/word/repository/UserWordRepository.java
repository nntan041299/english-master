package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.UserWord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserWordRepository extends JpaRepository<UserWord, Long> {

    Optional<UserWord> findByUserIdAndWordId(Long userId, Long wordId);

    @Query("SELECT uw FROM UserWord uw JOIN FETCH uw.word w JOIN FETCH w.meanings WHERE uw.practicesAssigned = false")
    List<UserWord> findUnassigned();

    @Query("SELECT uw FROM UserWord uw JOIN FETCH uw.word w LEFT JOIN FETCH w.meanings WHERE uw.user.id = :userId")
    Page<UserWord> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT uw FROM UserWord uw JOIN FETCH uw.word w LEFT JOIN FETCH w.meanings WHERE uw.user.id = :userId AND LOWER(w.text) LIKE %:keyword%")
    Page<UserWord> findByUserIdAndWordTextContaining(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}
