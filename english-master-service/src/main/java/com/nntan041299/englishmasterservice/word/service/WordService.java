package com.nntan041299.englishmasterservice.word.service;

import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.word.dto.SaveWordRequest;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.entity.LearningLevel;
import com.nntan041299.englishmasterservice.word.entity.UserWord;
import com.nntan041299.englishmasterservice.word.entity.Word;
import com.nntan041299.englishmasterservice.word.mapper.WordMapper;
import com.nntan041299.englishmasterservice.word.repository.UserPracticeRepository;
import com.nntan041299.englishmasterservice.word.repository.UserWordRepository;
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
    private final UserWordRepository userWordRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final WordMapper wordMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public WordResponse saveWord(SaveWordRequest request) {
        String normalizedText = request.getText().trim().toLowerCase();
        if (normalizedText.isEmpty()) {
            throw new IllegalArgumentException("Word text is required");
        }

        User currentUser = currentUserProvider.getCurrentUser();

        Word word = wordRepository.findByText(normalizedText)
                .orElseGet(() -> wordRepository.save(
                        Word.builder()
                                .text(normalizedText)
                                .build()
                ));

        UserWord userWord = userWordRepository.findByUserIdAndWordId(currentUser.getId(), word.getId())
                .orElseGet(() -> userWordRepository.save(
                        UserWord.builder()
                                .user(currentUser)
                                .word(word)
                                .build()
                ));

        return wordMapper.toResponse(userWord, LearningLevel.NEW);
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> searchWords(String keyword, Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();

        Page<UserWord> page = (keyword == null || keyword.isBlank())
                ? userWordRepository.findByUserId(currentUser.getId(), pageable)
                : userWordRepository.findByUserIdAndWordTextContaining(currentUser.getId(), keyword.trim().toLowerCase(), pageable);

        List<Long> wordIds = page.map(uw -> uw.getWord().getId()).toList();

        // Fetch meanings in a single query for the current page only
        List<Long> ids = page.map(UserWord::getId).toList();
        Map<Long, UserWord> withMeanings = userWordRepository.findByIdsWithMeanings(ids)
                .stream()
                .collect(Collectors.toMap(UserWord::getId, Function.identity()));

        Map<Long, Double> avgPointByWordId = userPracticeRepository
                .findAvgPointByUserIdAndWordIds(currentUser.getId(), wordIds)
                .stream()
                .collect(Collectors.toMap(WordAvgPoint::getWordId, WordAvgPoint::getAvgPoint));

        return page.map(uw -> {
            UserWord resolved = withMeanings.getOrDefault(uw.getId(), uw);
            double avgPoint = avgPointByWordId.getOrDefault(resolved.getWord().getId(), 0.0);
            return wordMapper.toResponse(resolved, LearningLevel.fromAveragePoint(avgPoint));
        });
    }
}
