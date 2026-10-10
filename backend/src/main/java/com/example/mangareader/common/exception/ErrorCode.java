package com.example.mangareader.common.exception;

import org.springframework.http.HttpStatus;

/** Kode error internal sesuai PRD bagian 8.3. */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    CONFLICT(HttpStatus.CONFLICT),
    UPSTREAM_RATE_LIMITED(HttpStatus.BAD_GATEWAY),
    UPSTREAM_UNAVAILABLE(HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
