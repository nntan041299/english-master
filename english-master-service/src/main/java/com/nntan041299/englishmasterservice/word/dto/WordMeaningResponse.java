package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.word.entity.PartOfSpeech;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordMeaningResponse {

    private Long id;
    private PartOfSpeech partOfSpeech;
    private String meaning;
}
