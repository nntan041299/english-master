package com.nntan041299.englishmasterservice.meaning.exception;

/**
 * Thrown when the AI enrichment determines the given text isn't a recognizable word (e.g. every
 * candidate meaning came back with part of speech {@code OTHER}). The caller should not persist
 * anything for this attempt.
 */
public class InvalidWordException extends RuntimeException {

    public InvalidWordException(String message) {
        super(message);
    }
}
