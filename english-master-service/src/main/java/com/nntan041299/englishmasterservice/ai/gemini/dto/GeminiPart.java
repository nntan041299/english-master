package com.nntan041299.englishmasterservice.ai.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiPart {
    private String text;

    /** Present on audio-generation responses: base64-encoded audio bytes + mime type. */
    private GeminiInlineData inlineData;
}
