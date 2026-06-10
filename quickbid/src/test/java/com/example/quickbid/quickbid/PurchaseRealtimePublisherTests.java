package com.example.quickbid.quickbid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.quickbid.quickbid.dto.response.PurchaseDtos.LotClosedEvent;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.AuctionLifecycleEvent;
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

	@Test void elGanadorRecibeLaProgramacionDeLoQueSigue() {
		SimpMessagingTemplate messages = mock(SimpMessagingTemplate.class);
		PurchaseRealtimePublisher publisher = new PurchaseRealtimePublisher(messages);
		OffsetDateTime nextLotAt = OffsetDateTime.parse("2026-06-10T15:00:10-03:00");
		LotClosedEvent event = new LotClosedEvent("LOTE_CERRADO", 6001, 9001, 13010L, 12501L,
				new BigDecimal("25100"), "ARS", false, 2L, nextLotAt, null);

		publisher.publish(event, 3001L);

		verify(messages).convertAndSendToUser("3001", "/queue/notificaciones",
				new LotClosedEvent("LOTE_GANADO", 6001, 9001, 13010L, 12501L,
						new BigDecimal("25100"), "ARS", false, 2L, nextLotAt, null));
	}

	@Test void publicaEventosDeCicloDeVidaAlTopicDeEstado() {
		SimpMessagingTemplate messages = mock(SimpMessagingTemplate.class);
		PurchaseRealtimePublisher publisher = new PurchaseRealtimePublisher(messages);
		OffsetDateTime lotDeadline = OffsetDateTime.parse("2026-06-10T15:01:00-03:00");
		AuctionLifecycleEvent event = new AuctionLifecycleEvent("LOTE_ACTIVADO", 6001, 9002, 5L, lotDeadline);

		publisher.afterCommit(event);

		verify(messages).convertAndSend("/topic/subastas/6001/estado", event);
	}
}
