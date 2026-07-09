package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.Meaning;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeaningRepository extends JpaRepository<Meaning, Long> {

    List<Meaning> findByWordId(Long wordId);

    @Query("SELECT m FROM Meaning m WHERE m.partOfSpeech <> 'OTHER' AND NOT EXISTS (SELECT 1 FROM Practice p WHERE p.meaning = m)")
    List<Meaning> findMeaningsWithoutPractices(Pageable pageable);
}
