package com.innowise.apigateway.exception;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

/**
 * @ClassName InvalidResourceException
 * @Description Exception thrown when a requested resource is invalid.
 * @Author dshparko
 * @Date 05.10.2025 21:25
 * @Version 1.0
 */
public class InvalidResourceException extends RuntimeException {

    @Getter
    @Serial
    private final UUID errorId = UUID.randomUUID();

    public InvalidResourceException(String message) {
        super(message);
    }

    public InvalidResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidResourceException(Throwable cause) {
        super(cause);
    }

}

