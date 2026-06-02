package com.example.quickbid.quickbid.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

	private final SecurityErrorWriter writer;

	public JsonAccessDeniedHandler(SecurityErrorWriter writer) {
		this.writer = writer;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		writer.write(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado", "FORBIDDEN");
	}
}
