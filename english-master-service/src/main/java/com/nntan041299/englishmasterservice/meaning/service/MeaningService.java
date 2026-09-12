package com.nntan041299.englishmasterservice.meaning.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.meaning.dto.MeaningAiResponse;
import com.nntan041299.englishmasterservice.meaning.entity.Category;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.entity.PartOfSpeech;
import com.nntan041299.englishmasterservice.meaning.exception.InvalidWordException;
import com.nntan041299.englishmasterservice.meaning.repository.CategoryRepository;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.word.entity.Word;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Enriches a single {@link Word} with meanings on demand, right after the user adds it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeaningService {

    private final MeaningRepository meaningRepository;
    private final CategoryRepository categoryRepository;
    private final AIService aiService;
    private final AiPromptManager aiPromptManager;

    public void enrich(Word word) {
        String partOfSpeechKeys =
                Arrays.stream(PartOfSpeech.values()).map(Enum::name).collect(Collectors.joining(", "));

        MeaningAiResponse[] responseMeanings;
        try {
            responseMeanings = aiService.generateContent(
                    aiPromptManager.get(AiPromptKey.WORD_ENRICHMENT).formatted(partOfSpeechKeys, word.getText()),
                    MeaningAiResponse[].class);
        } catch (Exception ex) {
            log.error("word_enrichment ai_service_error word={} error={}", word.getText(), ex.getMessage(), ex);
            throw new IllegalStateException("Failed to generate meaning for word: " + word.getText(), ex);
        }

        Map<String, Category> categoryCache = new HashMap<>();
        List<Meaning> meanings = Arrays.stream(responseMeanings)
                .filter(dto -> word.getText().equalsIgnoreCase(dto.word()))
                .filter(dto -> {
                    boolean valid = PartOfSpeech.OTHER != parsePartOfSpeech(dto.partOfSpeech());
                    if (!valid) log.warn("word_enrichment not_a_real_word word={}", word.getText());
                    return valid;
                })
                .map(dto -> Meaning.builder()
                        .word(word)
                        .partOfSpeech(parsePartOfSpeech(dto.partOfSpeech()))
                        .meaning(StringUtils.capitalizeFirst(dto.meaning()))
                        .ipa(dto.ipa())
                        .categories(resolveCategories(dto.categories(), categoryCache))
                        .build())
                .toList();

        if (meanings.isEmpty()) {
            log.warn("word_enrichment no_meaning_generated word={}", word.getText());
            throw new InvalidWordException(
                    "\"" + word.getText() + "\" doesn't look like a valid English word. Please check the spelling and try again.");
        }

        meaningRepository.saveAll(meanings);
        log.info("word_enrichment enriched word={}", word.getText());
    }

    private List<Category> resolveCategories(List<String> rawCategories, Map<String, Category> categoryCache) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return List.of();
        }
        return rawCategories.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .map(name -> categoryCache.computeIfAbsent(name, this::findOrCreateCategory))
                .toList();
    }

    private Category findOrCreateCategory(String name) {
        return categoryRepository
                .findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
    }

    private PartOfSpeech parsePartOfSpeech(String raw) {
        if (raw == null) return PartOfSpeech.OTHER;
        try {
            return PartOfSpeech.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("word_enrichment unknown_part_of_speech raw={} error={}", raw, ex.getMessage());
            return PartOfSpeech.OTHER;
        }
    }
}
