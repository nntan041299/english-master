package com.nntan041299.englishmasterservice.practice.dto;

import com.nntan041299.englishmasterservice.practice.repository.PracticeStatBucket;

public record PracticeStatBucketResponse(long correct, long incorrect, long total) {

    public static PracticeStatBucketResponse from(PracticeStatBucket bucket) {
        long correct = bucket.getCorrectCount();
        long total = bucket.getTotalCount();
        return new PracticeStatBucketResponse(correct, total - correct, total);
    }
}
