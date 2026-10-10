package com.example.mangareader.common.exception;

public class AppException extends RuntimeException {
    private final ErrorCode code;
    private final String publicMessage;

    public AppException(ErrorCode code, String publicMessage) {
        super(publicMessage);
        this.code = code;
        this.publicMessage = publicMessage;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getPublicMessage() {
        return publicMessage;
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.NOT_FOUND, message);
    }

    public static AppException validation(String message) {
        return new AppException(ErrorCode.VALIDATION_ERROR, message);
    }

    public static AppException conflict(String message) {
        return new AppException(ErrorCode.CONFLICT, message);
    }

    public static AppException forbidden(String message) {
        return new AppException(ErrorCode.FORBIDDEN, message);
    }
}
