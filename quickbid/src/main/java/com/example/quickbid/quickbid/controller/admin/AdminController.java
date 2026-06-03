package com.example.quickbid.quickbid.controller.admin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quickbid.quickbid.dto.admin.AdminDtos;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Account;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.ActiveItem;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Agreement;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.ApproveRegistration;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AssignAuction;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Auction;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AuctionCreate;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.AuctionUpdate;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.CatalogItem;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Category;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Consignment;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Liquidate;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.OwnerVerification;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.PaymentMethod;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Points;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.PurchaseSimulation;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Reason;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.ReviewDecision;
import com.example.quickbid.quickbid.dto.admin.AdminDtos.Status;
import com.example.quickbid.quickbid.dto.response.ApiResponse;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Liquidation;
import com.example.quickbid.quickbid.dto.response.MedioPagoResponse;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Detail;
import com.example.quickbid.quickbid.dto.response.PurchaseDtos.Payment;
import com.example.quickbid.quickbid.security.AdminPrincipal;
import com.example.quickbid.quickbid.service.AdminService;
import com.example.quickbid.quickbid.service.PurchaseService.PaymentOutcome;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final AdminService admin;

	public AdminController(AdminService admin) {
		this.admin = admin;
	}

	@GetMapping("/solicitudes-registro")
	public ApiResponse<List<AdminDtos.Registration>> registrations() {
		return ApiResponse.success(admin.registrations(), "Solicitudes de registro");
	}

	@GetMapping("/solicitudes-registro/{id}")
	public ApiResponse<AdminDtos.Registration> registration(@PathVariable Long id) {
		return ApiResponse.success(admin.registration(id), "Solicitud de registro");
	}

	@PostMapping("/solicitudes-registro/{id}/aprobar")
	public ApiResponse<AdminDtos.Registration> approveRegistration(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody ApproveRegistration request) {
		admin.approveRegistration(id, request.documento(), request.categoriaInicial(), employeeId(authentication));
		return ApiResponse.success(admin.registration(id), "Registro aprobado");
	}

	@PostMapping("/solicitudes-registro/{id}/rechazar")
	public ApiResponse<AdminDtos.Registration> rejectRegistration(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Reason request) {
		admin.rejectRegistration(id, request.motivo(), employeeId(authentication));
		return ApiResponse.success(admin.registration(id), "Registro rechazado");
	}

	@PostMapping("/usuarios/{id}/bloquear")
	public ApiResponse<Account> block(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(admin.block(id, employeeId(authentication)), "Usuario bloqueado");
	}

	@PostMapping("/usuarios/{id}/desbloquear")
	public ApiResponse<Account> unblock(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(admin.unblock(id, employeeId(authentication)), "Usuario desbloqueado");
	}

	@PatchMapping("/usuarios/{id}/puntos")
	public ApiResponse<Account> points(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Points request) {
		return ApiResponse.success(admin.points(id, request.delta(), employeeId(authentication)), "Puntos ajustados");
	}

	@PatchMapping("/usuarios/{id}/categoria")
	public ApiResponse<Account> category(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Category request) {
		return ApiResponse.success(admin.category(id, request.categoria(), employeeId(authentication)),
				"Categoria actualizada");
	}

	@GetMapping("/medios-pago")
	public ApiResponse<List<PaymentMethod>> paymentMethods(@RequestParam(required = false) String estado) {
		return ApiResponse.success(admin.paymentMethods(estado), "Medios de pago");
	}

	@PostMapping("/medios-pago/{id}/verificar")
	public ApiResponse<MedioPagoResponse> verifyPaymentMethod(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(admin.verifyPaymentMethod(id, employeeId(authentication)), "Medio verificado");
	}

	@PostMapping("/medios-pago/{id}/rechazar")
	public ApiResponse<MedioPagoResponse> rejectPaymentMethod(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Reason request) {
		return ApiResponse.success(admin.rejectPaymentMethod(id, request.motivo(), employeeId(authentication)),
				"Medio rechazado");
	}

	@PostMapping("/subastas")
	public ApiResponse<Auction> createAuction(Authentication authentication, @Valid @RequestBody AuctionCreate request) {
		return ApiResponse.success(admin.createAuction(request, employeeId(authentication)), "Subasta creada");
	}

	@PatchMapping("/subastas/{id}")
	public ApiResponse<Auction> updateAuction(Authentication authentication, @PathVariable Integer id,
			@Valid @RequestBody AuctionUpdate request) {
		return ApiResponse.success(admin.updateAuction(id, request, employeeId(authentication)), "Subasta actualizada");
	}

	@PostMapping("/subastas/{id}/abrir")
	public ApiResponse<Auction> openAuction(Authentication authentication, @PathVariable Integer id) {
		return ApiResponse.success(admin.openAuction(id, employeeId(authentication)), "Subasta abierta");
	}

	@PostMapping("/subastas/{id}/cerrar")
	public ApiResponse<Status> closeAuction(Authentication authentication, @PathVariable Integer id) {
		return ApiResponse.success(admin.closeAuction(id, employeeId(authentication)), "Subasta cerrada");
	}

	@PostMapping("/subastas/{id}/item-activo")
	public ApiResponse<Status> activeItem(Authentication authentication, @PathVariable Integer id,
			@Valid @RequestBody ActiveItem request) {
		return ApiResponse.success(admin.setActiveItem(id, request.itemCatalogoId(), employeeId(authentication)),
				"Item activo actualizado");
	}

	@PostMapping("/subastas/{id}/cerrar-lote")
	public ApiResponse<Detail> closeLot(Authentication authentication, @PathVariable Integer id,
			@RequestParam(defaultValue = "AUTO") PaymentOutcome resultado) {
		return ApiResponse.success(admin.closeLot(id, resultado, employeeId(authentication)), "Lote cerrado");
	}

	@PostMapping("/subastas/{id}/catalogo/items")
	public ApiResponse<Integer> addCatalogItem(Authentication authentication, @PathVariable Integer id,
			@Valid @RequestBody CatalogItem request) {
		return ApiResponse.success(admin.addCatalogItem(id, request, employeeId(authentication)), "Item agregado");
	}

	@PostMapping("/compras/{id}/simular-pago-exitoso")
	public ApiResponse<Payment> successfulPayment(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody PurchaseSimulation request) {
		return ApiResponse.success(admin.simulatePayment(id, request, true, employeeId(authentication)),
				"Pago exitoso simulado");
	}

	@PostMapping("/compras/{id}/simular-falla-pago")
	public ApiResponse<Payment> failedPayment(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody PurchaseSimulation request) {
		return ApiResponse.success(admin.simulatePayment(id, request, false, employeeId(authentication)),
				"Falla de pago simulada");
	}

	@PostMapping("/multas/{id}/vencer")
	public ApiResponse<Status> expireFine(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(admin.expireFine(id, employeeId(authentication)), "Multa vencida");
	}

	@PostMapping("/multas/{id}/marcar-pagada")
	public ApiResponse<Status> markFinePaid(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(admin.markFinePaid(id, employeeId(authentication)), "Multa pagada");
	}

	@GetMapping("/consignaciones")
	public ApiResponse<List<Consignment>> consignments() {
		return ApiResponse.success(admin.consignments(), "Consignaciones");
	}

	@PostMapping("/consignaciones/{id}/pedir-documentacion")
	public ApiResponse<Void> requestDocuments(Authentication authentication, @PathVariable Long id) {
		admin.requestDocuments(id, employeeId(authentication));
		return ApiResponse.success(null, "Documentacion solicitada");
	}

	@PostMapping("/consignaciones/{id}/rechazar")
	public ApiResponse<Void> rejectConsignment(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Reason request) {
		admin.rejectConsignment(id, request.motivo(), employeeId(authentication));
		return ApiResponse.success(null, "Consignacion rechazada");
	}

	@PostMapping("/consignaciones/{id}/aprobar-revision-digital")
	public ApiResponse<Void> approveDigitalReview(Authentication authentication, @PathVariable Long id) {
		admin.approveDigitalReview(id, employeeId(authentication));
		return ApiResponse.success(null, "Revision digital aprobada");
	}

	@PostMapping("/consignaciones/{id}/revisar-documentacion")
	public ApiResponse<Void> reviewDocuments(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody ReviewDecision request) {
		admin.reviewDocuments(id, request.aprobada(), request.motivo(), employeeId(authentication));
		return ApiResponse.success(null, "Documentacion revisada");
	}

	@PostMapping("/consignaciones/{id}/marcar-recibida-fisicamente")
	public ApiResponse<Void> physicalReception(Authentication authentication, @PathVariable Long id) {
		admin.markPhysicalReception(id, employeeId(authentication));
		return ApiResponse.success(null, "Recepcion fisica registrada");
	}

	@PostMapping("/consignaciones/{id}/aprobar-revision-fisica")
	public ApiResponse<Void> approvePhysicalReview(Authentication authentication, @PathVariable Long id) {
		admin.approvePhysicalReview(id, employeeId(authentication));
		return ApiResponse.success(null, "Revision fisica aprobada");
	}

	@PostMapping("/consignaciones/{id}/rechazar-revision-fisica")
	public ApiResponse<Void> rejectPhysicalReview(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Reason request) {
		admin.rejectPhysicalReview(id, request.motivo(), employeeId(authentication));
		return ApiResponse.success(null, "Revision fisica rechazada");
	}

	@PostMapping("/consignaciones/{id}/marcar-devolucion-incompleta")
	public ApiResponse<Void> incompleteReturn(Authentication authentication, @PathVariable Long id) {
		admin.markReturnIncomplete(id, employeeId(authentication));
		return ApiResponse.success(null, "Devolucion marcada incompleta");
	}

	@PostMapping("/consignadores/{cuentaId}/verificar-duenio")
	public ApiResponse<Integer> verifyOwner(Authentication authentication, @PathVariable Long cuentaId,
			@Valid @RequestBody OwnerVerification request) {
		return ApiResponse.success(admin.verifyOwner(cuentaId, request.financiera(), request.judicial(),
				request.riesgo(), employeeId(authentication)), "Consignador verificado");
	}

	@PostMapping("/consignaciones/{id}/proponer-acuerdo")
	public ApiResponse<Void> proposeAgreement(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Agreement request) {
		admin.proposeAgreement(id, request, employeeId(authentication));
		return ApiResponse.success(null, "Acuerdo propuesto");
	}

	@PostMapping("/consignaciones/{id}/asignar-subasta")
	public ApiResponse<Integer> assignAuction(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody AssignAuction request) {
		return ApiResponse.success(admin.assignAuction(id, request, employeeId(authentication)), "Subasta asignada");
	}

	@PostMapping("/consignaciones/{id}/liquidar")
	public ApiResponse<Liquidation> liquidate(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody Liquidate request) {
		return ApiResponse.success(admin.liquidate(id, request.medioPagoId(), employeeId(authentication)),
				"Consignacion liquidada");
	}

	@PostMapping("/seed/base")
	public ApiResponse<Status> seedBase(Authentication authentication) {
		return ApiResponse.success(admin.seed("base", employeeId(authentication)), "Seed base");
	}

	@PostMapping("/seed/demo-subastas")
	public ApiResponse<Status> seedAuctions(Authentication authentication) {
		return ApiResponse.success(admin.seed("demo_subastas", employeeId(authentication)), "Seed subastas");
	}

	@PostMapping("/seed/demo-usuarios")
	public ApiResponse<Status> seedUsers(Authentication authentication) {
		return ApiResponse.success(admin.seed("demo_usuarios", employeeId(authentication)), "Seed usuarios");
	}

	@PostMapping("/reset/demo")
	public ApiResponse<Status> resetDemo(Authentication authentication) {
		return ApiResponse.success(admin.resetDemo(employeeId(authentication)), "Reset demo");
	}

	private Integer employeeId(Authentication authentication) {
		return ((AdminPrincipal) authentication.getPrincipal()).employeeId();
	}
}
