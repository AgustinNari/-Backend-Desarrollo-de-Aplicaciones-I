package com.example.quickbid.quickbid.websocket;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnectionRegistry {
	private final Map<String, Connection> connections = new ConcurrentHashMap<>();
	private final long ttlMillis;
	private final Clock clock;

	@Autowired
	public WebSocketConnectionRegistry(@Value("${app.websocket.presence.ttl-ms:60000}") long ttlMillis) {
		this(ttlMillis, Clock.systemUTC());
	}

	WebSocketConnectionRegistry(long ttlMillis, Clock clock) {
		this.ttlMillis = ttlMillis;
		this.clock = clock;
	}

	public void connected(String sessionId, Long accountId) {
		if (sessionId != null) connections.put(sessionId, new Connection(accountId, now()));
	}

	public void touch(String sessionId) {
		if (sessionId != null) connections.computeIfPresent(sessionId,
				(key, connection) -> new Connection(connection.accountId(), now()));
	}

	public void disconnected(String sessionId) {
		if (sessionId != null) connections.remove(sessionId);
	}

	public Optional<Long> accountId(String sessionId) {
		Connection connection = sessionId == null ? null : connections.get(sessionId);
		return connection == null ? Optional.empty() : Optional.of(connection.accountId());
	}

	@Scheduled(fixedDelayString = "${app.websocket.presence.cleanup-ms:30000}")
	public void cleanupExpired() {
		Instant expiration = now().minusMillis(ttlMillis);
		connections.entrySet().removeIf(entry -> entry.getValue().lastSeenAt().isBefore(expiration));
	}

	public int activeConnections() {
		return connections.size();
	}

	private Instant now() {
		return clock.instant();
	}

	private record Connection(Long accountId, Instant lastSeenAt) {
	}
}
