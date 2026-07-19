package com.nntan041299.englishmasterservice.ai;

/**
 * AI capability for synthesizing spoken audio from text.
 */
public interface SpeechAIService {

    /**
     * Synthesizes the given text as speech and returns it as playable WAV audio bytes.
     */
    byte[] synthesizeSpeechWav(String text);
}
