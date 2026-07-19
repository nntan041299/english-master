package com.nntan041299.englishmasterservice.listening.service;

import com.nntan041299.englishmasterservice.ai.AIService;
import com.nntan041299.englishmasterservice.ai.AiPromptKey;
import com.nntan041299.englishmasterservice.ai.AiPromptManager;
import com.nntan041299.englishmasterservice.ai.tts.TextToSpeechService;
import com.nntan041299.englishmasterservice.auth.entity.LanguageLevel;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeAiResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningChallengeResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningFeedbackAiResponse;
import com.nntan041299.englishmasterservice.listening.dto.ListeningFeedbackResponse;
import com.nntan041299.englishmasterservice.listening.dto.SubmitListeningRequest;
import com.nntan041299.englishmasterservice.listening.entity.ListeningChallenge;
import com.nntan041299.englishmasterservice.listening.entity.ListeningSubmission;
import com.nntan041299.englishmasterservice.listening.repository.ListeningChallengeRepository;
import com.nntan041299.englishmasterservice.listening.repository.ListeningSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningService {

    /** Target sentence length (in words) to generate per CEFR level, so difficulty scales with level. */
    private static final Map<LanguageLevel, SentenceLength> SENTENCE_LENGTH_BY_LEVEL = new EnumMap<>(Map.of(
            LanguageLevel.A1, new SentenceLength(4, 6),
            LanguageLevel.A2, new SentenceLength(6, 9),
            LanguageLevel.B1, new SentenceLength(8, 12),
            LanguageLevel.B2, new SentenceLength(10, 15),
            LanguageLevel.C1, new SentenceLength(12, 18),
            LanguageLevel.C2, new SentenceLength(15, 22)));

    private final AIService aiService;
    private final AiPromptManager aiPromptManager;
    private final TextToSpeechService textToSpeechService;
    private final CurrentUserProvider currentUserProvider;
    private final ListeningChallengeRepository challengeRepository;
    private final ListeningSubmissionRepository submissionRepository;

    /**
     * Returns a listening challenge for the current user at their own language level. Reuses an
     * existing challenge at that level the user hasn't answered yet, if one exists, instead of
     * spending an AI call generating one that would just replace it unused.
     */
    @Transactional
    public ListeningChallengeResponse generateChallenge() {
        User user = currentUserProvider.getCurrentUser();
        LanguageLevel level = user.getLanguageLevel();

        List<ListeningChallenge> unsubmitted =
                challengeRepository.findUnsubmittedByUserIdAndLevel(user.getId(), level, PageRequest.of(0, 1));
        if (!unsubmitted.isEmpty()) {
            ListeningChallenge existing = unsubmitted.getFirst();
            log.info("listening_challenge_reused user={} level={} challenge={}", user.getId(), level, existing.getId());
            return toResponse(existing);
        }

        SentenceLength length = SENTENCE_LENGTH_BY_LEVEL.get(level);

        String prompt = aiPromptManager.get(AiPromptKey.LISTENING_CHALLENGE_GENERATION)
                .formatted(level.name(), length.min(), length.max());
        ListeningChallengeAiResponse aiResponse =
                aiService.generateContent(prompt, ListeningChallengeAiResponse.class);

        ListeningChallenge challenge = challengeRepository.save(ListeningChallenge.builder()
                .user(user)
                .level(level)
                .sentence(aiResponse.sentence())
                .build());

        log.info("listening_challenge_generated user={} level={} challenge={}", user.getId(), level, challenge.getId());
        return toResponse(challenge);
    }

    /** Sends the user's transcript to the AI for evaluation and persists the submission. */
    @Transactional
    public ListeningFeedbackResponse submit(SubmitListeningRequest request) {
        User user = currentUserProvider.getCurrentUser();

        ListeningChallenge challenge = challengeRepository.findByIdAndUserId(request.challengeId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Listening challenge not found: " + request.challengeId()));

        String prompt = aiPromptManager.get(AiPromptKey.LISTENING_FEEDBACK)
                .formatted(challenge.getLevel().name(), challenge.getSentence(), request.transcript());
        ListeningFeedbackAiResponse aiResponse =
                aiService.generateContent(prompt, ListeningFeedbackAiResponse.class);

        boolean correct = Boolean.TRUE.equals(aiResponse.correct());

        ListeningSubmission submission = submissionRepository.save(ListeningSubmission.builder()
                .challenge(challenge)
                .user(user)
                .transcript(request.transcript())
                .correct(correct)
                .feedback(aiResponse.feedback())
                .build());

        log.info("listening_submission_scored user={} challenge={} submission={} correct={}",
                user.getId(), challenge.getId(), submission.getId(), correct);

        return new ListeningFeedbackResponse(
                submission.getId(), correct, submission.getFeedback(), challenge.getSentence());
    }

    /** Synthesizes the given challenge's sentence as MP3 audio via Google Cloud Text-to-Speech. */
    public byte[] synthesizeAudio(Long challengeId) {
        User user = currentUserProvider.getCurrentUser();

        ListeningChallenge challenge = challengeRepository.findByIdAndUserId(challengeId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Listening challenge not found: " + challengeId));

        return textToSpeechService.synthesizeMp3(challenge.getSentence());
    }

    private ListeningChallengeResponse toResponse(ListeningChallenge challenge) {
        return new ListeningChallengeResponse(challenge.getId(), challenge.getLevel(), challenge.getSentence());
    }

    /** Target sentence length band (in words) for a CEFR level. */
    private record SentenceLength(int min, int max) {}
}
