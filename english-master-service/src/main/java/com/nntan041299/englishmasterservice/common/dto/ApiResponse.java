package com.nntan041299.englishmasterservice.common.dto;

import lombok.Getter;

/**
 * Global response envelope. Every API response body is wrapped so that
 * the client always receives either {"data": {...}} or {"data": [...]}.
 */
@Getter
public class ApiResponse<T> {

    private final T data;

    private ApiResponse(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
