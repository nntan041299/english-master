package com.nntan041299.englishmasterservice.word.service;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.word.dto.AnswerPracticeRequest;
import com.nntan041299.englishmasterservice.word.dto.AnswerPracticeResponse;
import com.nntan041299.englishmasterservice.word.dto.PracticeResponse;
import com.nntan041299.englishmasterservice.word.entity.*;
import com.nntan041299.englishmasterservice.word.repository.PracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeResultRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

        return userPracticeRepository.findDueByUserId(
                        userId,
                        now.minusDays(LearningLevel.TRACKING1.getDaysInterval()),
                        now.minusDays(LearningLevel.TRACKING2.getDaysInterval()),
                        now.minusDays(LearningLevel.TRACKING3.getDaysInterval()),
                        now.minusDays(LearningLevel.TRACKING4.getDaysInterval()),
                        now.minusDays(LearningLevel.TRACKING5.getDaysInterval()))
                .stream()
                .map(userPractice -> {
                    Practice practice = userPractice.getPractice();
                    Meaning meaning = practice.getMeaning();
                    return new PracticeResponse(
                            meaning.getWord().getId(),
                            meaning.getWord().getText(),
                            userPractice.getLevel(),
                            practice.getId(),
                            meaning.getPartOfSpeech().name(),
                            meaning.getMeaning(),
                            practice.getOption1(),
                            practice.getOption2(),
                            practice.getOption3(),
                            practice.getOption4(),
                            practice.getCorrectAnswer());
                })
                .toList();
    }

    @Transactional
    public AnswerPracticeResponse answerPractice(AnswerPracticeRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new EntityNotFoundException("Practice not found: " + request.practiceId()));

        boolean correct = practice.getCorrectAnswer() == request.selectedOption();

        userPracticeResultRepository.save(UserPracticeResult.builder()
                .user(currentUser)
                .practice(practice)
                .answeredOption(request.selectedOption())
                .correct(correct)
                .build());

        UserPractice userPractice = userPracticeRepository
                .findByUserIdAndPracticeId(currentUser.getId(), request.practiceId())
                .orElseThrow(() -> new EntityNotFoundException("UserPractice not found for practiceId: " + request.practiceId()));

        LearningLevel newLevel = correct ? nextLevel(userPractice.getLevel()) : LearningLevel.TRACKING1;
        userPractice.setLevel(newLevel);
        userPractice.setLastPracticedAt(LocalDateTime.now());
        userPracticeRepository.save(userPractice);

        return new AnswerPracticeResponse(correct, practice.getCorrectAnswer(), newLevel);
    }

    private LearningLevel nextLevel(LearningLevel current) {
        LearningLevel[] levels = LearningLevel.values();
        int next = current.ordinal() + 1;
        return next < levels.length ? levels[next] : levels[levels.length - 1];
    }
}
