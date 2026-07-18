package com.nntan041299.englishmasterservice.practice.job;

import com.nntan041299.englishmasterservice.practice.service.PracticeGenerationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeGenerationJob {

    private final List<PracticeGenerationService> practiceGenerationServices;

    @Scheduled(cron = "${word.meaning.practice.cron}")
    public void run() {
        log.info("practice_generation_job started services_count={}", practiceGenerationServices.size());
        practiceGenerationServices.forEach(service -> {
            log.info("practice_generation_job running type={}", service.getType());
            service.generate();
        });
    }
}
