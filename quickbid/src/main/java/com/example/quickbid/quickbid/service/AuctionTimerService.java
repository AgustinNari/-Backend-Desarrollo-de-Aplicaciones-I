package com.example.quickbid.quickbid.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbid.quickbid.service.PurchaseService.PaymentOutcome;

@Service
public class AuctionTimerService {
	private static final int LOT_IDLE_SECONDS = 60;

	private final JdbcTemplate jdbc;
	private final PurchaseService purchases;

	public AuctionTimerService(JdbcTemplate jdbc, PurchaseService purchases) {
		this.jdbc = jdbc;
		this.purchases = purchases;
	}

	@Transactional
	public TimerResult processDueTimers() {
		int closedLots = 0;
		for (Integer auctionId : dueLots()) {
			purchases.closeLot(auctionId, PaymentOutcome.AUTO);
			closedLots++;
		}
		int activatedLots = 0;
		for (Integer auctionId : dueNextLots()) {
			Integer nextItem = nextPendingItem(auctionId);
			if (nextItem != null) {
				activateItem(auctionId, nextItem);
				activatedLots++;
			}
		}
		int finalizedAuctions = 0;
		for (Integer auctionId : dueAuctions()) {
			purchases.closeAuction(auctionId);
			finalizedAuctions++;
		}
		return new TimerResult(closedLots, activatedLots, finalizedAuctions);
	}

	private List<Integer> dueLots() {
		return jdbc.query("""
				SELECT v.subasta_id FROM app_subasta_estado_vivo v
				JOIN app_subasta_ext e ON e.subasta_id=v.subasta_id
				WHERE e.estado_operativo='en_vivo'
				  AND v.item_catalogo_activo_id IS NOT NULL
				  AND v.lote_finaliza_estimado_at IS NOT NULL
				  AND v.lote_finaliza_estimado_at<=CURRENT_TIMESTAMP
				ORDER BY v.lote_finaliza_estimado_at,v.subasta_id
				""", (rs, row) -> rs.getInt(1));
	}

	private List<Integer> dueNextLots() {
		return jdbc.query("""
				SELECT v.subasta_id FROM app_subasta_estado_vivo v
				JOIN app_subasta_ext e ON e.subasta_id=v.subasta_id
				WHERE e.estado_operativo='en_vivo'
				  AND v.item_catalogo_activo_id IS NULL
				  AND v.proximo_lote_programado_at IS NOT NULL
				  AND v.proximo_lote_programado_at<=CURRENT_TIMESTAMP
				ORDER BY v.proximo_lote_programado_at,v.subasta_id
				""", (rs, row) -> rs.getInt(1));
	}

	private List<Integer> dueAuctions() {
		return jdbc.query("""
				SELECT v.subasta_id FROM app_subasta_estado_vivo v
				JOIN app_subasta_ext e ON e.subasta_id=v.subasta_id
				WHERE e.estado_operativo='en_vivo'
				  AND v.item_catalogo_activo_id IS NULL
				  AND v.subasta_finaliza_programado_at IS NOT NULL
				  AND v.subasta_finaliza_programado_at<=CURRENT_TIMESTAMP
				ORDER BY v.subasta_finaliza_programado_at,v.subasta_id
				""", (rs, row) -> rs.getInt(1));
	}

	private Integer nextPendingItem(Integer auctionId) {
		List<Integer> values = jdbc.query("""
				SELECT i.identificador FROM "itemsCatalogo" i
				JOIN catalogos c ON c.identificador=i.catalogo
				WHERE c.subasta=? AND i.subastado='no'
				ORDER BY i.identificador
				LIMIT 1
				""", (rs, row) -> rs.getInt(1), auctionId);
		return values.isEmpty() ? null : values.get(0);
	}

	private void activateItem(Integer auctionId, Integer itemId) {
		jdbc.update("""
				UPDATE app_subasta_estado_vivo
				SET item_catalogo_activo_id=?,version=version+1,lote_iniciado_at=CURRENT_TIMESTAMP,
					lote_finaliza_estimado_at=?,proximo_lote_programado_at=NULL,
					subasta_finaliza_programado_at=NULL,updated_at=CURRENT_TIMESTAMP
				WHERE subasta_id=?
				""", itemId, OffsetDateTime.now().plusSeconds(LOT_IDLE_SECONDS), auctionId);
	}

	public record TimerResult(int lotesCerrados, int lotesActivados, int subastasFinalizadas) {
	}
}
