package com.nntan041299.englishmasterservice.word.repository;

import com.nntan041299.englishmasterservice.word.entity.Practice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeRepository extends JpaRepository<Practice, Long> {

    @Query("SELECT p FROM Practice p WHERE p.meaning.id IN :meaningIds")
    List<Practice> findByMeaningIds(@Param("meaningIds") List<Long> meaningIds);
}
