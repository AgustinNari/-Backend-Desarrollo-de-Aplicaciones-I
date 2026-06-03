package com.example.quickbid.quickbid.websocket;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import com.example.quickbid.quickbid.repository.app.AuctionQueryRepository;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;

@Component
public class WebSocketSubscriptionAuthorizer {
	private static final Set<String> PRIVATE_DESTINATIONS = Set.of(
			"/user/queue/notificaciones",
			"/user/queue/pujas");
	private static final Pattern AUCTION_STATE = Pattern.compile("^/topic/subastas/(\\d+)/estado$");
	private static final Pattern ITEM_BIDS = Pattern.compile("^/topic/subastas/(\\d+)/items/(\\d+)/pujas$");

	private final CuentaAppRepository accounts;
	private final AuctionQueryRepository auctions;

	public WebSocketSubscriptionAuthorizer(CuentaAppRepository accounts, AuctionQueryRepository auctions) {
		this.accounts = accounts;
		this.auctions = auctions;
	}

	public void authorize(Long accountId, String destination) {
		requireLiveViewer(accountId);
		if (PRIVATE_DESTINATIONS.contains(destination)) return;

		Matcher auctionState = AUCTION_STATE.matcher(destination == null ? "" : destination);
		if (auctionState.matches()) {
			requireAuction(Integer.valueOf(auctionState.group(1)));
			return;
		}

		Matcher itemBids = ITEM_BIDS.matcher(destination == null ? "" : destination);
		if (itemBids.matches()) {
			requireAuctionItem(Integer.valueOf(itemBids.group(1)), Integer.valueOf(itemBids.group(2)));
			return;
		}
		throw new MessagingException("Destino STOMP no permitido");
	}

	private void requireLiveViewer(Long accountId) {
		var account = accounts.findById(accountId)
				.orElseThrow(() -> new MessagingException("Cuenta inexistente"));
		if (!Set.of("activa", "restriccion_multa").contains(account.getEstado())) {
			throw new MessagingException("Cuenta bloqueada");
		}
	}

	private void requireAuction(Integer auctionId) {
		if (!auctions.existsAuction(auctionId)) {
			throw new MessagingException("Subasta inexistente");
		}
	}

	private void requireAuctionItem(Integer auctionId, Integer itemId) {
		requireAuction(auctionId);
		if (!auctions.existsAuctionItem(auctionId, itemId)) {
			throw new MessagingException("Item inexistente para la subasta");
		}
	}
}
