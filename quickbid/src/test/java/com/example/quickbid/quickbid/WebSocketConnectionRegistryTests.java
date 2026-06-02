package com.example.quickbid.quickbid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.quickbid.quickbid.websocket.WebSocketConnectionRegistry;

class WebSocketConnectionRegistryTests {
	@Test void disconnectEliminaPresencia() {
		WebSocketConnectionRegistry connections = new WebSocketConnectionRegistry(60000);
		connections.connected("session-1", 3001L);

		connections.disconnected("session-1");

		assertEquals(0, connections.activeConnections());
	}

	@Test void ttlEliminaPresenciaSinDisconnect() throws Exception {
		WebSocketConnectionRegistry connections = new WebSocketConnectionRegistry(1);
		connections.connected("session-1", 3001L);
		Thread.sleep(5);

		connections.cleanupExpired();

		assertEquals(0, connections.activeConnections());
	}
}
