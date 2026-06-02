package com.example.quickbid.quickbid.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityErrorWriter {

	public void write(HttpServletResponse response, int status, String message, String code) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("""
				{"data":null,"message":"%s","errors":[{"field":null,"code":"%s","message":"%s"}]}
				""".formatted(message, code, message));
	}
}
