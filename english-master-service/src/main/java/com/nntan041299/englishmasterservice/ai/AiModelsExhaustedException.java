package com.nntan041299.englishmasterservice.ai;

/**
 * Thrown when every configured Gemini model for a given operation is over its daily quota or
 * requests-per-minute limit. This is an expected, recurring condition (not a bug), so callers
 * should log it concisely instead of at error level with a stack trace.
 */
public class AiModelsExhaustedException extends RuntimeException {

    public AiModelsExhaustedException(String message) {
        super(message);
    }
}
