package com.nntan041299.englishmasterservice.ai.tts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextToSpeechResponse {
    /** Base64-encoded audio bytes in the encoding requested (MP3). */
    private String audioContent;
}
