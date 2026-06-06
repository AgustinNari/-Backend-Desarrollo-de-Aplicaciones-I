package com.example.quickbid.quickbid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.quickbid.quickbid.dto.response.PurchaseDtos.LotClosedEvent;
import com.example.quickbid.quickbid.websocket.PurchaseRealtimePublisher;

class PurchaseRealtimePublisherTests {
	@Test void publicaCierreDeLoteYNotificaGanador() {
		SimpMessagingTemplate messages = mock(SimpMessagingTemplate.class);
		PurchaseRealtimePublisher publisher = new PurchaseRealtimePublisher(messages);
		LotClosedEvent event = new LotClosedEvent("LOTE_CERRADO", 6001, 9001, 13010L, 12501L,
				new BigDecimal("25100"), "ARS", false, 2L);

		publisher.publish(event, 3001L);

		verify(messages).convertAndSend("/topic/subastas/6001/estado", event);
		verify(messages).convertAndSendToUser("3001", "/queue/notificaciones",
				new LotClosedEvent("LOTE_GANADO", 6001, 9001, 13010L, 12501L,
						new BigDecimal("25100"), "ARS", false, 2L));
	}
}
