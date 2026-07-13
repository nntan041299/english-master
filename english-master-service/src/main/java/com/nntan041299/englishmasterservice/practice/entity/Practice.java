package com.nntan041299.englishmasterservice.practice.entity;

import com.nntan041299.englishmasterservice.common.entity.BaseEntity;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
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

    @Convert(converter = PracticeOptionListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<PracticeOption> options;

    @Column(name = "question", length = 1000)
    private String question;

    @Convert(converter = StringListConverter.class)
    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private List<String> correctAnswer;
}
