package com.nntan041299.englishmasterservice.practice.service.impl;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.entity.Practice;
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
 * Base implementation for AI-driven practice generators, independent of the concrete
 * {@link com.nntan041299.englishmasterservice.practice.entity.PracticeType}. It owns the shared flow:
 * pick a batch of meanings that still lack practices for this source, build the prompt, call the AI
 * and persist the produced practices.
 *
 * <p>Subclasses supply the practice type, creation source and prompt, plus the response type to
 * deserialize into and how to turn one response element into {@link Practice} entities.
 *
 * @param <R> the AI response element type for this generator
 */
@Slf4j
public abstract class AbstractPracticeGenerationService<R> implements PracticeGenerationService {

    protected static final int BATCH_SIZE = 1;
    protected static final int PRACTICES_PER_MEANING = 10;

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

    /** The prompt template used to ask the AI for practices. */
    protected abstract AiPromptKey getPromptKey();

    /** Array type the AI response is deserialized into (e.g. {@code MyResponse[].class}). */
    protected abstract Class<R[]> getResponseType();

    /** Maps one AI response element into practices for the given meaning batch. */
    protected abstract List<Practice> mapResponse(R response, List<Meaning> meanings, String logPrefix);

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

        String prompt = buildPrompt(meanings);

        R[] responses;
        try {
            responses = aiService.generateContent(prompt, getResponseType());
        } catch (Exception ex) {
            log.error("{} ai_error error={}", logPrefix, ex.getMessage(), ex);
            return;
        }

        List<Practice> practices = Arrays.stream(responses)
                .flatMap(response -> mapResponse(response, meanings, logPrefix).stream())
                .collect(Collectors.toCollection(ArrayList::new));

        practiceRepository.saveAll(practices);
        log.info("{} total_saved={}", logPrefix, practices.size());
    }

    /**
     * Builds the prompt for a meaning batch. The default fills the shared template with the requested
     * practice count (twice) and the formatted meaning list; override for a template with other args.
     */
    protected String buildPrompt(List<Meaning> meanings) {
        return aiPromptManager.get(getPromptKey())
                .formatted(PRACTICES_PER_MEANING, PRACTICES_PER_MEANING, formatMeanings(meanings));
    }

    /** Renders the meaning batch as an indexed, prompt-friendly list. */
    protected String formatMeanings(List<Meaning> meanings) {
        return IntStream.range(0, meanings.size())
                .mapToObj(i -> {
                    Meaning m = meanings.get(i);
                    return "[%d] word=%s partOfSpeech=%s meaning=%s"
                            .formatted(i, m.getWord().getText(), m.getPartOfSpeech().name(), m.getMeaning());
                })
                .collect(Collectors.joining("; "));
    }
}
