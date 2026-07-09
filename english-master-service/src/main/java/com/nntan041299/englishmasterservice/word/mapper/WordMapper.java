package com.nntan041299.englishmasterservice.word.mapper;

import com.nntan041299.englishmasterservice.word.dto.WordMeaningResponse;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.entity.Meaning;
import com.nntan041299.englishmasterservice.word.entity.UserWord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WordMapper {

    @Mapping(target = "id", source = "word.id")
    @Mapping(target = "text", source = "word.text")
    @Mapping(target = "meanings", source = "word.meanings")
    WordResponse toResponse(UserWord userWord);

    WordMeaningResponse toMeaningResponse(Meaning meaning);
}
