package com.nntan041299.englishmasterservice.word.controller;

import com.nntan041299.englishmasterservice.common.dto.PageResponse;
import com.nntan041299.englishmasterservice.word.dto.DashboardResponse;
import com.nntan041299.englishmasterservice.word.dto.SaveWordRequest;
import com.nntan041299.englishmasterservice.word.dto.UpdateWordRequest;
import com.nntan041299.englishmasterservice.word.dto.WordResponse;
import com.nntan041299.englishmasterservice.word.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @PostMapping
    public ResponseEntity<WordResponse> saveWord(@Valid @RequestBody SaveWordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wordService.saveWord(request));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(wordService.getDashboard());
    }

    @GetMapping
    public ResponseEntity<PageResponse<WordResponse>> searchWords(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(wordService.searchWords(keyword, pageable)));
    }

    @PatchMapping("/{wordId}")
    public ResponseEntity<WordResponse> updateWord(
            @PathVariable Long wordId,
            @Valid @RequestBody UpdateWordRequest request) {
        return ResponseEntity.ok(wordService.updateWord(wordId, request));
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long wordId) {
        wordService.deleteWord(wordId);
        return ResponseEntity.noContent().build();
    }
}
