package com.nntan041299.englishmasterservice.word.job;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.common.util.StringUtils;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.meaning.dto.MeaningAiResponse;
import com.nntan041299.englishmasterservice.meaning.entity.Category;
import com.nntan041299.englishmasterservice.meaning.entity.Meaning;
import com.nntan041299.englishmasterservice.meaning.entity.PartOfSpeech;
import com.nntan041299.englishmasterservice.word.entity.Word;
import com.nntan041299.englishmasterservice.meaning.repository.CategoryRepository;
import com.nntan041299.englishmasterservice.meaning.repository.MeaningRepository;
import com.nntan041299.englishmasterservice.word.repository.WordRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordEnrichmentJob {

    private static final int BATCH_SIZE = 5;

    private final WordRepository wordRepository;
    private final MeaningRepository meaningRepository;
    private final CategoryRepository categoryRepository;
    private final AIService aiService;
    private final AiPromptManager aiPromptManager;

    @Value("${job.word.enrichment.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${job.word.enrichment.cron}")
    @Transactional
    public void enrich() {
        if (!enabled) {
            return;
        }

        log.info("word_enrichment_job started");

        List<Word> words = wordRepository.findWordsWithoutMeanings(PageRequest.of(0, BATCH_SIZE));
        if (words.isEmpty()) {
            log.debug("word_enrichment_job no_words_to_enrich");
            return;
        }

        String wordList = words.stream().map(Word::getText).collect(Collectors.joining(", "));
        log.info("word_enrichment_job words={}", wordList);

        String partOfSpeechKeys = Arrays.stream(PartOfSpeech.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        MeaningAiResponse[] responseMeanings;
        try {
            responseMeanings = aiService.generateContent(
                    aiPromptManager.get(AiPromptKey.WORD_ENRICHMENT).formatted(partOfSpeechKeys, wordList),
                    MeaningAiResponse[].class);
        } catch (Exception ex) {
            log.error("word_enrichment_job ai_service_error error={}", ex.getMessage(), ex);
            return;
        }

        Map<String, Word> wordMap = words.stream().collect(Collectors.toMap(Word::getText, w -> w));

        List<MeaningAiResponse> validMeanings = Arrays.stream(responseMeanings)
                .map(dto -> new MeaningAiResponse(
                        dto.word().toLowerCase(), dto.partOfSpeech(), dto.meaning(), dto.ipa(), dto.categories()))
                .filter(dto -> {
                    boolean found = wordMap.containsKey(dto.word());
                    if (!found) log.warn("word_enrichment_job unknown_word word={}", dto.word());
                    return found;
                })
                .toList();

        List<Word> invalidMeanings = validMeanings.stream()
                .filter(dto -> PartOfSpeech.OTHER == parsePartOfSpeech(dto.partOfSpeech()))
                .map(dto -> wordMap.get(dto.word()))
                .distinct()
                .toList();

        if (!invalidMeanings.isEmpty()) {
            log.info("word_enrichment_job removing_invalid_words words={}", invalidMeanings.stream()
                    .map(Word::getText).collect(Collectors.joining(", ")));
            wordRepository.deleteAll(invalidMeanings);
        }

        Set<String> invalidWordTexts = invalidMeanings.stream().map(Word::getText).collect(Collectors.toSet());

        Map<String, Category> categoryCache = new HashMap<>();
        List<Meaning> meanings = validMeanings.stream()
                .filter(dto -> !invalidWordTexts.contains(dto.word()))
                .map(dto -> Meaning.builder()
                        .word(wordMap.get(dto.word()))
                        .partOfSpeech(parsePartOfSpeech(dto.partOfSpeech()))
                        .meaning(StringUtils.capitalizeFirst(dto.meaning()))
                        .ipa(dto.ipa())
                        .categories(resolveCategories(dto.categories(), categoryCache))
                        .build())
                .toList();

        meaningRepository.saveAll(meanings);
        log.info("word_enrichment_job enriched_words={}", meanings.stream()
                .map(m -> m.getWord().getText())
                .collect(Collectors.joining(", ")));
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
            log.error("word_enrichment_job unknown_part_of_speech raw={} error={}", raw, ex.getMessage());
            return PartOfSpeech.OTHER;
        }
    }
}
