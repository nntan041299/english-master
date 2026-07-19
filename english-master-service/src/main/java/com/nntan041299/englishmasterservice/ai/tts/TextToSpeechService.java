package com.nntan041299.englishmasterservice.ai.tts;

import com.nntan041299.englishmasterservice.ai.tts.dto.AudioConfig;
import com.nntan041299.englishmasterservice.ai.tts.dto.SynthesisInput;
import com.nntan041299.englishmasterservice.ai.tts.dto.TextToSpeechRequest;
import com.nntan041299.englishmasterservice.ai.tts.dto.TextToSpeechResponse;
import com.nntan041299.englishmasterservice.ai.tts.dto.VoiceSelectionParams;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Synthesizes natural-sounding speech via the Google Cloud Text-to-Speech API (free tier —
 * see google.tts.api-key). Nothing is cached or persisted; each call re-synthesizes from the
 * given text.
 */
@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private static final String AUDIO_ENCODING = "MP3";

    private final TextToSpeechClient textToSpeechClient;

    @Value("${google.tts.api-key}")
    private String apiKey;

    @Value("${google.tts.language-code}")
    private String languageCode;

    @Value("${google.tts.voice-name}")
    private String voiceName;

    /** Returns MP3 audio bytes for the given text. */
    public byte[] synthesizeMp3(String text) {
        TextToSpeechRequest request = new TextToSpeechRequest(
                new SynthesisInput(text),
                new VoiceSelectionParams(languageCode, voiceName),
                new AudioConfig(AUDIO_ENCODING));

        TextToSpeechResponse response = textToSpeechClient.synthesize(apiKey, request);
        return Base64.getDecoder().decode(response.getAudioContent());
    }
}
