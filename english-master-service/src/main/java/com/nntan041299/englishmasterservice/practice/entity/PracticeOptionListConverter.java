package com.nntan041299.englishmasterservice.practice.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Converter
public class PracticeOptionListConverter implements AttributeConverter<List<PracticeOption>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<PracticeOption>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<PracticeOption> options) {
        if (options == null || options.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize options to JSON", e);
        }
    }

    @Override
    public List<PracticeOption> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize options from JSON: " + json, e);
        }
    }
}
