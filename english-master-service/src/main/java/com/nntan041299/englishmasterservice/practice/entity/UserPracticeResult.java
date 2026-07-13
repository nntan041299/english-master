package com.nntan041299.englishmasterservice.practice.entity;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.common.entity.BaseEntity;
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
@Table(name = "user_practice_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPracticeResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @Convert(converter = StringListConverter.class)
    @Column(name = "answered_option", nullable = false, columnDefinition = "TEXT")
    private List<String> answeredOptionIds;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct;
}
