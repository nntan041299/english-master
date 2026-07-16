package com.nntan041299.englishmasterservice.meaning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeaningAiResponse(String word, String partOfSpeech, String meaning, String ipa) {}
