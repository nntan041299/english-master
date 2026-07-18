package com.nntan041299.englishmasterservice.practice.service.impl;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse.PracticeItem;
import com.nntan041299.englishmasterservice.practice.entity.Practice;
import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Base for {@link PracticeType#SINGLE_CHOICE} generators. It consumes the shared
 * {@link PracticeAiResponse} format (an indexed batch of multiple-choice items) and maps each item
 * into a single-choice {@link Practice}. Subclasses only declare their creation source and prompt.
 */
@Slf4j
public abstract class AbstractSingleChoicePracticeGenerationService
        extends AbstractPracticeGenerationService<PracticeAiResponse> {

    protected AbstractSingleChoicePracticeGenerationService(
            MeaningRepository meaningRepository,
            PracticeRepository practiceRepository,
            AIService aiService,
            AiPromptManager aiPromptManager) {
        super(meaningRepository, practiceRepository, aiService, aiPromptManager);
    }

    @Override
    public PracticeType getType() {
        return PracticeType.SINGLE_CHOICE;
    }

    @Override
    protected Class<PracticeAiResponse[]> getResponseType() {
        return PracticeAiResponse[].class;
    }

    @Override
    protected List<Practice> mapResponse(PracticeAiResponse response, List<Meaning> meanings, String logPrefix) {
        int idx = response.index();
        if (idx < 0 || idx >= meanings.size()) {
            log.warn("{} unknown_index index={}", logPrefix, idx);
            return List.of();
        }

        Meaning meaning = meanings.get(idx);
        List<PracticeItem> items = response.practices();
        if (items == null || items.isEmpty()) {
            log.warn("{} empty_practices index={}", logPrefix, idx);
            return List.of();
        }

        List<Practice> practices = items.stream()
                .filter(item -> item.options() != null && !item.options().isEmpty() && item.correctAnswer() != null)
                .map(item -> Practice.builder()
                        .meaning(meaning)
                        .practiceType(getType())
                        .creationSource(getSource())
                        .question(item.question())
                        .options(item.options())
                        .correctAnswer(item.correctAnswer())
                        .build())
                .toList();

        log.info("{} word={} count={}", logPrefix, meaning.getWord().getText(), items.size());
        return practices;
    }
}
