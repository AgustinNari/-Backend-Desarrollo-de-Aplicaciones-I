package com.example.quickbid.quickbid.dto.response;

import java.util.List;

public record ApiResponse<T>(T data, String message, List<ApiError> errors) {

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(data, message, null);
	}

	public static ApiResponse<Void> failure(String message, List<ApiError> errors) {
		return new ApiResponse<>(null, message, errors);
	}
}
