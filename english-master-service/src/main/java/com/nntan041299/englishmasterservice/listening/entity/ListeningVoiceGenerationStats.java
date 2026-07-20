package com.nntan041299.englishmasterservice.listening.entity;

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
 * Counts how many AI voice-generation calls have been made per Gemini TTS model, so the caller can
 * stop using a model once its daily quota or requests-per-minute window is exhausted and fall back
 * to the next configured model. The RPM window is persisted (rather than kept in memory) so it
 * survives server restarts.
 */
@Entity
@Table(name = "listening_voice_generation_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListeningVoiceGenerationStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model", nullable = false, unique = true)
    private String model;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "request_count_date", nullable = false)
    private LocalDate requestCountDate;

    @Column(name = "rpm_window_start")
    private LocalDateTime rpmWindowStart;

    @Column(name = "rpm_count", nullable = false)
    private int rpmCount;
}
