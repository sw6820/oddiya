package com.oddiya.plan.exception;

import com.oddiya.plan.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Global exception handler for Plan Service
 * Provides consistent error responses across all endpoints
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle LLM Service failures
     * Returns 503 Service Unavailable
     */
    @ExceptionHandler(LlmServiceException.class)
    public ResponseEntity<ErrorResponse> handleLlmServiceException(LlmServiceException ex) {
        log.error("LLM Service error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            "AI 여행 플래너가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.",
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(error);
    }

    /**
     * Handle WebClient errors (communication with LLM Agent)
     * Returns 503 Service Unavailable
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(WebClientResponseException ex) {
        log.error("LLM Agent communication error: {} - {}", ex.getStatusCode(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            "LLM_AGENT_ERROR",
            "AI 서비스와 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            String.format("Status: %s, Message: %s", ex.getStatusCode(), ex.getMessage())
        );

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(error);
    }

    /**
     * Handle resource not found errors
     * Returns 404 Not Found
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        // Check if it's a "not found" error
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            ErrorResponse error = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                null
            );

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
        }

        // Check if it's an "unauthorized" error
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unauthorized")) {
            ErrorResponse error = new ErrorResponse(
                "UNAUTHORIZED",
                "권한이 없습니다.",
                ex.getMessage()
            );

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
        }

        // Generic server error
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "요청 처리 중 오류가 발생했습니다.",
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    /**
     * Handle all other exceptions
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "서버 오류가 발생했습니다. 관리자에게 문의하세요.",
            ex.getClass().getSimpleName() + ": " + ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}
