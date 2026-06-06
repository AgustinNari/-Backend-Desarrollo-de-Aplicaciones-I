package com.example.quickbid.quickbid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.quickbid.quickbid.entity.app.CuentaApp;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;
import com.example.quickbid.quickbid.security.TokenService;
import com.example.quickbid.quickbid.websocket.WebSocketAuthChannelInterceptor;
import com.example.quickbid.quickbid.websocket.WebSocketConnectionRegistry;
import com.example.quickbid.quickbid.websocket.WebSocketSubscriptionAuthorizer;

class WebSocketAuthChannelInterceptorTests {
	private final TokenService tokens = mock(TokenService.class);
	private final CuentaAppRepository accounts = mock(CuentaAppRepository.class);
	private final WebSocketSubscriptionAuthorizer subscriptions = mock(WebSocketSubscriptionAuthorizer.class);
	private final WebSocketConnectionRegistry connections = mock(WebSocketConnectionRegistry.class);
	private final MessageChannel channel = mock(MessageChannel.class);
	private final WebSocketAuthChannelInterceptor interceptor = new WebSocketAuthChannelInterceptor(tokens, accounts,
			subscriptions, connections);

	@Test void connectSinBearerSeRechaza() {
		assertThrows(MessagingException.class, () -> interceptor.preSend(message(StompCommand.CONNECT), channel));
	}

	@Test void subscribeSinPrincipalSeRechaza() {
		when(connections.accountId(null)).thenReturn(Optional.empty());

		assertThrows(MessagingException.class, () -> interceptor.preSend(message(StompCommand.SUBSCRIBE), channel));
	}

	@Test void connectConBearerInvalidoSeRechaza() {
		when(tokens.accountId("jwt-invalido")).thenThrow(new IllegalArgumentException());

		assertThrows(MessagingException.class,
				() -> interceptor.preSend(message(StompCommand.CONNECT, "Bearer jwt-invalido"), channel));
	}

	@Test void connectConCuentaBloqueadaSeRechaza() {
		CuentaApp account = mock(CuentaApp.class);
		when(tokens.accountId("jwt-bloqueado")).thenReturn(3003L);
		when(accounts.findById(3003L)).thenReturn(Optional.of(account));
		when(account.getEstado()).thenReturn("bloqueada_permanente");

		assertThrows(MessagingException.class,
				() -> interceptor.preSend(message(StompCommand.CONNECT, "Bearer jwt-bloqueado"), channel));
	}

	@Test void connectConBearerValidoAsociaCuentaComoPrincipal() {
		CuentaApp account = mock(CuentaApp.class);
		when(tokens.accountId("jwt-demo")).thenReturn(3001L);
		when(accounts.findById(3001L)).thenReturn(Optional.of(account));
		when(account.getEstado()).thenReturn("activa");
		Message<?> message = message(StompCommand.CONNECT, "Bearer jwt-demo", "session-1", null, null);

		Message<?> authenticated = interceptor.preSend(message, channel);

		assertEquals("3001", StompHeaderAccessor.wrap(authenticated).getUser().getName());
		verify(connections).connected("session-1", 3001L);
	}

	@Test void connectConCuentaRestringidaPuedeMirarLive() {
		CuentaApp account = mock(CuentaApp.class);
		when(tokens.accountId("jwt-restringido")).thenReturn(3002L);
		when(accounts.findById(3002L)).thenReturn(Optional.of(account));
		when(account.getEstado()).thenReturn("restriccion_multa");

		Message<?> authenticated = interceptor.preSend(
				message(StompCommand.CONNECT, "Bearer jwt-restringido", "session-2", null, null), channel);

		assertEquals("3002", StompHeaderAccessor.wrap(authenticated).getUser().getName());
		verify(connections).connected("session-2", 3002L);
	}

	@Test void subscribeConPrincipalDelegaAutorizacionFina() {
		interceptor.preSend(message(StompCommand.SUBSCRIBE, null, "session-1",
				"/topic/subastas/6001/estado", 3001L), channel);

		verify(subscriptions).authorize(3001L, "/topic/subastas/6001/estado");
	}

	@Test void subscribeSinPrincipalUsaCuentaRegistradaParaLaSesion() {
		when(connections.accountId("session-1")).thenReturn(Optional.of(3001L));
		Message<?> subscription = message(StompCommand.SUBSCRIBE, null, "session-1",
				"/topic/subastas/6001/estado", null);

		Message<?> authorized = interceptor.preSend(subscription, channel);

		assertEquals("3001", StompHeaderAccessor.wrap(authorized).getUser().getName());
		verify(subscriptions).authorize(3001L, "/topic/subastas/6001/estado");
	}

	@Test void disconnectLimpiaPresencia() {
		interceptor.preSend(message(StompCommand.DISCONNECT, null, "session-1", null, 3001L), channel);

		verify(connections).disconnected("session-1");
	}

	private Message<?> message(StompCommand command) {
		return message(command, null);
	}

	private Message<?> message(StompCommand command, String authorization) {
		return message(command, authorization, null, null, null);
	}

	private Message<?> message(StompCommand command, String authorization, String sessionId, String destination,
			Long accountId) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
		if (authorization != null) accessor.setNativeHeader("Authorization", authorization);
		if (sessionId != null) accessor.setSessionId(sessionId);
		if (destination != null) accessor.setDestination(destination);
		if (accountId != null) accessor.setUser(new UsernamePasswordAuthenticationToken(accountId, null));
		return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
	}
}
