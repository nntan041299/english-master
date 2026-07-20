package com.nntan041299.englishmasterservice.listening.entity;

import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A pre-generated listening challenge, shared across all users at its {@link #level} — not owned by
 * a single user, since each one costs a limited AI voice-generation call to produce (see
 * {@link com.nntan041299.englishmasterservice.ai.entity.AiLimitStats}). Whether a given user has already answered one is tracked via
 * {@link ListeningSubmission}, not on this entity.
 */
@Entity
@Table(name = "listening_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningChallenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private LanguageLevel level;

    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    @Column(name = "voice_data", nullable = false)
    private byte[] voiceData;
}
