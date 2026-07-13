package com.nntan041299.englishmasterservice.practice.job;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse;
import com.nntan041299.englishmasterservice.practice.dto.PracticeAiResponse.PracticeItem;
import com.nntan041299.englishmasterservice.practice.entity.Practice;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordMeaningPracticeGenerationJob {

    private static final int BATCH_SIZE = 1;
    private static final int PRACTICES_PER_MEANING = 10;

    private final MeaningRepository meaningRepository;
    private final PracticeRepository practiceRepository;
    private final AIService aiService;
    private final AiPromptManager aiPromptManager;

    @Scheduled(cron = "${word.meaning.practice.cron}")
    @Transactional
    public void generate() {
        log.info("word_meaning_practice_generation_job started");

        List<Meaning> meanings = meaningRepository.findMeaningsWithoutPractices(PageRequest.of(0, BATCH_SIZE));
        if (meanings.isEmpty()) {
            log.debug("word_meaning_practice_generation_job no_meanings_to_process");
            return;
        }

        log.info("word_meaning_practice_generation_job meanings_count={}", meanings.size());

        String meaningList = IntStream.range(0, meanings.size())
                .mapToObj(i -> {
                    Meaning m = meanings.get(i);
                    return "[%d] word=%s partOfSpeech=%s meaning=%s"
                            .formatted(i, m.getWord().getText(), m.getPartOfSpeech().name(), m.getMeaning());
                })
                .collect(Collectors.joining("; "));

        String prompt = aiPromptManager.get(AiPromptKey.WORD_MEANING_PRACTICE_GENERATION)
                .formatted(PRACTICES_PER_MEANING, PRACTICES_PER_MEANING, meaningList);

        PracticeAiResponse[] responses;
        try {
            responses = aiService.generateContent(prompt, PracticeAiResponse[].class);
        } catch (Exception ex) {
            log.error("word_meaning_practice_generation_job ai_service_error error={}", ex.getMessage(), ex);
            return;
        }

        List<Practice> allPractices = new ArrayList<>();

        Arrays.stream(responses).forEach(response -> {
            int idx = response.index();
            if (idx < 0 || idx >= meanings.size()) {
                log.warn("word_meaning_practice_generation_job unknown_index index={}", idx);
                return;
            }

            Meaning meaning = meanings.get(idx);
            List<PracticeItem> items = response.practices();
            if (items == null || items.isEmpty()) {
                log.warn("word_meaning_practice_generation_job empty_practices index={}", idx);
                return;
            }

            items.stream()
                    .filter(item -> item.options() != null && !item.options().isEmpty() && item.correctAnswer() != null)
                    .map(item -> Practice.builder()
                            .meaning(meaning)
                            .question(item.question())
                            .options(item.options())
                            .correctAnswer(item.correctAnswer())
                            .build())
                    .forEach(allPractices::add);

            log.info("word_meaning_practice_generation_job word={} practices_generated={}", meaning.getWord().getText(), items.size());
        });

        practiceRepository.saveAll(allPractices);
        log.info("word_meaning_practice_generation_job total_practices_saved={}", allPractices.size());
    }
}
