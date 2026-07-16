package com.nntan041299.englishmasterservice.practice.service;

import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import java.util.List;

public interface SingleChoicePracticeGenerationService {

    PracticeType getType();

    void generate(List<Meaning> meanings);
}
