package com.nntan041299.englishmasterservice.ai.repository;

import com.nntan041299.englishmasterservice.ai.entity.AiLimitStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiLimitStatsRepository extends JpaRepository<AiLimitStats, Long> {

    Optional<AiLimitStats> findByModel(String model);
}
