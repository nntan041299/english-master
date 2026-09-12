package com.nntan041299.englishmasterservice.word.mapper;

import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.word.dto.WordMeaningResponse;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.entity.LearningLevel;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.word.entity.Word;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = StringUtils.class)
public interface WordMapper {

    @Mapping(target = "text", expression = "java(StringUtils.capitalizeFirst(word.getText()))")
    @Mapping(target = "learningLevel", source = "learningLevel")
    WordResponse toResponse(Word word, LearningLevel learningLevel);

    @Mapping(target = "meaning", expression = "java(StringUtils.capitalizeFirst(meaning.getMeaning()))")
    @Mapping(target = "ipa", source = "ipa")
    @Mapping(target = "categories", expression = "java(meaning.getCategories().stream()"
            + ".map(com.nntan041299.englishmasterservice.meaning.entity.Category::getName).toList())")
    WordMeaningResponse toMeaningResponse(Meaning meaning);
}
