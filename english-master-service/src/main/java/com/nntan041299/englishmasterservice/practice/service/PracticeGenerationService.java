package com.nntan041299.englishmasterservice.practice.service;

import com.nntan041299.englishmasterservice.practice.entity.PracticeType;

public interface PracticeGenerationService {

    PracticeType getType();

    void generate();
}
