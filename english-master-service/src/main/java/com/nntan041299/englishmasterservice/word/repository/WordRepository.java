package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.Word;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByText(String text);

    @Query("SELECT w FROM Word w WHERE NOT EXISTS (SELECT m FROM Meaning m WHERE m.word = w)")
    List<Word> findWordsWithoutMeanings(Pageable pageable);
}
