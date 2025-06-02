package com.example.exception;

public class OpenAIServiceException extends RuntimeException {
    public OpenAIServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenAIServiceException(String message) {
        super(message);
    }
}
