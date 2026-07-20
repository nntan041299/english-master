package com.nntan041299.englishmasterservice.practice.job;

import com.nntan041299.englishmasterservice.auth.repository.UserRepository;
import com.nntan041299.englishmasterservice.practice.dto.MissingPracticeAssignment;
import com.nntan041299.englishmasterservice.practice.entity.LearningTracking;
import com.nntan041299.englishmasterservice.practice.entity.PracticeCreationSource;
import com.nntan041299.englishmasterservice.practice.entity.UserPractice;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.practice.repository.UserPracticeRepository;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PracticeAssignmentJob {

    private final UserRepository userRepository;
    private final PracticeRepository practiceRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final Random random = new Random();

    @Value("${word.practice.assignment.enabled}")
    private boolean enabled;

    /**
     * Ensures every user has one practice per creation source for each meaning of the words they
     * saved. The missing groups are computed in SQL; for each missing {@code (user, meaning, source)}
     * group a random candidate practice is assigned.
     */
    @Scheduled(cron = "${word.practice.assignment.cron}")
    @Transactional
    public void assign() {
        if (!enabled) {
            return;
        }

        log.info("practice_assignment_job started");

        List<MissingPracticeAssignment> missing = userPracticeRepository.findMissingAssignments();
        if (missing.isEmpty()) {
            log.debug("practice_assignment_job no_missing_assignments");
            return;
        }

        Map<AssignmentGroup, List<Long>> practiceIdsByGroup = missing.stream()
                .collect(Collectors.groupingBy(
                        m -> new AssignmentGroup(m.userId(), m.meaningId(), m.creationSource()),
                        Collectors.mapping(MissingPracticeAssignment::practiceId, Collectors.toList())));

        List<UserPractice> assignments = practiceIdsByGroup.entrySet().stream()
                .map(entry -> {
                    AssignmentGroup group = entry.getKey();
                    List<Long> practiceIds = entry.getValue();
                    Long practiceId = practiceIds.get(random.nextInt(practiceIds.size()));
                    return UserPractice.builder()
                            .user(userRepository.getReferenceById(group.userId()))
                            .practice(practiceRepository.getReferenceById(practiceId))
                            .learningTracking(LearningTracking.TRACKING1)
                            .build();
                })
                .toList();

        userPracticeRepository.saveAll(assignments);
        log.info("practice_assignment_job completed assignments_created={}", assignments.size());
    }

    /** A distinct meaning + creation source a single user still needs a practice for. */
    private record AssignmentGroup(Long userId, Long meaningId, PracticeCreationSource creationSource) {}
}
