package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.Word;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findByText(String text);
}
