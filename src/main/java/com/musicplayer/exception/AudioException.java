package com.musicplayer.exception;

/**
 * Unified exception for all audio engine failures.
 *
 * <p>All concrete {@link com.musicplayer.engine.AudioEngine} implementations,
 * including adapters, must translate their internal error representations
 * (checked exceptions, status codes, etc.) into this exception.
 * Nothing engine-specific may leak through the {@code AudioEngine} interface.
 */
public class AudioException extends RuntimeException {

    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}
