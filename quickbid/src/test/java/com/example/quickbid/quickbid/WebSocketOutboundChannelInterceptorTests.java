package com.example.quickbid.quickbid;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.example.quickbid.quickbid.entity.app.CuentaApp;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;
import com.example.quickbid.quickbid.websocket.WebSocketConnectionRegistry;
import com.example.quickbid.quickbid.websocket.WebSocketOutboundChannelInterceptor;

class WebSocketOutboundChannelInterceptorTests {
	private final WebSocketConnectionRegistry connections = mock(WebSocketConnectionRegistry.class);
	private final CuentaAppRepository accounts = mock(CuentaAppRepository.class);
	private final MessageChannel channel = mock(MessageChannel.class);
	private final WebSocketOutboundChannelInterceptor interceptor =
			new WebSocketOutboundChannelInterceptor(connections, accounts);

	@Test void entregaLiveAUsuarioActivo() {
		CuentaApp account = account("activa");
		when(connections.accountId("session-1")).thenReturn(Optional.of(3001L));
		when(accounts.findById(3001L)).thenReturn(Optional.of(account));

		assertNotNull(interceptor.preSend(message("/topic/subastas/6001/estado"), channel));
	}

	@Test void descartaLiveSiLaCuentaFueBloqueadaDespuesDeSuscribirse() {
		CuentaApp account = account("bloqueada_permanente");
		when(connections.accountId("session-1")).thenReturn(Optional.of(3001L));
		when(accounts.findById(3001L)).thenReturn(Optional.of(account));

		assertNull(interceptor.preSend(message("/topic/subastas/6001/estado"), channel));
	}

	@Test void descartaLiveSinPresenciaAutenticada() {
		when(connections.accountId("session-1")).thenReturn(Optional.empty());

		assertNull(interceptor.preSend(message("/topic/subastas/6001/estado"), channel));
	}

	private CuentaApp account(String state) {
		CuentaApp account = mock(CuentaApp.class);
		when(account.getEstado()).thenReturn(state);
		return account;
	}

	private Message<?> message(String destination) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
		accessor.setSessionId("session-1");
		accessor.setDestination(destination);
		return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
	}
}
