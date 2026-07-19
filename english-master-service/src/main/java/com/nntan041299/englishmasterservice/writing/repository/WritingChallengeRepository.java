package com.nntan041299.englishmasterservice.writing.repository;

import com.nntan041299.englishmasterservice.writing.entity.WritingChallenge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WritingChallengeRepository extends JpaRepository<WritingChallenge, Long> {

    Optional<WritingChallenge> findByIdAndUserId(Long id, Long userId);
}
