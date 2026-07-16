package com.nntan041299.englishmasterservice.practice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A user's mastery level for a given word, progressing as they practice it.
 * Each level defines how long the user should wait before the word needs
 * to be practiced again:
 *
 * <ul>
 *     <li>{@link #TRACKING1} - review after 1 day</li>
 *     <li>{@link #TRACKING2} - review after 3 days</li>
 *     <li>{@link #TRACKING3} - review after 1 week</li>
 *     <li>{@link #TRACKING4} - review after 1 month</li>
 *     <li>{@link #TRACKING5} - review after 4 months</li>
 *     <li>{@link #FINISH} - finish</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public enum LearningTracking {
    TRACKING1(1, 1),
    TRACKING2(3, 2),
    TRACKING3(7, 3),
    TRACKING4(30, 4),
    TRACKING5(120, 5),
    FINISH(0, 6);

    private final int daysInterval;
    private final int point;
}
