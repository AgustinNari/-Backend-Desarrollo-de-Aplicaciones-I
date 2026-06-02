package com.example.quickbid.quickbid.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.quickbid.quickbid.dto.response.SubastaDtos.AuctionStateEvent;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.Bid;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.BidEvent;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.RejectedBidEvent;

@Service
public class BidRealtimePublisher {
	private final SimpMessagingTemplate messages;

	public BidRealtimePublisher(SimpMessagingTemplate messages) {
		this.messages = messages;
	}

	public void afterCommit(Bid accepted, Long acceptedAccountId, Long surpassedAccountId) {
		Runnable publish = () -> publishAccepted(accepted, acceptedAccountId, surpassedAccountId);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					publish.run();
				}
			});
		} else {
			publish.run();
		}
	}

	public void publishAccepted(Bid accepted, Long acceptedAccountId, Long surpassedAccountId) {
		BidEvent acceptedEvent = event("PUJA_ACEPTADA", accepted);
		messages.convertAndSend(itemTopic(accepted), event("MEJOR_OFERTA_ACTUALIZADA", accepted));
		messages.convertAndSend(stateTopic(accepted), new AuctionStateEvent("ESTADO_ACTUALIZADO", accepted.subastaId(),
				accepted.itemCatalogoId(), accepted.mejorOfertaActual(), accepted.moneda(), accepted.versionEstado()));
		messages.convertAndSendToUser(acceptedAccountId.toString(), "/queue/notificaciones", acceptedEvent);
		if (surpassedAccountId != null) {
			messages.convertAndSendToUser(surpassedAccountId.toString(), "/queue/pujas",
					event("PUJA_SUPERADA", accepted));
		}
	}

	public void publishRejected(Long accountId, Integer subastaId, Integer itemCatalogoId, String code, String message) {
		messages.convertAndSendToUser(accountId.toString(), "/queue/pujas",
				new RejectedBidEvent("PUJA_RECHAZADA", subastaId, itemCatalogoId, code, message));
	}

	private BidEvent event(String type, Bid bid) {
		return new BidEvent(type, bid.subastaId(), bid.itemCatalogoId(), bid.id(), bid.monto(), bid.moneda(),
				bid.secuencia(), bid.versionEstado(), bid.numeroPostor(), "Postor #" + bid.numeroPostor());
	}

	private String itemTopic(Bid bid) {
		return "/topic/subastas/" + bid.subastaId() + "/items/" + bid.itemCatalogoId() + "/pujas";
	}

	private String stateTopic(Bid bid) {
		return "/topic/subastas/" + bid.subastaId() + "/estado";
	}
}
