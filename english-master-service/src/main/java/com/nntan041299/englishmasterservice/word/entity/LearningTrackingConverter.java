package com.nntan041299.englishmasterservice.word.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

@Converter(autoApply = true)
public class LearningTrackingConverter implements AttributeConverter<LearningTracking, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LearningTracking attribute) {
        return attribute == null ? null : attribute.getPoint();
    }

    @Override
    public LearningTracking convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return Arrays.stream(LearningTracking.values())
                .filter(t -> t.getPoint() == dbData)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown LearningTracking point: " + dbData));
    }
}
