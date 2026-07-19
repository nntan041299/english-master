package com.nntan041299.englishmasterservice.ai;

/**
 * Common contract for AI providers that can generate content from a prompt.
 */
public interface AIService {

    /**
     * Generates content for the given prompt and converts it into the requested type.
     *
     * @param prompt       the input prompt
     * @param responseType the type to convert the generated content into.
     *                     Use {@code String.class} for raw text.
     * @param <T>          the response type
     * @return the generated content as an instance of {@code responseType}
     */
    <T> T generateContent(String prompt, Class<T> responseType);

    /**
     * Synthesizes the given text as speech and returns it as playable WAV audio bytes.
     */
    byte[] synthesizeSpeechWav(String text);
}
