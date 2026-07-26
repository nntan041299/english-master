package com.nntan041299.englishmasterservice.practice.dto;

public record PracticeStatsResponse(
        PracticeStatBucketResponse today,
        PracticeStatBucketResponse thisWeek,
        PracticeStatBucketResponse thisMonth) {}
