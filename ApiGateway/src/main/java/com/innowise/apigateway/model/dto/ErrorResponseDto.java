package com.innowise.apigateway.model.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @ClassName ErrorResponseDto
 * @Description DTO used for exposing structured error details in API responses.
 * Encapsulates HTTP status code, error message, request path, and timestamp.
 * Serves as a standardized error payload for {@link org.springframework.web.bind.annotation.ExceptionHandler} methods.
 * @Author dshparko
 * @Date 16.09.2025 16:25
 * @Version 1.0
 */
public record ErrorResponseDto(
        ZonedDateTime timestamp,
        int status,
        String error,
        String path,
        UUID errorId
) {
    public ErrorResponseDto(int status, String error, String path, UUID errorId) {
        this(ZonedDateTime.now(), status, error, path, errorId);
    }
}
