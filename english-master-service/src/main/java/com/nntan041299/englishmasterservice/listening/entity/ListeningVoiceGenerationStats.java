package com.nntan041299.englishmasterservice.listening.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single-row table counting how many AI voice-generation calls have been made in total, so the
 * generation job can stop once the free-tier budget is used up.
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

    @Column(name = "request_count", nullable = false)
    private int requestCount;
}
