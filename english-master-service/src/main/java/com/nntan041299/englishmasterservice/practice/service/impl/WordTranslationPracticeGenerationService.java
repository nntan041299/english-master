package com.nntan041299.englishmasterservice.practice.service.impl;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import org.springframework.stereotype.Service;

@Service
public class WordTranslationPracticeGenerationService extends AbstractSingleChoicePracticeGenerationService {

    public WordTranslationPracticeGenerationService(
            MeaningRepository meaningRepository,
            PracticeRepository practiceRepository,
            AIService aiService,
            AiPromptManager aiPromptManager) {
        super(meaningRepository, practiceRepository, aiService, aiPromptManager);
    }

    @Override
    protected AiPromptKey getPromptKey() {
        return AiPromptKey.WORD_TRANSLATION_PRACTICE_GENERATION;
    }

    @Override
    public PracticeCreationSource getSource() {
        return PracticeCreationSource.WORD_TRANSLATION;
    }
}
