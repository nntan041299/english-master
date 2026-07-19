package com.nntan041299.englishmasterservice.ai.gemini.dto;

import java.util.List;
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
public class GeminiGenerationConfig {
    private List<String> responseModalities;
    private GeminiSpeechConfig speechConfig;
}
