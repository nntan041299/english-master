package com.nntan041299.englishmasterservice.word.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveWordRequest {

    @NotBlank(message = "Word text is required")
    @Size(max = 150, message = "Word text must not exceed 150 characters")
    private String text;
}
