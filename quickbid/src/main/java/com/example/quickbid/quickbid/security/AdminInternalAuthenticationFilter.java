package com.example.quickbid.quickbid.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminInternalAuthenticationFilter extends OncePerRequestFilter {
	public static final String HEADER = "X-QuickBid-Admin-Key";
	private final boolean enabled;
	private final byte[] configuredKey;
	private final Integer employeeId;

	public AdminInternalAuthenticationFilter(@Value("${app.admin.enabled:false}") boolean enabled,
			@Value("${app.admin.internal-key:}") String configuredKey,
			@Value("${app.admin.employee-id:1002}") Integer employeeId) {
		this.enabled = enabled;
		this.configuredKey = configuredKey.getBytes(StandardCharsets.UTF_8);
		this.employeeId = employeeId;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (request.getRequestURI().startsWith("/api/admin/") && enabled && configuredKey.length > 0) {
			String supplied = request.getHeader(HEADER);
			if (supplied != null && MessageDigest.isEqual(configuredKey, supplied.getBytes(StandardCharsets.UTF_8))) {
				var authentication = new UsernamePasswordAuthenticationToken(new AdminPrincipal(employeeId), null,
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		chain.doFilter(request, response);
	}
}
