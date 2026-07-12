package com.nntan041299.englishmasterservice.word.job;

import com.nntan041299.englishmasterservice.word.entity.*;
import com.nntan041299.englishmasterservice.word.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserWordRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeAssignmentJob {

    private final UserWordRepository userWordRepository;
    private final PracticeRepository practiceRepository;
    private final UserPracticeRepository userPracticeRepository;

    @Scheduled(cron = "${word.practice.assignment.cron}")
    @Transactional
    public void assign() {
        log.info("practice_assignment_job started");

        List<UserWord> unassigned = userWordRepository.findUnassigned();
        if (unassigned.isEmpty()) {
            log.debug("practice_assignment_job no_unassigned_user_words");
            return;
        }

        log.info("practice_assignment_job user_words_count={}", unassigned.size());

        for (UserWord userWord : unassigned) {
            List<Long> meaningIds = userWord.getWord().getMeanings().stream()
                    .map(Meaning::getId)
                    .toList();

            if (meaningIds.isEmpty()) {
                log.debug("practice_assignment_job skipped_no_meanings word={}", userWord.getWord().getText());
                continue;
            }

            List<Practice> practices = practiceRepository.findByMeaningIds(meaningIds);
            if (practices.isEmpty()) {
                log.debug("practice_assignment_job skipped_no_practices word={}", userWord.getWord().getText());
                continue;
            }

            // Pick one practice per meaning
            List<Practice> onePracticePerMeaning = practices.stream()
                    .collect(Collectors.toMap(
                            p -> p.getMeaning().getId(),
                            p -> p,
                            (existing, replacement) -> existing))
                    .values().stream().toList();

            List<UserPractice> assignments = onePracticePerMeaning.stream()
                    .map(practice -> UserPractice.builder()
                            .user(userWord.getUser())
                            .practice(practice)
                            .learningTracking(LearningTracking.TRACKING1)
                            .build())
                    .toList();

            userPracticeRepository.saveAll(assignments);
            userWord.setPracticesAssigned(true);
            userWordRepository.save(userWord);

            log.info("practice_assignment_job word={} user={} practices_assigned={}",
                    userWord.getWord().getText(), userWord.getUser().getId(), assignments.size());
        }

        log.info("practice_assignment_job completed user_words_processed={}", unassigned.size());
    }
}
