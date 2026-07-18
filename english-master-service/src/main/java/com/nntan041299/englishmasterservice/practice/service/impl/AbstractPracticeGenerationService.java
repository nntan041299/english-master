package com.nntan041299.englishmasterservice.practice.service.impl;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse.PracticeItem;
import com.nntan041299.englishmasterservice.practice.entity.Practice;
import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.practice.service.PracticeGenerationService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base implementation for single-choice practice generators. Subclasses only declare their
 * {@link com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource} and the AI
 * prompt to use; the shared batching, AI call, response mapping and persistence live here.
 */
@Slf4j
public abstract class AbstractPracticeGenerationService implements PracticeGenerationService {

    private static final int BATCH_SIZE = 1;
    private static final int PRACTICES_PER_MEANING = 10;

    private final MeaningRepository meaningRepository;
    private final PracticeRepository practiceRepository;
    private final AIService aiService;
    private final AiPromptManager aiPromptManager;

    protected AbstractPracticeGenerationService(
            MeaningRepository meaningRepository,
            PracticeRepository practiceRepository,
            AIService aiService,
            AiPromptManager aiPromptManager) {
        this.meaningRepository = meaningRepository;
        this.practiceRepository = practiceRepository;
        this.aiService = aiService;
        this.aiPromptManager = aiPromptManager;
    }

    /** The prompt template used to ask the AI for practices; must contain two {@code %d} slots and one {@code %s}. */
    protected abstract AiPromptKey getPromptKey();

    @Override
    public PracticeType getType() {
        return PracticeType.SINGLE_CHOICE;
    }

    @Override
    @Transactional
    public void generate() {
        String logPrefix = getSource().name().toLowerCase() + "_practice_generation";

        List<Meaning> meanings =
                meaningRepository.findMeaningsWithoutPracticesForSource(getSource(), PageRequest.of(0, BATCH_SIZE));
        if (meanings.isEmpty()) {
            log.debug("{} no_meanings_to_process", logPrefix);
            return;
        }

        String meaningList = IntStream.range(0, meanings.size())
                .mapToObj(i -> {
                    Meaning m = meanings.get(i);
                    return "[%d] word=%s partOfSpeech=%s meaning=%s"
                            .formatted(i, m.getWord().getText(), m.getPartOfSpeech().name(), m.getMeaning());
                })
                .collect(Collectors.joining("; "));

        String prompt = aiPromptManager.get(getPromptKey())
                .formatted(PRACTICES_PER_MEANING, PRACTICES_PER_MEANING, meaningList);

        PracticeAiResponse[] responses;
        try {
            responses = aiService.generateContent(prompt, PracticeAiResponse[].class);
        } catch (Exception ex) {
            log.error("{} ai_error error={}", logPrefix, ex.getMessage(), ex);
            return;
        }

        List<Practice> practices = new ArrayList<>();

        Arrays.stream(responses).forEach(response -> {
            int idx = response.index();
            if (idx < 0 || idx >= meanings.size()) {
                log.warn("{} unknown_index index={}", logPrefix, idx);
                return;
            }

            Meaning meaning = meanings.get(idx);
            List<PracticeItem> items = response.practices();
            if (items == null || items.isEmpty()) {
                log.warn("{} empty_practices index={}", logPrefix, idx);
                return;
            }

            items.stream()
                    .filter(item -> item.options() != null && !item.options().isEmpty() && item.correctAnswer() != null)
                    .map(item -> Practice.builder()
                            .meaning(meaning)
                            .practiceType(getType())
                            .creationSource(getSource())
                            .question(item.question())
                            .options(item.options())
                            .correctAnswer(item.correctAnswer())
                            .build())
                    .forEach(practices::add);

            log.info("{} word={} count={}", logPrefix, meaning.getWord().getText(), items.size());
        });

        practiceRepository.saveAll(practices);
        log.info("{} total_saved={}", logPrefix, practices.size());
    }
}
