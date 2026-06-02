package com.example.quickbid.quickbid.websocket;

import java.util.Set;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;

@Component
public class WebSocketOutboundChannelInterceptor implements ChannelInterceptor {
	private final WebSocketConnectionRegistry connections;
	private final CuentaAppRepository accounts;

	public WebSocketOutboundChannelInterceptor(WebSocketConnectionRegistry connections, CuentaAppRepository accounts) {
		this.connections = connections;
		this.accounts = accounts;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		if (accessor.getCommand() != StompCommand.MESSAGE || !protectedDestination(accessor.getDestination())) {
			return message;
		}
		return connections.accountId(accessor.getSessionId())
				.flatMap(accounts::findById)
				.filter(account -> Set.of("activa", "restriccion_multa").contains(account.getEstado()))
				.map(account -> message)
				.orElse(null);
	}

	private boolean protectedDestination(String destination) {
		return destination != null && (destination.startsWith("/topic/subastas/") || destination.startsWith("/queue/"));
	}
}
