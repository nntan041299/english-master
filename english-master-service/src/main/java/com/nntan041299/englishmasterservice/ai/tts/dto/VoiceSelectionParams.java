package com.nntan041299.englishmasterservice.ai.tts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceSelectionParams {
    private String languageCode;
    private String name;
}
