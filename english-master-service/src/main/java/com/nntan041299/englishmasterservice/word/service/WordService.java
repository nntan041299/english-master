package com.nntan041299.englishmasterservice.word.service;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.meaning.service.MeaningService;
import com.nntan041299.englishmasterservice.word.dto.DashboardResponse;
import com.nntan041299.englishmasterservice.word.dto.SaveWordRequest;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.entity.LearningLevel;
import com.nntan041299.englishmasterservice.word.entity.Word;
import com.nntan041299.englishmasterservice.word.mapper.WordMapper;
import com.nntan041299.englishmasterservice.practice.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.practice.repository.UserPracticeResultRepository;
import com.nntan041299.englishmasterservice.word.repository.WordRepository;
import com.nntan041299.englishmasterservice.word.repository.WordAvgPoint;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final UserPracticeResultRepository userPracticeResultRepository;
    private final WordMapper wordMapper;
    private final CurrentUserProvider currentUserProvider;
    private final MeaningService meaningService;

    @Transactional
    public WordResponse saveWord(SaveWordRequest request) {
        String normalizedText = request.getText().trim().toLowerCase();
        if (normalizedText.isEmpty()) {
            throw new IllegalArgumentException("Word text is required");
        }

        User currentUser = currentUserProvider.getCurrentUser();

        Word word = wordRepository.findByUserIdAndText(currentUser.getId(), normalizedText)
                .orElseGet(() -> wordRepository.save(
                        Word.builder()
                                .user(currentUser)
                                .text(normalizedText)
                                .build()
                ));

        if (word.getMeanings().isEmpty()) {
            meaningService.enrich(word);
        }

        return wordMapper.toResponse(word, LearningLevel.NEW);
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> searchWords(String keyword, Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();

        Page<Word> page = (keyword == null || keyword.isBlank())
                ? wordRepository.findByUserId(currentUser.getId(), pageable)
                : wordRepository.findByUserIdAndTextContaining(currentUser.getId(), keyword.trim().toLowerCase(), pageable);

        List<Long> wordIds = page.map(Word::getId).toList();

        // Fetch meanings in a single query for the current page only
        Map<Long, Word> withMeanings = wordRepository.findByIdsWithMeanings(wordIds)
                .stream()
                .collect(Collectors.toMap(Word::getId, Function.identity()));

        Map<Long, Double> avgPointByWordId = userPracticeRepository
                .findAvgPointByUserIdAndWordIds(currentUser.getId(), wordIds)
                .stream()
                .collect(Collectors.toMap(WordAvgPoint::getWordId, WordAvgPoint::getAvgPoint));

        return page.map(w -> {
            Word resolved = withMeanings.getOrDefault(w.getId(), w);
            double avgPoint = avgPointByWordId.getOrDefault(resolved.getId(), 0.0);
            return wordMapper.toResponse(resolved, LearningLevel.fromAveragePoint(avgPoint));
        });
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        User currentUser = currentUserProvider.getCurrentUser();
        Long userId = currentUser.getId();

        long totalWords = wordRepository.countByUserId(userId);

        Map<Long, Double> avgPointByWordId = userPracticeRepository.findAvgPointByUserId(userId)
                .stream()
                .collect(Collectors.toMap(WordAvgPoint::getWordId, WordAvgPoint::getAvgPoint));

        long newWords = 0, learningWords = 0, familiarWords = 0, masteredWords = 0;

        for (double avgPoint : avgPointByWordId.values()) {
            LearningLevel level = LearningLevel.fromAveragePoint(avgPoint);
            switch (level) {
                case NEW                      -> newWords++;
                case LEVEL_1, LEVEL_2, LEVEL_3 -> learningWords++;
                case LEVEL_4, LEVEL_5         -> familiarWords++;
                case MASTERED                 -> masteredWords++;
            }
        }

        // Words with no practice entry yet are all NEW
        long wordsWithPractice = avgPointByWordId.size();
        newWords += (totalWords - wordsWithPractice);

        long practicesDone = userPracticeResultRepository.countByUserId(userId);

        return DashboardResponse.builder()
                .totalWords(totalWords)
                .newWords(newWords)
                .learningWords(learningWords)
                .familiarWords(familiarWords)
                .masteredWords(masteredWords)
                .practicesDone(practicesDone)
                .build();
    }
}
