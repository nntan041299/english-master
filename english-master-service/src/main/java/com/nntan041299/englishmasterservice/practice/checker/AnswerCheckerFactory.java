package com.nntan041299.englishmasterservice.practice.checker;

import com.nntan041299.englishmasterservice.practice.entity.PracticeType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnswerCheckerFactory {

    private final Map<PracticeType, AnswerChecker> checkers;

    public AnswerCheckerFactory(List<AnswerChecker> checkers) {
        this.checkers = checkers.stream()
                .collect(Collectors.toMap(AnswerChecker::getType, Function.identity()));
    }

    public AnswerChecker get(PracticeType type) {
        AnswerChecker checker = checkers.get(type);
        if (checker == null) {
            throw new IllegalArgumentException("No AnswerChecker registered for type: " + type);
        }
        return checker;
    }
}
