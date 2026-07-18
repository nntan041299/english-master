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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordTranslationPracticeGenerationService implements PracticeGenerationService {

    private static final int BATCH_SIZE = 1;
    private static final int PRACTICES_PER_MEANING = 10;

    private final MeaningRepository meaningRepository;
    private final PracticeRepository practiceRepository;
    private final AIService aiService;
    private final AiPromptManager aiPromptManager;

    @Override
    public PracticeType getType() {
        return PracticeType.SINGLE_CHOICE;
    }

    @Override
    @Transactional
    public void generate() {
        List<Meaning> meanings = meaningRepository.findMeaningsWithoutPractices(PageRequest.of(0, BATCH_SIZE));
        if (meanings.isEmpty()) {
            log.debug("word_translation_practice_generation no_meanings_to_process");
            return;
        }

        String meaningList = IntStream.range(0, meanings.size())
                .mapToObj(i -> {
                    Meaning m = meanings.get(i);
                    return "[%d] word=%s partOfSpeech=%s meaning=%s"
                            .formatted(i, m.getWord().getText(), m.getPartOfSpeech().name(), m.getMeaning());
                })
                .collect(Collectors.joining("; "));

        String prompt = aiPromptManager.get(AiPromptKey.WORD_TRANSLATION_PRACTICE_GENERATION)
                .formatted(PRACTICES_PER_MEANING, PRACTICES_PER_MEANING, meaningList);

        PracticeAiResponse[] responses;
        try {
            responses = aiService.generateContent(prompt, PracticeAiResponse[].class);
        } catch (Exception ex) {
            log.error("word_translation_practice_generation ai_error error={}", ex.getMessage(), ex);
            return;
        }

        List<Practice> practices = new ArrayList<>();

        Arrays.stream(responses).forEach(response -> {
            int idx = response.index();
            if (idx < 0 || idx >= meanings.size()) {
                log.warn("word_translation_practice_generation unknown_index index={}", idx);
                return;
            }

            Meaning meaning = meanings.get(idx);
            List<PracticeItem> items = response.practices();
            if (items == null || items.isEmpty()) {
                log.warn("word_translation_practice_generation empty_practices index={}", idx);
                return;
            }

            items.stream()
                    .filter(item -> item.options() != null && !item.options().isEmpty() && item.correctAnswer() != null)
                    .map(item -> Practice.builder()
                            .meaning(meaning)
                            .practiceType(getType())
                            .question(item.question())
                            .options(item.options())
                            .correctAnswer(item.correctAnswer())
                            .build())
                    .forEach(practices::add);

            log.info("word_translation_practice_generation word={} count={}", meaning.getWord().getText(), items.size());
        });

        practiceRepository.saveAll(practices);
        log.info("word_translation_practice_generation total_saved={}", practices.size());
    }
}
