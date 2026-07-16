package com.nntan041299.englishmasterservice.practice.checker;

import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SingleChoiceAnswerChecker implements AnswerChecker {

    @Override
    public PracticeType getType() {
        return PracticeType.SINGLE_CHOICE;
    }

    @Override
    public boolean isCorrect(List<String> correctAnswer, List<String> selectedOptionIds) {
        if (correctAnswer == null || correctAnswer.size() != 1) return false;
        if (selectedOptionIds == null || selectedOptionIds.size() != 1) return false;
        return correctAnswer.getFirst().equals(selectedOptionIds.getFirst());
    }
}
