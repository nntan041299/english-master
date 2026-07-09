package com.nntan041299.englishmasterservice.word.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WordResponse {

    private Long id;
    private String text;
    private List<WordMeaningResponse> meanings;
}
