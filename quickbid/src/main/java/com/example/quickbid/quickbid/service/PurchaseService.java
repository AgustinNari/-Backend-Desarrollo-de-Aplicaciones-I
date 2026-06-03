package com.example.quickbid.quickbid.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbid.quickbid.audit.AuditEvent;
import com.example.quickbid.quickbid.audit.AuditService;
import com.example.quickbid.quickbid.dto.request.PurchaseDeliveryRequest;
import com.example.quickbid.quickbid.dto.request.PurchasePaymentRequest;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Detail;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Document;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Fine;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.LotClosedEvent;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Page;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Payment;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Summary;
import com.example.quickbid.quickbid.entity.app.CuentaApp;
import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;
import com.example.quickbid.quickbid.repository.app.DireccionEnvioRepository;
import com.example.quickbid.quickbid.repository.app.PurchaseReadRepository;
import com.example.quickbid.quickbid.websocket.PurchaseRealtimePublisher;

@Service
public class PurchaseService {
	private static final BigDecimal FINE_RATE = new BigDecimal("0.10");
	private final JdbcTemplate jdbc;
	private final CuentaAppRepository accounts;
	private final CategoriaService categories;
	private final AuditService audit;
	private final PurchaseRealtimePublisher realtime;
	private final MailNotificationService mail;
	private final PurchaseReadRepository reads;
	private final DireccionEnvioRepository addresses;
	private final BigDecimal shippingFlatCost;
	private final int completedPoints;
	private final int finePointsPenalty;

	public PurchaseService(JdbcTemplate jdbc, CuentaAppRepository accounts, CategoriaService categories,
			AuditService audit, PurchaseRealtimePublisher realtime, MailNotificationService mail,
			PurchaseReadRepository reads, DireccionEnvioRepository addresses,
			@Value("${app.purchase.shipping-flat-cost:5000}") BigDecimal shippingFlatCost,
			@Value("${app.purchase.completed-points:100}") int completedPoints,
			@Value("${app.purchase.fine-points-penalty:100}") int finePointsPenalty) {
		this.jdbc = jdbc;
		this.accounts = accounts;
		this.categories = categories;
		this.audit = audit;
		this.realtime = realtime;
		this.mail = mail;
		this.reads = reads;
		this.addresses = addresses;
		this.shippingFlatCost = shippingFlatCost;
		this.completedPoints = completedPoints;
		this.finePointsPenalty = finePointsPenalty;
	}

	@Transactional(readOnly = true)
	public Page<Summary> list(Long accountId, int page, int size) {
		account(accountId);
		checkPage(page, size);
		long total = reads.countByAccountId(accountId);
		List<Summary> values = reads.findByAccountId(accountId, page, size);
		return new Page<>(values, page, size, total, (int) Math.ceil((double) total / size));
	}

	@Transactional(readOnly = true)
	public Detail detail(Long accountId, Long purchaseId) {
		return detail(ownedPurchase(accountId, purchaseId, false));
	}

	@Transactional(readOnly = true)
	public List<Document> documents(Long accountId, Long purchaseId) {
		ownedPurchase(accountId, purchaseId, false);
		return reads.findAvailableDocuments(purchaseId);
	}

	@Transactional
	public PurchaseDtos.Delivery configureDelivery(Long accountId, Long purchaseId, PurchaseDeliveryRequest request) {
		Purchase purchase = ownedPurchase(accountId, purchaseId, true);
		if (!purchase.state().equals("pagos_extra_pendientes")) {
			throw conflict("La compra no admite configurar entrega", "INVALID_STATE_TRANSITION");
		}
		Long addressId = null;
		BigDecimal cost = BigDecimal.ZERO;
		boolean losesCoverage = request.tipo().equals("retiro");
		if (request.tipo().equals("envio")) {
			addressId = shippingAddress(accountId, request.direccionEnvioId());
			cost = shippingFlatCost;
		}
		List<Long> existing = jdbc.query("SELECT id FROM app_entregas WHERE compra_id=?", (rs, row) -> rs.getLong(1),
				purchaseId);
		if (existing.isEmpty()) {
			jdbc.update("""
					INSERT INTO app_entregas(compra_id,tipo,direccion_envio_id,costo_envio,estado,perdio_cobertura_seguro)
					VALUES (?,?,?,?,'pendiente',?)
					""", purchaseId, request.tipo(), addressId, cost, losesCoverage);
		} else {
			jdbc.update("""
					UPDATE app_entregas SET tipo=?,direccion_envio_id=?,costo_envio=?,estado='pendiente',
						perdio_cobertura_seguro=?,updated_at=CURRENT_TIMESTAMP WHERE compra_id=?
					""", request.tipo(), addressId, cost, losesCoverage, purchaseId);
		}
		audit.record(new AuditEvent("usuario", accountId, "compra.entrega_configurada", "compra", purchaseId,
				"{\"tipo\":\"" + request.tipo() + "\"}"));
		return delivery(purchaseId);
	}

	@Transactional
	public Payment payExtras(Long accountId, Long purchaseId, PurchasePaymentRequest request) {
		return payExtras(accountId, purchaseId, request.medioPagoId(), normalizedKey(request.idempotencyKey()),
				PaymentOutcome.AUTO);
	}

	@Transactional
	public Payment payFine(Long accountId, Long purchaseId, PurchasePaymentRequest request) {
		return payFine(accountId, purchaseId, request.medioPagoId(), normalizedKey(request.idempotencyKey()),
				PaymentOutcome.AUTO);
	}

	@Transactional
	public Detail closeLot(Integer auctionId, PaymentOutcome outcome) {
		LiveState live = lockLiveState(auctionId);
		if (live.itemId() == null) throw conflict("El lote ya fue cerrado", "LOT_ALREADY_CLOSED");
		Auction auction = auction(auctionId);
		if (!auction.state().equals("en_vivo")) throw conflict("La subasta no esta en vivo", "AUCTION_NOT_LIVE");
		Item item = item(live.itemId());
		if (purchaseForItem(item.id())) throw conflict("El lote ya posee compra", "LOT_ALREADY_CLOSED");
		WinningBid winning = winningBid(auctionId, item.id());
		Long purchaseId;
		Long buyerAccountId = null;
		Long winningBidId = null;
		BigDecimal amount;
		if (winning == null) {
			amount = item.basePrice();
			purchaseId = insertPurchase(auctionId, item, null, true, null, amount, auction.currency(), null,
					"pagos_extra_pendientes");
			updateConsignment(item.id(), "comprada_por_empresa");
		} else {
			buyerAccountId = winning.accountId();
			winningBidId = winning.id();
			amount = winning.amount();
			jdbc.update("UPDATE app_pujas_live SET estado='superada' WHERE subasta_id=? AND item_catalogo_id=? AND estado='aceptada' AND id<>?",
					auctionId, item.id(), winning.id());
			jdbc.update("UPDATE app_pujas_live SET estado='ganadora' WHERE id=?", winning.id());
			jdbc.update("UPDATE pujos SET ganador='no' WHERE item=?", item.id());
			jdbc.update("UPDATE pujos SET ganador='si' WHERE identificador=?", winning.legacyBidId());
			purchaseId = insertPurchase(auctionId, item, winning.accountId(), false, winning.id(), winning.amount(),
					auction.currency(), winning.paymentId(), "adjudicacion_pendiente");
			registerLegacySale(auctionId, item, winning);
			updateConsignment(item.id(), "vendida");
			automaticAdjudicationPayment(purchaseId, winning, auction.currency(), outcome);
		}
		jdbc.update("UPDATE \"itemsCatalogo\" SET subastado='si' WHERE identificador=?", item.id());
		long nextVersion = live.version() + 1;
		jdbc.update("""
				UPDATE app_subasta_estado_vivo SET item_catalogo_activo_id=NULL,version=?,updated_at=CURRENT_TIMESTAMP
				WHERE subasta_id=?
				""", nextVersion, auctionId);
		notifyOwner(item.id(), purchaseId);
		audit.record(new AuditEvent("sistema", null, "subasta.lote_cerrado", "compra", purchaseId,
				"{\"subastaId\":" + auctionId + ",\"itemId\":" + item.id() + "}"));
		realtime.afterCommit(new LotClosedEvent("LOTE_CERRADO", auctionId, item.id(), purchaseId, winningBidId, amount,
				auction.currency(), winning == null, nextVersion), buyerAccountId);
		return internalDetail(purchaseId);
	}

	@Transactional
	public void closeAuction(Integer auctionId) {
		LiveState live = lockLiveState(auctionId);
		if (live.itemId() != null) throw conflict("Existe un lote activo", "INVALID_STATE_TRANSITION");
		jdbc.update("UPDATE app_subasta_ext SET estado_operativo='finalizada',updated_at=CURRENT_TIMESTAMP WHERE subasta_id=?",
				auctionId);
		jdbc.update("UPDATE subastas SET estado='cerrada' WHERE identificador=?", auctionId);
		audit.record(new AuditEvent("sistema", null, "subasta.cerrada", "subasta", auctionId.longValue(), "{}"));
	}

	@Transactional
	public Payment simulateSuccessfulExtras(Long accountId, Long purchaseId, Long paymentId) {
		return payExtras(accountId, purchaseId, paymentId, "internal-extra-success-" + UUID.randomUUID(),
				PaymentOutcome.SUCCESS);
	}

	@Transactional
	public Payment simulateFailedExtras(Long accountId, Long purchaseId, Long paymentId) {
		return payExtras(accountId, purchaseId, paymentId, "internal-extra-failure-" + UUID.randomUUID(),
				PaymentOutcome.FAILURE);
	}

	@Transactional
	public Payment simulateSuccessfulFine(Long accountId, Long purchaseId, Long paymentId) {
		return payFine(accountId, purchaseId, paymentId, "internal-fine-success-" + UUID.randomUUID(),
				PaymentOutcome.SUCCESS);
	}

	@Transactional
	public Payment simulateFailedFine(Long accountId, Long purchaseId, Long paymentId) {
		return payFine(accountId, purchaseId, paymentId, "internal-fine-failure-" + UUID.randomUUID(),
				PaymentOutcome.FAILURE);
	}

	@Transactional
	public void expireFine(Long fineId, boolean force) {
		FineRow fine = lockFine(fineId);
		if (!fine.state().equals("pendiente")) throw conflict("La multa no esta pendiente", "INVALID_STATE_TRANSITION");
		if (!force && fine.expiresAt().isAfter(OffsetDateTime.now())) {
			throw conflict("La multa todavia no vencio", "FINE_NOT_EXPIRED");
		}
		jdbc.update("UPDATE app_multas SET estado='vencida' WHERE id=?", fineId);
		CuentaApp account = account(fine.accountId());
		account.changeState("bloqueada_permanente");
		categories.addPoints(account, -finePointsPenalty, "multa_vencida", "multa", fineId);
		notify(fine.accountId(), "multa_vencida", "Cuenta bloqueada",
				"La multa vencio y la cuenta quedo bloqueada permanentemente.", "multa", fineId);
		audit.record(new AuditEvent("sistema", null, "multa.vencida", "multa", fineId, "{}"));
	}

	@Transactional
	public void markFinePaid(Long fineId) {
		FineRow fine = lockFine(fineId);
		if (!fine.state().equals("pendiente")) throw conflict("La multa no esta pendiente", "INVALID_STATE_TRANSITION");
		jdbc.update("UPDATE app_multas SET estado='pagada',paid_at=CURRENT_TIMESTAMP WHERE id=?", fineId);
		Long purchaseId = jdbc.queryForObject("SELECT compra_id FROM app_multas WHERE id=?", Long.class, fineId);
		jdbc.update("UPDATE app_compras SET estado='pagos_extra_pendientes',updated_at=CURRENT_TIMESTAMP WHERE id=?",
				purchaseId);
		if (pendingFineCount(fine.accountId()) == 0) account(fine.accountId()).changeState("activa");
		notify(fine.accountId(), "multa_pagada", "Multa pagada",
				"La restriccion por multa fue regularizada manualmente.", "multa", fineId);
		audit.record(new AuditEvent("admin", null, "multa.marcada_pagada", "multa", fineId, "{}"));
	}

	@Transactional
	public void completeDelivery(Long purchaseId) {
		completeDelivery(purchaseId, "envio");
	}

	@Transactional
	public void completePickup(Long purchaseId) {
		completeDelivery(purchaseId, "retiro");
	}

	@Transactional
	public void abandon(Long purchaseId, boolean pickupFailure) {
		Purchase purchase = lockPurchase(purchaseId);
		String state = pickupFailure ? "abandonada_por_incumplimiento_retiro" : "abandonada_por_incumplimiento_pago";
		jdbc.update("UPDATE app_compras SET estado=?,updated_at=CURRENT_TIMESTAMP WHERE id=?", state, purchaseId);
		if (purchase.accountId() != null) {
			notify(purchase.accountId(), "compra_abandonada", "Compra abandonada",
					"La compra fue marcada por incumplimiento.", "compra", purchaseId);
		}
		audit.record(new AuditEvent("sistema", null, "compra.abandonada", "compra", purchaseId,
				"{\"estado\":\"" + state + "\"}"));
	}

	private void automaticAdjudicationPayment(Long purchaseId, WinningBid winning, String currency,
			PaymentOutcome outcome) {
		boolean success = paymentSucceeds(winning.accountId(), winning.paymentId(), currency, winning.amount(), outcome);
		Payment payment = insertPayment(purchaseId, null, winning.paymentId(), winning.amount(), currency,
				success ? "aprobado" : "rechazado", "auto-adjudicacion-" + purchaseId,
				success ? null : "INSUFFICIENT_FUNDS_OR_LIMIT");
		if (success) {
			consume(winning.paymentId(), winning.amount());
			jdbc.update("UPDATE app_compras SET estado='pagos_extra_pendientes',updated_at=CURRENT_TIMESTAMP WHERE id=?",
					purchaseId);
			categories.addPoints(account(winning.accountId()), completedPoints, "compra_concretada", "compra", purchaseId);
			notify(winning.accountId(), "pago_adjudicacion_exitoso", "Pago de adjudicacion aprobado",
					"El monto adjudicado fue cobrado. Restan comision y entrega.", "compra", purchaseId);
			audit.record(new AuditEvent("sistema", null, "pago.adjudicacion_aprobado", "pago", payment.id(), "{}"));
		} else {
			Long fineId = createFine(winning.accountId(), purchaseId, winning.amount(), currency);
			jdbc.update("UPDATE app_compras SET estado='multa_activa',updated_at=CURRENT_TIMESTAMP WHERE id=?", purchaseId);
			CuentaApp account = account(winning.accountId());
			account.changeState("restriccion_multa");
			categories.addPoints(account, -finePointsPenalty, "multa_generada", "multa", fineId);
			notify(winning.accountId(), "multa_generada", "Multa pendiente",
					"El cobro automatico fallo. Debes pagar obligacion y multa dentro de 72 horas.", "multa", fineId);
			audit.record(new AuditEvent("sistema", null, "multa.generada", "multa", fineId,
					"{\"compraId\":" + purchaseId + "}"));
		}
		notify(winning.accountId(), "lote_ganado", "Lote ganado", "Ganaste el lote subastado.", "compra", purchaseId);
	}

	private Payment payExtras(Long accountId, Long purchaseId, Long paymentId, String key, PaymentOutcome outcome) {
		Payment replay = replayPayment(accountId, purchaseId, paymentId, key);
		if (replay != null) return replay;
		Purchase purchase = ownedPurchase(accountId, purchaseId, true);
		if (!purchase.state().equals("pagos_extra_pendientes")) {
			throw conflict("La compra no admite pago de extras", "INVALID_STATE_TRANSITION");
		}
		if (activeFine(purchaseId) != null) throw conflict("La compra posee una multa activa", "FINE_PAYMENT_REQUIRED");
		PurchaseDtos.Delivery delivery = delivery(purchaseId);
		if (delivery == null) throw conflict("Primero debes elegir envio o retiro", "DELIVERY_SELECTION_REQUIRED");
		BigDecimal amount = commission(purchase.itemId()).add(delivery.costoEnvio());
		boolean success = paymentSucceeds(accountId, paymentId, purchase.currency(), amount, outcome);
		Payment payment = insertPayment(purchaseId, null, paymentId, amount, purchase.currency(),
				success ? "aprobado" : "rechazado", key, success ? null : "INSUFFICIENT_FUNDS_OR_LIMIT");
		if (success) {
			consume(paymentId, amount);
			String nextState = delivery.tipo().equals("envio") ? "entrega_pendiente" : "retiro_pendiente";
			jdbc.update("UPDATE app_entregas SET estado='pagada',updated_at=CURRENT_TIMESTAMP WHERE compra_id=?", purchaseId);
			jdbc.update("UPDATE app_compras SET estado=?,updated_at=CURRENT_TIMESTAMP WHERE id=?", nextState, purchaseId);
			notify(accountId, nextState, nextState.equals("entrega_pendiente") ? "Entrega pendiente" : "Retiro pendiente",
					"El pago de extras fue aprobado.", "compra", purchaseId);
		}
		audit.record(new AuditEvent("usuario", accountId, success ? "pago.extras_aprobado" : "pago.extras_rechazado",
				"pago", payment.id(), "{}"));
		return payment(payment.id(), false);
	}

	private Payment payFine(Long accountId, Long purchaseId, Long paymentId, String key, PaymentOutcome outcome) {
		Payment replay = replayPayment(accountId, purchaseId, paymentId, key);
		if (replay != null) return replay;
		Purchase purchase = ownedPurchase(accountId, purchaseId, true);
		if (!purchase.state().equals("multa_activa")) {
			throw conflict("La compra no posee multa activa", "INVALID_STATE_TRANSITION");
		}
		FineRow fine = activeFine(purchaseId);
		if (fine == null) throw conflict("La multa no esta disponible", "INVALID_STATE_TRANSITION");
		BigDecimal amount = purchase.amount().add(fine.amount());
		boolean success = paymentSucceeds(accountId, paymentId, purchase.currency(), amount, outcome);
		Payment payment = insertPayment(purchaseId, fine.id(), paymentId, amount, purchase.currency(),
				success ? "aprobado" : "rechazado", key, success ? null : "INSUFFICIENT_FUNDS_OR_LIMIT");
		if (success) {
			consume(paymentId, amount);
			jdbc.update("UPDATE app_multas SET estado='pagada',paid_at=CURRENT_TIMESTAMP WHERE id=?", fine.id());
			jdbc.update("UPDATE app_compras SET estado='pagos_extra_pendientes',updated_at=CURRENT_TIMESTAMP WHERE id=?",
					purchaseId);
			if (pendingFineCount(accountId) == 0) account(accountId).changeState("activa");
			notify(accountId, "multa_pagada", "Multa pagada", "La restriccion por multa fue regularizada.", "multa",
					fine.id());
			audit.record(new AuditEvent("usuario", accountId, "multa.pagada", "multa", fine.id(), "{}"));
		}
		audit.record(new AuditEvent("usuario", accountId, success ? "pago.multa_aprobado" : "pago.multa_rechazado",
				"pago", payment.id(), "{}"));
		return payment(payment.id(), false);
	}

	private boolean paymentSucceeds(Long accountId, Long paymentId, String currency, BigDecimal amount,
			PaymentOutcome outcome) {
		PaymentMethod method = paymentMethod(accountId, paymentId);
		if (!method.currency().equals(currency)) throw unprocessable("Moneda incompatible", "PAYMENT_METHOD_CURRENCY_MISMATCH");
		if (!method.state().equals("verificado") || method.deletedAt() != null) {
			throw forbidden("El medio no esta verificado", "PAYMENT_METHOD_NOT_VERIFIED");
		}
		if (method.verifiedUntil() == null || !method.verifiedUntil().isAfter(OffsetDateTime.now())) {
			throw forbidden("La verificacion del medio vencio", "PAYMENT_METHOD_VERIFICATION_EXPIRED");
		}
		if (outcome == PaymentOutcome.FAILURE) return false;
		if (outcome == PaymentOutcome.SUCCESS) return true;
		if (method.limit() != null && amount.compareTo(method.limit().subtract(method.consumed())) > 0) return false;
		return !method.type().equals("cheque_certificado")
				|| method.guarantee() != null && amount.compareTo(method.guarantee()) <= 0;
	}

	private PaymentMethod paymentMethod(Long accountId, Long paymentId) {
		List<PaymentMethod> values = jdbc.query("""
				SELECT cuenta_id,tipo,moneda,estado,limite_monto,consumo_actual,saldo_garantia,verificado_hasta,deleted_at
				FROM app_medios_pago WHERE id=?
				""", (rs, row) -> new PaymentMethod(rs.getLong("cuenta_id"), rs.getString("tipo"), rs.getString("moneda"),
						rs.getString("estado"), rs.getBigDecimal("limite_monto"), rs.getBigDecimal("consumo_actual"),
						rs.getBigDecimal("saldo_garantia"), rs.getObject("verificado_hasta", OffsetDateTime.class),
						rs.getObject("deleted_at", OffsetDateTime.class)), paymentId);
		if (values.isEmpty()) throw notFound("Medio de pago inexistente");
		PaymentMethod method = values.get(0);
		if (!method.accountId().equals(accountId)) throw forbidden("El medio no pertenece al usuario", "RESOURCE_NOT_OWNED");
		return method;
	}

	private Payment replayPayment(Long accountId, Long purchaseId, Long paymentId, String key) {
		List<ExistingPayment> values = jdbc.query("""
				SELECT p.id,c.cuenta_comprador_id FROM app_pagos p
				LEFT JOIN app_compras c ON c.id=p.compra_id WHERE p.idempotency_key=?
				""", (rs, row) -> new ExistingPayment(rs.getLong("id"), (Long) rs.getObject("cuenta_comprador_id")), key);
		if (values.isEmpty()) return null;
		ExistingPayment existing = values.get(0);
		Payment payment = payment(existing.id(), true);
		if (!accountId.equals(existing.accountId()) || !payment.compraId().equals(purchaseId)
				|| !payment.medioPagoId().equals(paymentId)) {
			throw conflict("Clave de idempotencia reutilizada", "IDEMPOTENCY_CONFLICT");
		}
		return payment;
	}

	private Payment insertPayment(Long purchaseId, Long fineId, Long paymentId, BigDecimal amount, String currency,
			String state, String key, String errorCode) {
		long id = insert("""
				INSERT INTO app_pagos(compra_id,multa_id,medio_pago_id,monto,moneda,estado,referencia_externa,
					idempotency_key,error_codigo,error_detalle)
				VALUES (?,?,?,?,?,?,?, ?,?,?)
				""", statement -> {
			statement.setLong(1, purchaseId);
			if (fineId == null) statement.setNull(2, java.sql.Types.BIGINT); else statement.setLong(2, fineId);
			statement.setLong(3, paymentId);
			statement.setBigDecimal(4, amount);
			statement.setString(5, currency);
			statement.setString(6, state);
			statement.setString(7, "SIM-" + UUID.randomUUID());
			statement.setString(8, key);
			statement.setString(9, errorCode);
			statement.setString(10, errorCode == null ? null : "Falla de cobro simulada o fondos insuficientes.");
		});
		return payment(id, false);
	}

	private Payment payment(Long id, boolean replay) {
		return jdbc.query("""
				SELECT p.id,p.compra_id,p.multa_id,p.medio_pago_id,p.monto,p.moneda,p.estado,p.error_codigo,c.estado compra_estado
				FROM app_pagos p JOIN app_compras c ON c.id=p.compra_id WHERE p.id=?
				""", rs -> {
			if (!rs.next()) throw notFound("Pago inexistente");
			return new Payment(rs.getLong("id"), rs.getLong("compra_id"), (Long) rs.getObject("multa_id"),
					rs.getLong("medio_pago_id"), rs.getBigDecimal("monto"), rs.getString("moneda"),
					rs.getString("estado"), rs.getString("error_codigo"), rs.getString("compra_estado"), replay);
		}, id);
	}

	private Long createFine(Long accountId, Long purchaseId, BigDecimal adjudication, String currency) {
		BigDecimal amount = adjudication.multiply(FINE_RATE).setScale(2, RoundingMode.HALF_UP);
		return insert("""
				INSERT INTO app_multas(cuenta_id,compra_id,monto,moneda,porcentaje,estado,vence_at)
				VALUES (?,?,?,? ,10,'pendiente',?)
				""", statement -> {
			statement.setLong(1, accountId);
			statement.setLong(2, purchaseId);
			statement.setBigDecimal(3, amount);
			statement.setString(4, currency);
			statement.setObject(5, OffsetDateTime.now().plusHours(72));
		});
	}

	private Long insertPurchase(Integer auctionId, Item item, Long accountId, boolean company, Long bidId,
			BigDecimal amount, String currency, Long paymentId, String state) {
		return insert("""
				INSERT INTO app_compras(subasta_id,item_catalogo_id,producto_id,cuenta_comprador_id,comprador_empresa,
					puja_id,monto_adjudicacion,moneda,estado,medio_pago_id)
				VALUES (?,?,?,?,?,?,?,?,?,?)
				""", statement -> {
			statement.setInt(1, auctionId);
			statement.setInt(2, item.id());
			statement.setInt(3, item.productId());
			if (accountId == null) statement.setNull(4, java.sql.Types.BIGINT); else statement.setLong(4, accountId);
			statement.setBoolean(5, company);
			if (bidId == null) statement.setNull(6, java.sql.Types.BIGINT); else statement.setLong(6, bidId);
			statement.setBigDecimal(7, amount);
			statement.setString(8, currency);
			statement.setString(9, state);
			if (paymentId == null) statement.setNull(10, java.sql.Types.BIGINT); else statement.setLong(10, paymentId);
		});
	}

	private void registerLegacySale(Integer auctionId, Item item, WinningBid winning) {
		List<LegacySale> sales = jdbc.query("""
				SELECT p.duenio,c.cliente_id FROM productos p JOIN app_cuentas c ON c.id=?
				WHERE p.identificador=? AND p.duenio IS NOT NULL
				""", (rs, row) -> new LegacySale(rs.getInt("duenio"), rs.getInt("cliente_id")), winning.accountId(),
				item.productId());
		if (!sales.isEmpty()) {
			LegacySale sale = sales.get(0);
			jdbc.update("""
					INSERT INTO "registroDeSubasta"(subasta,duenio,producto,cliente,importe,comision)
					VALUES (?,?,?,?,?,?)
					""", auctionId, sale.ownerId(), item.productId(), sale.clientId(), winning.amount(), item.commission());
		}
	}

	private void consume(Long paymentId, BigDecimal amount) {
		jdbc.update("UPDATE app_medios_pago SET consumo_actual=consumo_actual+?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
				amount, paymentId);
	}

	private void updateConsignment(Integer itemId, String state) {
		jdbc.update("""
				UPDATE app_solicitudes_consignacion SET estado=?,updated_at=CURRENT_TIMESTAMP WHERE item_catalogo_id=?
				""", state, itemId);
	}

	private void notifyOwner(Integer itemId, Long purchaseId) {
		jdbc.query("SELECT cuenta_id FROM app_solicitudes_consignacion WHERE item_catalogo_id=?", rs -> {
			notify(rs.getLong(1), "lote_cerrado", "Lote cerrado", "El lote consignado fue cerrado.", "compra", purchaseId);
		}, itemId);
	}

	private void notify(Long accountId, String type, String title, String description, String referenceType,
			Long referenceId) {
		jdbc.update("""
				INSERT INTO app_notificaciones(cuenta_id,tipo,titulo,descripcion,referencia_tipo,referencia_id)
				VALUES (?,?,?,?,?,?)
				""", accountId, type, title, description, referenceType, referenceId);
		mail.critical(accountId, type);
	}

	private void completeDelivery(Long purchaseId, String type) {
		Purchase purchase = lockPurchase(purchaseId);
		String requiredState = type.equals("envio") ? "entrega_pendiente" : "retiro_pendiente";
		if (!purchase.state().equals(requiredState)) throw conflict("Estado incompatible", "INVALID_STATE_TRANSITION");
		Integer changed = jdbc.update("""
				UPDATE app_entregas SET estado='completada',updated_at=CURRENT_TIMESTAMP WHERE compra_id=? AND tipo=?
				""", purchaseId, type);
		if (changed == 0) throw conflict("Entrega inexistente", "INVALID_STATE_TRANSITION");
		jdbc.update("UPDATE app_compras SET estado='completada',updated_at=CURRENT_TIMESTAMP WHERE id=?", purchaseId);
		if (purchase.accountId() != null) {
			notify(purchase.accountId(), "compra_completada", "Compra completada",
					"El bien ya esta en posesion del comprador.", "compra", purchaseId);
		}
		audit.record(new AuditEvent("sistema", null, "compra.completada", "compra", purchaseId,
				"{\"tipo\":\"" + type + "\"}"));
	}

	private Long shippingAddress(Long accountId, Long requestedId) {
		return (requestedId == null
				? addresses.findFirstByCuentaIdAndPrincipalTrueAndDeletedAtIsNull(accountId)
				: addresses.findByIdAndCuentaIdAndDeletedAtIsNull(requestedId, accountId))
				.map(com.example.quickbid.quickbid.entity.app.DireccionEnvio::getId)
				.orElseThrow(() -> unprocessable("Direccion de envio requerida", "SHIPPING_ADDRESS_REQUIRED"));
	}

	private Detail internalDetail(Long purchaseId) {
		return detail(lockPurchase(purchaseId));
	}

	private Detail detail(Purchase purchase) {
		return new Detail(purchase.id(), purchase.auctionId(), purchase.itemId(), purchase.productId(), purchase.bidId(),
				purchase.amount(), purchase.currency(), purchase.state(), purchase.paymentId(), delivery(purchase.id()),
				fine(purchase.id()), commission(purchase.itemId()), purchase.createdAt());
	}

	private PurchaseDtos.Delivery delivery(Long purchaseId) {
		List<PurchaseDtos.Delivery> values = jdbc.query("""
				SELECT id,tipo,direccion_envio_id,costo_envio,estado,perdio_cobertura_seguro
				FROM app_entregas WHERE compra_id=?
				""", (rs, row) -> new PurchaseDtos.Delivery(rs.getLong("id"), rs.getString("tipo"),
						(Long) rs.getObject("direccion_envio_id"), rs.getBigDecimal("costo_envio"), rs.getString("estado"),
						rs.getBoolean("perdio_cobertura_seguro")), purchaseId);
		return values.isEmpty() ? null : values.get(0);
	}

	private Fine fine(Long purchaseId) {
		List<Fine> values = jdbc.query("""
				SELECT id,monto,moneda,estado,vence_at,paid_at FROM app_multas WHERE compra_id=? ORDER BY id DESC LIMIT 1
				""", (rs, row) -> new Fine(rs.getLong("id"), rs.getBigDecimal("monto"), rs.getString("moneda"),
						rs.getString("estado"), rs.getObject("vence_at", OffsetDateTime.class),
						rs.getObject("paid_at", OffsetDateTime.class)), purchaseId);
		return values.isEmpty() ? null : values.get(0);
	}

	private Purchase ownedPurchase(Long accountId, Long purchaseId, boolean lock) {
		Purchase purchase = lock ? lockPurchase(purchaseId) : purchase(purchaseId);
		if (purchase.accountId() == null || !purchase.accountId().equals(accountId)) {
			throw forbidden("La compra no pertenece al usuario", "RESOURCE_NOT_OWNED");
		}
		return purchase;
	}

	private Purchase lockPurchase(Long purchaseId) {
		return purchase(purchaseId, " FOR UPDATE");
	}

	private Purchase purchase(Long purchaseId) {
		return purchase(purchaseId, "");
	}

	private Purchase purchase(Long purchaseId, String suffix) {
		List<Purchase> values = jdbc.query("""
				SELECT id,subasta_id,item_catalogo_id,producto_id,cuenta_comprador_id,comprador_empresa,puja_id,
				       monto_adjudicacion,moneda,estado,medio_pago_id,created_at
				FROM app_compras WHERE id=?
				""" + suffix, (rs, row) -> new Purchase(rs.getLong("id"), rs.getInt("subasta_id"),
						rs.getInt("item_catalogo_id"), rs.getInt("producto_id"), (Long) rs.getObject("cuenta_comprador_id"),
						rs.getBoolean("comprador_empresa"), (Long) rs.getObject("puja_id"),
						rs.getBigDecimal("monto_adjudicacion"), rs.getString("moneda"), rs.getString("estado"),
						(Long) rs.getObject("medio_pago_id"), rs.getObject("created_at", OffsetDateTime.class)),
				purchaseId);
		if (values.isEmpty()) throw notFound("Compra inexistente");
		return values.get(0);
	}

	private FineRow activeFine(Long purchaseId) {
		List<FineRow> values = jdbc.query("""
				SELECT id,cuenta_id,monto,estado,vence_at FROM app_multas WHERE compra_id=? AND estado='pendiente'
				ORDER BY id DESC LIMIT 1
				""", (rs, row) -> fineRow(rs), purchaseId);
		return values.isEmpty() ? null : values.get(0);
	}

	private FineRow lockFine(Long fineId) {
		List<FineRow> values = jdbc.query("""
				SELECT id,cuenta_id,monto,estado,vence_at FROM app_multas WHERE id=? FOR UPDATE
				""", (rs, row) -> fineRow(rs), fineId);
		if (values.isEmpty()) throw notFound("Multa inexistente");
		return values.get(0);
	}

	private FineRow fineRow(java.sql.ResultSet rs) throws java.sql.SQLException {
		return new FineRow(rs.getLong("id"), rs.getLong("cuenta_id"), rs.getBigDecimal("monto"), rs.getString("estado"),
				rs.getObject("vence_at", OffsetDateTime.class));
	}

	private int pendingFineCount(Long accountId) {
		return jdbc.queryForObject("SELECT COUNT(*) FROM app_multas WHERE cuenta_id=? AND estado='pendiente'",
				Integer.class, accountId);
	}

	private LiveState lockLiveState(Integer auctionId) {
		List<LiveState> values = jdbc.query("""
				SELECT item_catalogo_activo_id,version FROM app_subasta_estado_vivo WHERE subasta_id=? FOR UPDATE
				""", (rs, row) -> new LiveState((Integer) rs.getObject("item_catalogo_activo_id"), rs.getLong("version")),
				auctionId);
		if (values.isEmpty()) throw notFound("Estado vivo inexistente");
		return values.get(0);
	}

	private Auction auction(Integer id) {
		List<Auction> values = jdbc.query("SELECT moneda,estado_operativo FROM app_subasta_ext WHERE subasta_id=?",
				(rs, row) -> new Auction(rs.getString("moneda"), rs.getString("estado_operativo")), id);
		if (values.isEmpty()) throw notFound("Subasta inexistente");
		return values.get(0);
	}

	private Item item(Integer id) {
		List<Item> values = jdbc.query("""
				SELECT identificador,producto,"precioBase",comision FROM "itemsCatalogo" WHERE identificador=?
				""", (rs, row) -> new Item(rs.getInt("identificador"), rs.getInt("producto"),
						rs.getBigDecimal("precioBase"), rs.getBigDecimal("comision")), id);
		if (values.isEmpty()) throw notFound("Item inexistente");
		return values.get(0);
	}

	private BigDecimal commission(Integer itemId) {
		return item(itemId).commission();
	}

	private boolean purchaseForItem(Integer itemId) {
		return jdbc.queryForObject("SELECT COUNT(*) FROM app_compras WHERE item_catalogo_id=?", Integer.class, itemId) > 0;
	}

	private WinningBid winningBid(Integer auctionId, Integer itemId) {
		List<WinningBid> values = jdbc.query("""
				SELECT id,pujo_legacy_id,cuenta_id,medio_pago_id,monto FROM app_pujas_live
				WHERE subasta_id=? AND item_catalogo_id=? AND estado='aceptada'
				ORDER BY monto DESC,secuencia DESC LIMIT 1
				""", (rs, row) -> new WinningBid(rs.getLong("id"), rs.getInt("pujo_legacy_id"), rs.getLong("cuenta_id"),
						rs.getLong("medio_pago_id"), rs.getBigDecimal("monto")), auctionId, itemId);
		return values.isEmpty() ? null : values.get(0);
	}

	private CuentaApp account(Long accountId) {
		return accounts.findById(accountId).orElseThrow(() -> notFound("Cuenta inexistente"));
	}

	private long insert(String sql, SqlBinder binder) {
		GeneratedKeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(sql, new String[] { "id" });
			binder.bind(statement);
			return statement;
		}, keys);
		return keys.getKey().longValue();
	}

	private String normalizedKey(String key) {
		return key == null || key.isBlank() ? UUID.randomUUID().toString() : key.trim();
	}

	private void checkPage(int page, int size) {
		if (page < 0 || size < 1 || size > 100) throw new BusinessException(HttpStatus.BAD_REQUEST, "Paginacion invalida", "INVALID_PAGE");
	}

	private BusinessException forbidden(String message, String code) { return new BusinessException(HttpStatus.FORBIDDEN, message, code); }
	private BusinessException conflict(String message, String code) { return new BusinessException(HttpStatus.CONFLICT, message, code); }
	private BusinessException unprocessable(String message, String code) { return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, message, code); }
	private BusinessException notFound(String message) { return new BusinessException(HttpStatus.NOT_FOUND, message, "RESOURCE_NOT_FOUND"); }

	public enum PaymentOutcome {
		AUTO, SUCCESS, FAILURE
	}

	@FunctionalInterface
	private interface SqlBinder {
		void bind(PreparedStatement statement) throws java.sql.SQLException;
	}

	private record LiveState(Integer itemId, Long version) {
	}

	private record Auction(String currency, String state) {
	}

	private record Item(Integer id, Integer productId, BigDecimal basePrice, BigDecimal commission) {
	}

	private record WinningBid(Long id, Integer legacyBidId, Long accountId, Long paymentId, BigDecimal amount) {
	}

	private record LegacySale(Integer ownerId, Integer clientId) {
	}

	private record Purchase(Long id, Integer auctionId, Integer itemId, Integer productId, Long accountId,
			Boolean company, Long bidId, BigDecimal amount, String currency, String state, Long paymentId,
			OffsetDateTime createdAt) {
	}

	private record FineRow(Long id, Long accountId, BigDecimal amount, String state, OffsetDateTime expiresAt) {
	}

	private record PaymentMethod(Long accountId, String type, String currency, String state, BigDecimal limit,
			BigDecimal consumed, BigDecimal guarantee, OffsetDateTime verifiedUntil, OffsetDateTime deletedAt) {
	}

	private record ExistingPayment(Long id, Long accountId) {
	}
}
