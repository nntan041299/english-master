package com.nntan041299.englishmasterservice.word.mapper;

import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.word.dto.WordMeaningResponse;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.entity.Meaning;
import com.nntan041299.englishmasterservice.word.entity.UserWord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = StringUtils.class)
public interface WordMapper {

    @Mapping(target = "id", source = "word.id")
    @Mapping(target = "text", expression = "java(StringUtils.capitalizeFirst(userWord.getWord().getText()))")
    @Mapping(target = "meanings", source = "word.meanings")
    WordResponse toResponse(UserWord userWord);

    @Mapping(target = "meaning", expression = "java(StringUtils.capitalizeFirst(meaning.getMeaning()))")
    WordMeaningResponse toMeaningResponse(Meaning meaning);
}
