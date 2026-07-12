package com.nntan041299.englishmasterservice.word.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {

    private long totalWords;
    private long newWords;
    private long learningWords;
    private long familiarWords;
    private long masteredWords;
    private long practicesDone;
}
