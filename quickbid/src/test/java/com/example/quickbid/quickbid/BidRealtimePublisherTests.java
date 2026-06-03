package com.example.quickbid.quickbid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.quickbid.quickbid.dto.response.SubastaDtos.Bid;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.BidEvent;
import com.example.quickbid.quickbid.websocket.BidRealtimePublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

class BidRealtimePublisherTests {
	@Test void publicaPujaAceptadaEstadoYAvisoPrivadoDeSuperacion() throws Exception {
		SimpMessagingTemplate messages = mock(SimpMessagingTemplate.class);
		BidRealtimePublisher publisher = new BidRealtimePublisher(messages);
		Bid bid = new Bid(10L, 6001, 9001, "aceptada", new BigDecimal("25300"), "ARS", 2L, 2L,
				new BigDecimal("25300"), 1, false);

		publisher.publishAccepted(bid, 3001L, 3004L);

		BidEvent publicEvent = new BidEvent("MEJOR_OFERTA_ACTUALIZADA", 6001, 9001, 10L, new BigDecimal("25300"),
				"ARS", 2L, 2L, 1, "Postor #1");
		BidEvent acceptedEvent = new BidEvent("PUJA_ACEPTADA", 6001, 9001, 10L, new BigDecimal("25300"),
				"ARS", 2L, 2L, 1, "Postor #1");
		verify(messages).convertAndSend("/topic/subastas/6001/items/9001/pujas", publicEvent);
		verify(messages).convertAndSendToUser("3001", "/queue/notificaciones",
				acceptedEvent);
		verify(messages).convertAndSendToUser("3004", "/queue/pujas",
				new BidEvent("PUJA_SUPERADA", 6001, 9001, 10L, new BigDecimal("25300"), "ARS", 2L, 2L, 1, "Postor #1"));
		verify(messages, never()).convertAndSend("/topic/subastas/6001/items/9001/pujas", acceptedEvent);
		verify(messages, never()).convertAndSendToUser("3004", "/queue/notificaciones", acceptedEvent);
		String json = new ObjectMapper().writeValueAsString(publicEvent);
		assertEquals("Postor #1", publicEvent.postorAlias());
		assertFalse(json.contains("email"));
		assertFalse(json.contains("nombre"));
	}
}
