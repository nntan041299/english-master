package com.nntan041299.englishmasterservice.practice.repository;

import com.nntan041299.englishmasterservice.practice.entity.UserPracticeResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPracticeResultRepository extends JpaRepository<UserPracticeResult, Long> {

    long countByUserId(Long userId);
}
