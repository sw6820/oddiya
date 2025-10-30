package com.oddiya.plan.exception;

/**
 * Exception thrown when LLM Agent service fails
 * This indicates the AI service is temporarily unavailable
 */
public class LlmServiceException extends RuntimeException {

    private final String errorCode;

    public LlmServiceException(String message) {
        super(message);
        this.errorCode = "LLM_UNAVAILABLE";
    }

    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LLM_UNAVAILABLE";
    }

    public LlmServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
