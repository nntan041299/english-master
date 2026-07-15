package com.nntan041299.englishmasterservice.practice.job;

import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.practice.service.SingleChoicePracticeGenerationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordTranslationPracticeGenerationJob {

    private static final int MEANING_COUNT = 1;

    private final MeaningRepository meaningRepository;
    private final SingleChoicePracticeGenerationService practiceGenerationService;

    @Scheduled(cron = "${word.meaning.practice.cron}")
    public void run() {
        log.info("word_translation_practice_generation_job started");

        List<Meaning> meanings = meaningRepository.findMeaningsWithoutPractices(PageRequest.of(0, MEANING_COUNT));
        if (meanings.isEmpty()) {
            log.debug("word_translation_practice_generation_job no_meanings_to_process");
            return;
        }

        log.info("word_translation_practice_generation_job meanings_count={}", meanings.size());
        practiceGenerationService.generate(meanings);
    }
}
