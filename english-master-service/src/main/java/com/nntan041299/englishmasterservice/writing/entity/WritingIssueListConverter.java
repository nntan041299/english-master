package com.nntan041299.englishmasterservice.writing.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Converter
public class WritingIssueListConverter implements AttributeConverter<List<WritingIssue>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<WritingIssue>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<WritingIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(issues);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize writing issues to JSON", e);
        }
    }

    @Override
    public List<WritingIssue> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize writing issues from JSON: " + json, e);
        }
    }
}
