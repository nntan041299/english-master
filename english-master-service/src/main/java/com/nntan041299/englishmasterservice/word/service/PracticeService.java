package com.nntan041299.englishmasterservice.word.service;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.word.dto.AnswerPracticeRequest;
import com.nntan041299.englishmasterservice.word.dto.AnswerPracticeResponse;
import com.nntan041299.englishmasterservice.word.dto.PracticeResponse;
import com.nntan041299.englishmasterservice.word.entity.*;
import com.nntan041299.englishmasterservice.word.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeResultRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final UserPracticeRepository userPracticeRepository;
    private final PracticeRepository practiceRepository;
    private final UserPracticeResultRepository userPracticeResultRepository;
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

        boolean correct = practice.getCorrectAnswer().equals(request.selectedOptionIds());

        userPracticeResultRepository.save(UserPracticeResult.builder()
                .user(currentUser)
                .practice(practice)
                .answeredOptionIds(request.selectedOptionIds())
                .correct(correct)
                .build());

        UserPractice userPractice = userPracticeRepository
                .findByUserIdAndPracticeId(currentUser.getId(), request.practiceId())
                .orElseThrow(() -> new EntityNotFoundException("UserPractice not found for practiceId: " + request.practiceId()));

        LearningTracking newLearningTracking = correct ? nextLevel(userPractice.getLearningTracking()) : LearningTracking.TRACKING1;
        userPractice.setLearningTracking(newLearningTracking);
        userPractice.setLastPracticedAt(LocalDateTime.now());
        userPracticeRepository.save(userPractice);

        return new AnswerPracticeResponse(correct, practice.getCorrectAnswer(), newLearningTracking);
    }

    private LearningTracking nextLevel(LearningTracking current) {
        LearningTracking[] levels = LearningTracking.values();
        int next = current.ordinal() + 1;
        return next < levels.length ? levels[next] : levels[levels.length - 1];
    }
}
