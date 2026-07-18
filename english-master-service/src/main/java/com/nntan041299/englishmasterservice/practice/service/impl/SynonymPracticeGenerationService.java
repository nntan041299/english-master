package com.nntan041299.englishmasterservice.practice.service.impl;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import org.springframework.stereotype.Service;

@Service
public class SynonymPracticeGenerationService extends AbstractPracticeGenerationService {

    public SynonymPracticeGenerationService(
            MeaningRepository meaningRepository,
            PracticeRepository practiceRepository,
            AIService aiService,
            AiPromptManager aiPromptManager) {
        super(meaningRepository, practiceRepository, aiService, aiPromptManager);
    }

    @Override
    protected AiPromptKey getPromptKey() {
        return AiPromptKey.SYNONYM_PRACTICE_GENERATION;
    }

    @Override
    public PracticeCreationSource getSource() {
        return PracticeCreationSource.SYNONYM;
    }
}
