package com.example.quickbid.quickbid.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.example.quickbid.quickbid.dto.response.ApiError;
import com.example.quickbid.quickbid.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityErrorWriter {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public void write(HttpServletResponse response, int status, String message, String code) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		ApiError error = new ApiError(null, code, message);
		objectMapper.writeValue(response.getWriter(), ApiResponse.failure(message, List.of(error)));
	}
}
