package com.example.mangareader.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Konsisten dengan kontrak respons API internal (PRD bagian 8.2).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, PaginationMeta pagination, ErrorInfo error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, PaginationMeta pagination) {
        return new ApiResponse<>(true, data, pagination, null);
    }

    public static <T> ApiResponse<T> failure(ErrorInfo error) {
        return new ApiResponse<>(false, null, null, error);
    }

    public record PaginationMeta(int page, int limit, boolean hasNext, Long total) {
    }

    public record ErrorInfo(String code, String message) {
    }
}
