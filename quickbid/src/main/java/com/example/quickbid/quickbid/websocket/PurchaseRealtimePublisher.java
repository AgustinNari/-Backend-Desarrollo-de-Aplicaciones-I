package com.example.quickbid.quickbid.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.quickbid.quickbid.dto.response.PurchaseDtos.LotClosedEvent;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.AuctionLifecycleEvent;

@Service
public class PurchaseRealtimePublisher {
	private final SimpMessagingTemplate messages;

	public PurchaseRealtimePublisher(SimpMessagingTemplate messages) {
		this.messages = messages;
	}

	public void afterCommit(LotClosedEvent event, Long buyerAccountId) {
		Runnable publish = () -> publish(event, buyerAccountId);
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

	public void publish(LotClosedEvent event, Long buyerAccountId) {
		messages.convertAndSend("/topic/subastas/" + event.subastaId() + "/estado", event);
		if (buyerAccountId != null) {
			messages.convertAndSendToUser(buyerAccountId.toString(), "/queue/notificaciones",
					new LotClosedEvent("LOTE_GANADO", event.subastaId(), event.itemCatalogoId(), event.compraId(),
							event.pujaGanadoraId(), event.montoAdjudicacion(), event.moneda(), event.compradorEmpresa(),
							event.versionEstado()));
		}
	}

	public void afterCommit(AuctionLifecycleEvent event) {
		Runnable publish = () -> messages.convertAndSend("/topic/subastas/" + event.subastaId() + "/estado", event);
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
}
