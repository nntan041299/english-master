package com.nntan041299.englishmasterservice.word.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Aggregate learning level for a word, derived from the average point
 * of all UserPractice tracking levels across the word's meanings.
 *
 * <ul>
 *     <li>{@link #NEW}         - avg point 0   (no practice yet)</li>
 *     <li>{@link #LEVEL_1}     - avg point 1</li>
 *     <li>{@link #LEVEL_2}     - avg point 2</li>
 *     <li>{@link #LEVEL_3}     - avg point 3</li>
 *     <li>{@link #LEVEL_4}     - avg point 4</li>
 *     <li>{@link #LEVEL_5}     - avg point 5</li>
 *     <li>{@link #MASTERED}    - avg point 6</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public enum LearningLevel {
    NEW(0),
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5),
    MASTERED(6);

    private final int point;

    public static LearningLevel fromAveragePoint(double avgPoint) {
        LearningLevel result = NEW;
        for (LearningLevel level : values()) {
            if (avgPoint >= level.point) {
                result = level;
            }
        }
        return result;
    }
}
