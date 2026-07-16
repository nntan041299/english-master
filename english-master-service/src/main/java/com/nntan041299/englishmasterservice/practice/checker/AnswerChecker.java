package com.nntan041299.englishmasterservice.practice.checker;

import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import java.util.List;

public interface AnswerChecker {

    PracticeType getType();

    boolean isCorrect(List<String> correctAnswer, List<String> selectedOptionIds);
}
