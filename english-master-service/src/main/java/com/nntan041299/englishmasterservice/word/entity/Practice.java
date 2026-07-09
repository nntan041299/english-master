package com.nntan041299.englishmasterservice.word.entity;

import com.nntan041299.englishmasterservice.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "practices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Practice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meaning_id", nullable = false)
    private Meaning meaning;

    @Column(name = "option_1", nullable = false, length = 500)
    private String option1;

    @Column(name = "option_2", nullable = false, length = 500)
    private String option2;

    @Column(name = "option_3", nullable = false, length = 500)
    private String option3;

    @Column(name = "option_4", nullable = false, length = 500)
    private String option4;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_answer", nullable = false, length = 20)
    private PracticeOption correctAnswer;
}
