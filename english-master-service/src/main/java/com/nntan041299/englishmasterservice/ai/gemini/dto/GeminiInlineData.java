package com.nntan041299.englishmasterservice.ai.gemini.dto;

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
public class GeminiInlineData {
    private String mimeType;
    /** Base64-encoded raw audio bytes (PCM for the TTS model). */
    private String data;
}
