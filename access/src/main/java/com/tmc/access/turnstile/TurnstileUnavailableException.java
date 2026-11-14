package com.tmc.access.turnstile;

public class TurnstileUnavailableException extends RuntimeException {
    public TurnstileUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
