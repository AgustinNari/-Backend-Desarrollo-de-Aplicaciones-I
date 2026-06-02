package com.example.quickbid.quickbid.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quickbid.quickbid.audit.AuditEvent;
import com.example.quickbid.quickbid.audit.AuditService;
import com.example.quickbid.quickbid.dto.admin.AdminDtos;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Account;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Agreement;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AssignAuction;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Auction;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AuctionCreate;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AuctionUpdate;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.CatalogItem;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Consignment;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.PaymentMethod;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.PurchaseSimulation;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Status;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Liquidation;
import com.example.quickbid.quickbid.dto.response.MedioPagoResponse;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Detail;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Payment;
import com.example.quickbid.quickbid.entity.app.CuentaApp;
import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.repository.app.AdminQueryRepository;
import com.example.quickbid.quickbid.repository.app.AuctionQueryRepository;
import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;
import com.example.quickbid.quickbid.repository.app.SolicitudRegistroRepository;
import com.example.quickbid.quickbid.repository.legacy.ClienteRepository;
import com.example.quickbid.quickbid.service.PurchaseService.PaymentOutcome;

@Service
public class AdminService {
	private static final Set<String> CATEGORIES = Set.of("comun", "especial", "plata", "oro", "platino");
	private final JdbcTemplate jdbc;
	private final SolicitudRegistroRepository registrations;
	private final RegistrationApprovalService registrationApproval;
	private final CuentaAppRepository accounts;
	private final ClienteRepository clients;
	private final CategoriaService categories;
	private final MedioPagoService paymentMethods;
	private final PurchaseService purchases;
	private final ConsignmentService consignments;
	private final AuditService audit;
	private final Environment environment;
	private final MailNotificationService mail;
	private final AdminQueryRepository queries;
	private final AuctionQueryRepository auctionQueries;

	public AdminService(JdbcTemplate jdbc, SolicitudRegistroRepository registrations,
			RegistrationApprovalService registrationApproval, CuentaAppRepository accounts, ClienteRepository clients,
			CategoriaService categories, MedioPagoService paymentMethods, PurchaseService purchases,
			ConsignmentService consignments, AuditService audit, Environment environment, MailNotificationService mail,
			AdminQueryRepository queries, AuctionQueryRepository auctionQueries) {
		this.jdbc = jdbc;
		this.registrations = registrations;
		this.registrationApproval = registrationApproval;
		this.accounts = accounts;
		this.clients = clients;
		this.categories = categories;
		this.paymentMethods = paymentMethods;
		this.purchases = purchases;
		this.consignments = consignments;
		this.audit = audit;
		this.environment = environment;
		this.mail = mail;
		this.queries = queries;
		this.auctionQueries = auctionQueries;
	}

	@Transactional(readOnly = true)
	public List<AdminDtos.Registration> registrations() {
		return registrations.findAll().stream().map(this::registration).toList();
	}

	@Transactional(readOnly = true)
	public AdminDtos.Registration registration(Long id) {
		return registrations.findById(id).map(this::registration).orElseThrow(() -> notFound("Solicitud inexistente"));
	}

	public void approveRegistration(Long id, String document, String category, Integer employeeId) {
		registrationApproval.approve(id, document, employeeId, category);
	}

	public void rejectRegistration(Long id, String reason, Integer employeeId) {
		registrationApproval.reject(id, reason, employeeId);
	}

	@Transactional
	public Account block(Long id, Integer employeeId) {
		CuentaApp account = account(id);
		account.changeState("deshabilitada_admin");
		audit(employeeId, "usuario.bloqueado_admin", "cuenta", id);
		return account(account);
	}

	@Transactional
	public Account unblock(Long id, Integer employeeId) {
		CuentaApp account = account(id);
		if (!Set.of("deshabilitada_admin", "bloqueada_permanente").contains(account.getEstado())) {
			throw conflict("La cuenta no esta bloqueada", "INVALID_STATE_TRANSITION");
		}
		account.changeState("activa");
		audit(employeeId, "usuario.desbloqueado_admin", "cuenta", id);
		return account(account);
	}

	@Transactional
	public Account points(Long id, Integer delta, Integer employeeId) {
		CuentaApp account = account(id);
		if (account.getPuntos() + delta < 0) throw bad("Los puntos no pueden quedar negativos", "INVALID_POINTS");
		categories.addPoints(account, delta, "ajuste_admin", "cuenta", id);
		audit(employeeId, "usuario.puntos_ajustados", "cuenta", id);
		return account(account);
	}

	@Transactional
	public Account category(Long id, String category, Integer employeeId) {
		CuentaApp account = account(id);
		String value = category.toLowerCase();
		if (!CATEGORIES.contains(value)) throw bad("Categoria invalida", "INVALID_CATEGORY");
		account.updateCategory(value);
		clients.findById(account.getClienteId()).orElseThrow(() -> notFound("Cliente inexistente")).updateCategory(value);
		audit(employeeId, "usuario.categoria_forzada", "cuenta", id);
		return account(account);
	}

	@Transactional(readOnly = true)
	public List<PaymentMethod> paymentMethods(String state) {
		return queries.findPaymentMethods(state);
	}

	public MedioPagoResponse verifyPaymentMethod(Long id, Integer employeeId) {
		return paymentMethods.verify(id, employeeId);
	}

	public MedioPagoResponse rejectPaymentMethod(Long id, String reason, Integer employeeId) {
		return paymentMethods.reject(id, employeeId, reason);
	}

	@Transactional
	public Auction createAuction(AuctionCreate request, Integer employeeId) {
		validateAuction(request.fecha(), request.categoria(), request.moneda());
		long id = insert("""
				INSERT INTO subastas(fecha,hora,estado,ubicacion,categoria) VALUES (?,?,'abierta',?,?)
				""", "identificador", statement -> {
			statement.setObject(1, request.fecha());
			statement.setObject(2, request.hora());
			statement.setString(3, request.ubicacion());
			statement.setString(4, request.categoria().toLowerCase());
		});
		jdbc.update("""
				INSERT INTO app_subasta_ext(subasta_id,titulo,descripcion,moneda,segmento,estado_operativo,
					permite_inscripcion_online) VALUES (?,?,?,?,?,'programada',?)
				""", id, request.titulo(), request.descripcion(), request.moneda().toUpperCase(), request.segmento(),
				request.permiteInscripcionOnline() == null || request.permiteInscripcionOnline());
		jdbc.update("INSERT INTO app_subasta_estado_vivo(subasta_id,version,usuarios_conectados) VALUES (?,0,0)", id);
		audit(employeeId, "subasta.creada", "subasta", id);
		return auction(Math.toIntExact(id));
	}

	@Transactional
	public Auction updateAuction(Integer id, AuctionUpdate request, Integer employeeId) {
		validateAuction(request.fecha(), request.categoria(), null);
		if (!auctionQueries.existsAuction(id)) throw notFound("Subasta inexistente");
		jdbc.update("UPDATE subastas SET fecha=?,hora=?,ubicacion=?,categoria=? WHERE identificador=?", request.fecha(),
				request.hora(), request.ubicacion(), request.categoria().toLowerCase(), id);
		jdbc.update("""
				UPDATE app_subasta_ext SET titulo=?,descripcion=?,segmento=?,permite_inscripcion_online=?,
					updated_at=CURRENT_TIMESTAMP WHERE subasta_id=?
				""", request.titulo(), request.descripcion(), request.segmento(),
				request.permiteInscripcionOnline() == null || request.permiteInscripcionOnline(), id);
		audit(employeeId, "subasta.actualizada", "subasta", id.longValue());
		return auction(id);
	}

	@Transactional
	public Auction openAuction(Integer id, Integer employeeId) {
		requireAuction(id);
		boolean becameLive = auctionQueries.isNotLive(id);
		jdbc.update("UPDATE subastas SET estado='abierta' WHERE identificador=?", id);
		jdbc.update("UPDATE app_subasta_ext SET estado_operativo='en_vivo',updated_at=CURRENT_TIMESTAMP WHERE subasta_id=?", id);
		if (becameLive) {
			auctionQueries.findNotificationRecipientsForAuctionStart(id).forEach(accountId -> {
				jdbc.update("""
						INSERT INTO app_notificaciones(cuenta_id,tipo,titulo,descripcion,referencia_tipo,referencia_id)
						VALUES (?,'subasta_inscripta_proxima_inicio','Subasta disponible en vivo',
							'Una subasta en la que manifestaste interes ya esta disponible en vivo.','subasta',?)
						""", accountId, id);
				mail.critical(accountId, "subasta_inscripta_proxima_inicio");
			});
		}
		audit(employeeId, "subasta.abierta", "subasta", id.longValue());
		return auction(id);
	}

	public Status closeAuction(Integer id, Integer employeeId) {
		purchases.closeAuction(id);
		audit(employeeId, "subasta.cerrada_admin", "subasta", id.longValue());
		return new Status("cerrada", "Subasta cerrada");
	}

	@Transactional
	public Status setActiveItem(Integer id, Integer itemId, Integer employeeId) {
		requireAuction(id);
		if (!auctionQueries.existsAuctionItem(id, itemId)) throw bad("El item no pertenece a la subasta", "INVALID_AUCTION_ITEM");
		jdbc.update("""
				UPDATE app_subasta_estado_vivo SET item_catalogo_activo_id=?,version=version+1,
					lote_iniciado_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE subasta_id=?
				""", itemId, id);
		audit(employeeId, "subasta.item_activo_actualizado", "subasta", id.longValue());
		return new Status("item_activo", "Item activo " + itemId);
	}

	public Detail closeLot(Integer id, PaymentOutcome outcome, Integer employeeId) {
		Detail detail = purchases.closeLot(id, outcome);
		audit(employeeId, "subasta.lote_cerrado_admin", "compra", detail.id());
		return detail;
	}

	@Transactional
	public Integer addCatalogItem(Integer auctionId, CatalogItem request, Integer employeeId) {
		if (!auctionQueries.existsCatalogForAuction(auctionId, request.catalogoId())) {
			throw bad("Catalogo incompatible", "INVALID_CATALOG");
		}
		if (!auctionQueries.existsProduct(request.productoId())) {
			throw notFound("Producto inexistente");
		}
		if (request.precioBase().signum() <= 0 || request.comision().signum() <= 0) {
			throw bad("Importes invalidos", "INVALID_AMOUNT");
		}
		long id = insert("""
				INSERT INTO "itemsCatalogo"(catalogo,producto,"precioBase",comision,subastado) VALUES (?,?,?,?,'no')
				""", "identificador", statement -> {
			statement.setInt(1, request.catalogoId());
			statement.setInt(2, request.productoId());
			statement.setBigDecimal(3, request.precioBase());
			statement.setBigDecimal(4, request.comision());
		});
		audit(employeeId, "catalogo.item_agregado", "item_catalogo", id);
		return Math.toIntExact(id);
	}

	public Payment simulatePayment(Long purchaseId, PurchaseSimulation request, boolean success, Integer employeeId) {
		String type = request.tipo() == null ? "extras" : request.tipo().toLowerCase();
		Payment payment = type.equals("multa")
				? success ? purchases.simulateSuccessfulFine(request.cuentaId(), purchaseId, request.medioPagoId())
						: purchases.simulateFailedFine(request.cuentaId(), purchaseId, request.medioPagoId())
				: success ? purchases.simulateSuccessfulExtras(request.cuentaId(), purchaseId, request.medioPagoId())
						: purchases.simulateFailedExtras(request.cuentaId(), purchaseId, request.medioPagoId());
		audit(employeeId, success ? "pago.simulacion_exitosa" : "pago.simulacion_fallida", "pago", payment.id());
		return payment;
	}

	public Status expireFine(Long id, Integer employeeId) {
		purchases.expireFine(id, true);
		audit(employeeId, "multa.vencida_admin", "multa", id);
		return new Status("vencida", "Multa vencida");
	}

	public Status markFinePaid(Long id, Integer employeeId) {
		purchases.markFinePaid(id);
		audit(employeeId, "multa.pagada_admin", "multa", id);
		return new Status("pagada", "Multa marcada como pagada");
	}

	@Transactional(readOnly = true)
	public List<Consignment> consignments() {
		return queries.findConsignments();
	}

	public void requestDocuments(Long id, Integer employeeId) { consignments.requestOriginDocuments(id, employeeId); }
	public void rejectConsignment(Long id, String reason, Integer employeeId) { consignments.rejectDigitalReview(id, employeeId, reason); }
	public void approveDigitalReview(Long id, Integer employeeId) { consignments.approveDigitalReview(id, employeeId); }
	public void reviewDocuments(Long id, boolean approved, String reason, Integer employeeId) { consignments.reviewOriginDocuments(id, employeeId, approved, reason); }
	public void markPhysicalReception(Long id, Integer employeeId) { consignments.markPhysicalReception(id, employeeId); }
	public void approvePhysicalReview(Long id, Integer employeeId) { consignments.approvePhysicalReview(id, employeeId); }
	public void rejectPhysicalReview(Long id, String reason, Integer employeeId) { consignments.rejectPhysicalReview(id, employeeId, reason); }
	public Integer verifyOwner(Long accountId, boolean financial, boolean judicial, int risk, Integer employeeId) { return consignments.verifyConsignor(accountId, employeeId, financial, judicial, risk); }
	public void proposeAgreement(Long id, Agreement request, Integer employeeId) { consignments.proposeAgreement(id, employeeId, request.valorBase(), request.moneda(), request.comisionCompradorPct(), request.comisionVendedorPct(), request.condiciones()); }
	public Integer assignAuction(Long id, AssignAuction request, Integer employeeId) { return consignments.assignAuctionAndInsurance(id, employeeId, request.subastaId(), request.catalogoId(), request.polizaCombinada()); }
	public Liquidation liquidate(Long id, Long paymentMethodId, Integer employeeId) { return consignments.liquidate(id, employeeId, paymentMethodId); }
	public void markReturnIncomplete(Long id, Integer employeeId) { consignments.markReturnIncomplete(id, employeeId); }

	public Status seed(String scope, Integer employeeId) {
		requireDevOrTest();
		audit(employeeId, "seed." + scope + ".solicitado", "seed", null);
		return new Status("sin_cambios", "Flyway ya carga el seed " + scope + " de forma idempotente por base");
	}

	public Status resetDemo(Integer employeeId) {
		requireDevOrTest();
		audit(employeeId, "seed.reset_demo_solicitado", "seed", null);
		return new Status("sin_cambios", "Reset destructivo deshabilitado; recrea la base dev para restaurar V1-V7");
	}

	private Auction auction(Integer id) {
		return queries.findAuction(id).orElseThrow(() -> notFound("Subasta inexistente"));
	}

	private void requireAuction(Integer id) { auction(id); }

	private void validateAuction(LocalDate date, String category, String currency) {
		if (!date.isAfter(LocalDate.now().plusDays(10))) throw bad("La fecha debe superar diez dias", "INVALID_AUCTION_DATE");
		if (!CATEGORIES.contains(category.toLowerCase())) throw bad("Categoria invalida", "INVALID_CATEGORY");
		if (currency != null && !Set.of("ARS", "USD").contains(currency.toUpperCase())) throw bad("Moneda invalida", "INVALID_CURRENCY");
	}

	private void requireDevOrTest() {
		if (Set.of(environment.getActiveProfiles()).stream().noneMatch(profile -> profile.equals("dev") || profile.equals("test"))) {
			throw forbidden("Endpoint disponible solo en dev/test", "DEV_ONLY_ENDPOINT");
		}
	}

	private CuentaApp account(Long id) { return accounts.findById(id).orElseThrow(() -> notFound("Cuenta inexistente")); }
	private AdminDtos.Registration registration(com.example.quickbid.quickbid.entity.app.SolicitudRegistro value) { return new AdminDtos.Registration(value.getId(), value.getEmail(), value.getNombre(), value.getApellido(), value.getEstado(), value.getMotivoRechazo(), value.getPersonaId(), value.getClienteId()); }
	private Account account(CuentaApp value) { return new Account(value.getId(), value.getEmail(), value.getEstado(), value.getPuntos(), value.getCategoriaCalculada()); }
	private void audit(Integer employeeId, String action, String entity, Long id) { audit.record(new AuditEvent("admin", employeeId.longValue(), action, entity, id, "{}")); }

	private long insert(String sql, String column, SqlBinder binder) {
		GeneratedKeyHolder keys = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(sql, new String[] { column });
			binder.bind(statement);
			return statement;
		}, keys);
		return keys.getKey().longValue();
	}

	private BusinessException bad(String message, String code) { return new BusinessException(HttpStatus.BAD_REQUEST, message, code); }
	private BusinessException forbidden(String message, String code) { return new BusinessException(HttpStatus.FORBIDDEN, message, code); }
	private BusinessException conflict(String message, String code) { return new BusinessException(HttpStatus.CONFLICT, message, code); }
	private BusinessException notFound(String message) { return new BusinessException(HttpStatus.NOT_FOUND, message, "RESOURCE_NOT_FOUND"); }

	@FunctionalInterface
	private interface SqlBinder { void bind(PreparedStatement statement) throws java.sql.SQLException; }
}
