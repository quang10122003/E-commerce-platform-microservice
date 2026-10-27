package com.example.common.exception;

public final class CacheAccessException extends RuntimeException {
    private final boolean invalidData;

    public CacheAccessException(String message, Throwable cause, boolean invalidData) {
        super(message, cause);
        this.invalidData = invalidData;
    }

    public boolean isInvalidData() {
        return invalidData;
    }
}
