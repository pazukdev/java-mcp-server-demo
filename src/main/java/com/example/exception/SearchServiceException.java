package com.example.exception;

public class SearchServiceException extends RuntimeException {
    public SearchServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchServiceException(String message) {
        super(message);
    }
}
