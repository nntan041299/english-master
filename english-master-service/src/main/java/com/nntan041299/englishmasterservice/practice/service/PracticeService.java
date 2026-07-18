package com.nntan041299.englishmasterservice.practice.service;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.practice.checker.AnswerCheckerFactory;
import com.nntan041299.englishmasterservice.practice.dto.AnswerPracticeRequest;
import com.nntan041299.englishmasterservice.practice.dto.AnswerPracticeResponse;
import com.nntan041299.englishmasterservice.practice.dto.PracticeResponse;
import com.nntan041299.englishmasterservice.practice.entity.LearningTracking;
import com.nntan041299.englishmasterservice.practice.entity.Practice;
import com.nntan041299.englishmasterservice.practice.entity.UserPractice;
import com.nntan041299.englishmasterservice.practice.entity.UserPracticeResult;
import com.nntan041299.englishmasterservice.practice.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.practice.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.practice.repository.UserPracticeResultRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final UserPracticeRepository userPracticeRepository;
    private final PracticeRepository practiceRepository;
    private final UserPracticeResultRepository userPracticeResultRepository;
    private final AnswerCheckerFactory answerCheckerFactory;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<PracticeResponse> getDuePractices() {
        Long userId = currentUserProvider.getCurrentUser().getId();
        LocalDateTime now = LocalDateTime.now();

        return userPracticeRepository.findActiveByUserId(userId, LearningTracking.FINISH)
                .stream()
                .filter(up -> up.getLastPracticedAt() == null ||
                        up.getLastPracticedAt().isBefore(now.minusDays(up.getLearningTracking().getDaysInterval())))
                .map(userPractice -> {
                    Practice practice = userPractice.getPractice();
                    Meaning meaning = practice.getMeaning();
                    return new PracticeResponse(
                            meaning.getWord().getId(),
                            StringUtils.capitalizeFirst(meaning.getWord().getText()),
                            userPractice.getLearningTracking(),
                            practice.getId(),
                            meaning.getPartOfSpeech().name(),
                            StringUtils.capitalizeFirst(meaning.getMeaning()),
                            practice.getPracticeType(),
                            practice.getCreationSource(),
                            practice.getQuestion(),
                            practice.getOptions(),
                            practice.getCorrectAnswer());
                })
                .toList();
    }

    @Transactional
    public AnswerPracticeResponse answerPractice(AnswerPracticeRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new EntityNotFoundException("Practice not found: " + request.practiceId()));

        UserPractice userPractice = userPracticeRepository
                .findByUserIdAndPracticeId(currentUser.getId(), practice.getId())
                .orElseThrow(() -> new EntityNotFoundException("UserPractice not found for user " + currentUser.getId() + " and practice " + practice.getId()));

        boolean correct = answerCheckerFactory.get(practice.getPracticeType())
                .isCorrect(practice.getCorrectAnswer(), request.selectedOptionIds());

        userPracticeResultRepository.save(UserPracticeResult.builder()
                .user(currentUser)
                .practice(practice)
                .answeredOptionIds(request.selectedOptionIds())
                .correct(correct)
                .build());

        LearningTracking newTracking = userPractice.getLearningTracking();
        if (correct) {
            LearningTracking[] values = LearningTracking.values();
            int nextOrdinal = newTracking.ordinal() + 1;
            if (nextOrdinal < values.length) {
                newTracking = values[nextOrdinal];
            }
        }

        userPractice.setLearningTracking(newTracking);
        userPractice.setLastPracticedAt(LocalDateTime.now());
        userPracticeRepository.save(userPractice);

        return new AnswerPracticeResponse(correct, practice.getCorrectAnswer(), newTracking);
    }
}
