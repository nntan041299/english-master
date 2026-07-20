package com.nntan041299.englishmasterservice.listening.job;

import com.nntan041299.englishmasterservice.listening.service.ListeningPracticeGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListeningPracticeGenerationJob {

    private final ListeningPracticeGenerationService listeningPracticeGenerationService;

    @Value("${listening.practice.generation.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${listening.practice.generation.cron}")
    public void run() {
        if (!enabled) {
            return;
        }
        log.info("listening_practice_generation_job started");
        listeningPracticeGenerationService.generate();
    }
}
