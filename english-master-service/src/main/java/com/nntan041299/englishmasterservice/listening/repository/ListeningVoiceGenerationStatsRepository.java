package com.nntan041299.englishmasterservice.listening.repository;

import com.nntan041299.englishmasterservice.listening.entity.ListeningVoiceGenerationStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningVoiceGenerationStatsRepository extends JpaRepository<ListeningVoiceGenerationStats, Long> {

    Optional<ListeningVoiceGenerationStats> findFirstByOrderByIdAsc();
}
