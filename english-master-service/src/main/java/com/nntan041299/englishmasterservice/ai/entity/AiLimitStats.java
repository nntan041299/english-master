package com.nntan041299.englishmasterservice.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Counts how many AI calls have been made per Gemini model, so the caller can stop using a model
 * once its daily quota or requests-per-minute window is exhausted and fall back to the next
 * configured model. Both windows are persisted (rather than kept in memory) so they survive server
 * restarts.
 */
@Entity
@Table(name = "ai_limit_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiLimitStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model", nullable = false, unique = true)
    private String model;

    @Column(name = "request_per_date_count", nullable = false)
    private int requestPerDateCount;

    @Column(name = "request_count_date", nullable = false)
    private LocalDate requestCountDate;

    @Column(name = "rpm_window_start")
    private LocalDateTime rpmWindowStart;

    @Column(name = "rpm_count", nullable = false)
    private int rpmCount;
}
