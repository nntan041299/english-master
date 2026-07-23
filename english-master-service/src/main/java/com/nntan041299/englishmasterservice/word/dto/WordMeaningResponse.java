package com.nntan041299.englishmasterservice.word.dto;

import com.nntan041299.englishmasterservice.meaning.entity.PartOfSpeech;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordMeaningResponse {

    private Long id;
    private PartOfSpeech partOfSpeech;
    private String meaning;
    private String ipa;
    private List<String> categories;
}
