package com.nntan041299.englishmasterservice.practice.service;

import com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource;
import com.nntan041299.englishmasterservice.practice.entity.PracticeType;

public interface PracticeGenerationService {

    PracticeType getType();

    PracticeCreationSource getSource();

    void generate();
}
