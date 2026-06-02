package com.example.quickbid.quickbid.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.example.quickbid.quickbid.dto.response.ApiError;

public class BusinessException extends RuntimeException {

	private final HttpStatus status;
	private final List<ApiError> errors;

	public BusinessException(HttpStatus status, String message, String code) {
		this(status, message, List.of(new ApiError(null, code, message)));
	}

	public BusinessException(HttpStatus status, String message, List<ApiError> errors) {
		super(message);
		this.status = status;
		this.errors = List.copyOf(errors);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public List<ApiError> getErrors() {
		return errors;
	}
}
