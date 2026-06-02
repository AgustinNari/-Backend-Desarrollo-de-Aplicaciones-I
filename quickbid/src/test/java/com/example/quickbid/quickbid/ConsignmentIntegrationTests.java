package com.example.quickbid.quickbid;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import com.example.quickbid.quickbid.dto.request.ConsignmentAgreementAcceptanceRequest;
import com.example.quickbid.quickbid.dto.request.ConsignmentReturnPaymentRequest;
import com.example.quickbid.quickbid.dto.request.ConsignmentReturnRequest;
import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.security.AuthRateLimitService;
import com.example.quickbid.quickbid.service.ConsignmentService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/auth-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ConsignmentIntegrationTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;
	@Autowired AuthRateLimitService limits;
	@Autowired ConsignmentService consignments;

	@BeforeEach void clearLimits() {
		limits.clear();
	}

	@Test void requisitosPermitenCuentaActivaYCuentaConRestriccionDeMulta() throws Exception {
		request(get("/api/consignaciones/requisitos"), "oro@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puedeContinuar").value(true));
		request(get("/api/consignaciones/requisitos"), "multa@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puedeContinuar").value(true));
	}

	@Test void cuentaBancariaPendientePermiteCrearConsignacionSinMedioVerificadoVigente() throws Exception {
		jdbc.update("UPDATE app_medios_pago SET estado='pendiente_verificacion',verificado_hasta=NULL WHERE id=5006");
		request(get("/api/consignaciones/requisitos"), "oro@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puedeContinuar").value(true))
				.andExpect(jsonPath("$.data.requisitos[0].codigo").value("MEDIO_PAGO_REGISTRADO"))
				.andExpect(jsonPath("$.data.requisitos[0].cumplido").value(true))
				.andExpect(jsonPath("$.data.requisitos[1].codigo").value("CUENTA_COBRO_REGISTRADA"))
				.andExpect(jsonPath("$.data.requisitos[1].cumplido").value(true))
				.andExpect(jsonPath("$.data.requisitos[2].codigo").value("MEDIO_PAGO_APTO_PARA_ENVIO_DEVOLUCION"))
				.andExpect(jsonPath("$.data.requisitos[2].cumplido").value(false));
		assertNotNull(create(3004L));
	}

	@Test void cuentaBancariaConVerificacionManualExpiradaPermiteCrearConsignacion() {
		jdbc.update("UPDATE app_medios_pago SET verificado_hasta=DATEADD('DAY',-1,CURRENT_TIMESTAMP) WHERE id=5006");
		assertNotNull(create(3004L));
	}

	@Test void sinCuentaBancariaRegistradaNoPuedeCrearConsignacionAunqueTengaTarjeta() throws Exception {
		jdbc.update("UPDATE app_medios_pago SET estado='eliminado',deleted_at=CURRENT_TIMESTAMP WHERE id=5006");
		jdbc.update("""
				INSERT INTO app_medios_pago(id,cuenta_id,tipo,moneda,estado,principal,nacional,alias_visible,ultimos_4,
					titular,hash_identificador,consumo_actual) VALUES
					(5091,3004,'tarjeta','ARS','pendiente_verificacion',false,true,'Tarjeta pendiente','1111',
					'Diego Oro','demo-card-pending-consignment',0)
				""");
		request(get("/api/consignaciones/requisitos"), "oro@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puedeContinuar").value(false))
				.andExpect(jsonPath("$.data.requisitos[0].cumplido").value(true))
				.andExpect(jsonPath("$.data.requisitos[1].cumplido").value(false));
		assertCode("CONSIGNMENT_REQUIREMENTS_NOT_MET", () -> create(3004L));
	}

	@Test void mediosSoloRechazadosOEliminadosNoPermitenCrearConsignacion() throws Exception {
		jdbc.update("UPDATE app_medios_pago SET estado='rechazado' WHERE id=5006");
		request(get("/api/consignaciones/requisitos"), "oro@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puedeContinuar").value(false))
				.andExpect(jsonPath("$.data.requisitos[0].cumplido").value(false))
				.andExpect(jsonPath("$.data.requisitos[1].cumplido").value(false));
		assertCode("CONSIGNMENT_REQUIREMENTS_NOT_MET", () -> create(3004L));
	}

	@Test void cuentaConRestriccionDeMultaPuedeCrearConCuentaBancariaPendiente() {
		jdbc.update("UPDATE app_medios_pago SET estado='pendiente_verificacion',verificado_hasta=NULL WHERE id=5005");
		assertNotNull(create(3002L));
	}

	@Test void altaExigeSeisFotosYNoCreaLegacyAntesDelAcuerdo() throws Exception {
		var insufficient = multipart("/api/consignaciones");
		params(insufficient);
		for (int i = 0; i < 5; i++) insufficient.file(photo("fotos", i));
		request(insufficient, "oro@quickbid.demo").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("MINIMUM_PHOTOS_REQUIRED"));

		long beforeProducts = count("SELECT COUNT(*) FROM productos");
		long beforeOwners = count("SELECT COUNT(*) FROM duenios");
		var valid = multipart("/api/consignaciones");
		params(valid);
		for (int i = 0; i < 6; i++) valid.file(photo("fotos", i));
		String json = request(valid, "multa@quickbid.demo").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.estado").value("pendiente_revision"))
				.andExpect(jsonPath("$.data.productoId").value(nullValue()))
				.andReturn().getResponse().getContentAsString();
		Long id = ((Number) JsonPath.read(json, "$.data.id")).longValue();
		assertNull(jdbc.queryForObject("SELECT producto_id FROM app_solicitudes_consignacion WHERE id=?", Integer.class, id));
		assertEquals(beforeProducts, count("SELECT COUNT(*) FROM productos"));
		assertEquals(beforeOwners, count("SELECT COUNT(*) FROM duenios"));
	}

	@Test void documentacionSolicitadaQuedaPendienteDeRevisionManual() throws Exception {
		consignments.requestOriginDocuments(16001L, 1002);
		request(multipart("/api/consignaciones/16001/documentacion-origen")
				.file(pdf("facturaCompra"))
				.param("observaciones", "Factura demo"), "oro@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("documentacion_recibida"))
				.andExpect(jsonPath("$.data.documentosOrigen[0].estado").value("pendiente"));
		assertEquals("documentacion_recibida",
				jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=16001", String.class));

		consignments.reviewOriginDocuments(16001L, 1002, true, null);
		assertEquals("recepcion_pendiente",
				jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=16001", String.class));
	}

	@Test void documentacionNoConfiaSoloEnMimeDeclarado() throws Exception {
		consignments.requestOriginDocuments(16001L, 1002);
		request(multipart("/api/consignaciones/16001/documentacion-origen")
				.file(new MockMultipartFile("facturaCompra", "origen.pdf", "application/pdf", "contenido falso".getBytes())),
				"oro@quickbid.demo").andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors[0].code").value("FILE_TYPE_NOT_SUPPORTED"));
	}

	@Test void aceptarAcuerdoCreaProductoYFotosLegacyPublicarCreaItemYPoliza() {
		Long id = create(3004L);
		toAgreement(id);
		var accepted = consignments.acceptAgreement(3004L, id,
				new ConsignmentAgreementAcceptanceRequest(true, true));
		assertNotNull(accepted.productoId());
		assertEquals(6, count("SELECT COUNT(*) FROM fotos WHERE producto=?", accepted.productoId()));
		assertNull(jdbc.queryForObject("SELECT seguro FROM productos WHERE identificador=?", String.class,
				accepted.productoId()));

		Integer itemId = consignments.assignAuctionAndInsurance(id, 1002, 6004, 7004, null);
		assertNotNull(itemId);
		assertNotNull(jdbc.queryForObject("SELECT seguro FROM productos WHERE identificador=?", String.class,
				accepted.productoId()));
		assertEquals("publicada", jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=?",
				String.class, id));
		assertEquals(1, count("SELECT COUNT(*) FROM app_movimientos_puntos WHERE motivo='consignacion_publicada' AND referencia_id=?",
				id));
	}

	@Test void polizaCombinadaSoloAdmiteMismoDuenioYMismaSubasta() {
		Long first = accepted();
		consignments.assignAuctionAndInsurance(first, 1002, 6004, 7004, null);
		String policy = jdbc.queryForObject("""
				SELECT p.seguro FROM productos p JOIN app_solicitudes_consignacion s ON s.producto_id=p.identificador
				WHERE s.id=?
				""", String.class, first);
		Long second = accepted();
		consignments.assignAuctionAndInsurance(second, 1002, 6004, 7004, policy);
		assertEquals("si", jdbc.queryForObject("SELECT \"polizaCombinada\" FROM seguros WHERE \"nroPoliza\"=?",
				String.class, policy));
	}

	@Test void rechazoFisicoPermiteElegirEnvioYPagarlo() throws Exception {
		Long id = rejectedReturn();
		request(post("/api/consignaciones/" + id + "/devolucion").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"modalidad":"envio","direccion":"Av. Demo 400","codigoPostal":"C1000",
						"localidad":"Buenos Aires","provincia":"Buenos Aires"}
						"""), "oro@quickbid.demo").andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.estado").value("pendiente_pago"))
				.andExpect(jsonPath("$.data.costo").value(12000));
		request(post("/api/consignaciones/" + id + "/devolucion/pagar-envio").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5006,\"idempotencyKey\":\"return-" + id + "\"}"), "oro@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobado"));
		request(post("/api/consignaciones/" + id + "/devolucion/pagar-envio").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5006,\"idempotencyKey\":\"return-" + id + "\"}"), "oro@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.idempotentReplay").value(true));
		assertEquals("pendiente_entrega", jdbc.queryForObject("""
				SELECT estado FROM app_consignacion_devoluciones WHERE solicitud_id=?
				""", String.class, id));
	}

	@Test void pagoDeEnvioDeDevolucionExigeMedioVerificadoConValidacionManualVigente() {
		Long id = rejectedReturn();
		consignments.selectReturn(3004L, id, shippingReturn());
		jdbc.update("UPDATE app_medios_pago SET estado='pendiente_verificacion',verificado_hasta=NULL WHERE id=5006");
		assertCode("PAYMENT_METHOD_NOT_VERIFIED",
				() -> consignments.payReturnShipping(3004L, id, new ConsignmentReturnPaymentRequest(5006L, "pending")));
		jdbc.update("UPDATE app_medios_pago SET estado='verificado',verificado_hasta=DATEADD('DAY',-1,CURRENT_TIMESTAMP) WHERE id=5006");
		assertCode("PAYMENT_METHOD_VERIFICATION_EXPIRED",
				() -> consignments.payReturnShipping(3004L, id, new ConsignmentReturnPaymentRequest(5006L, "expired")));
	}

	@Test void envioDeDevolucionPuedePagarseConMedioDistintoAlRegistradoAlIniciar() {
		jdbc.update("UPDATE app_medios_pago SET estado='pendiente_verificacion',verificado_hasta=NULL WHERE id=5006");
		Long id = rejectedReturn();
		consignments.selectReturn(3004L, id, shippingReturn());
		jdbc.update("""
				INSERT INTO app_medios_pago(id,cuenta_id,tipo,moneda,estado,principal,nacional,alias_visible,ultimos_4,
					titular,hash_identificador,limite_monto,consumo_actual,verificado_hasta) VALUES
					(5092,3004,'tarjeta','ARS','verificado',false,true,'Tarjeta devolucion','2222',
					'Diego Oro','demo-card-return-payment',100000,0,DATEADD('DAY',7,CURRENT_TIMESTAMP))
				""");
		var payment = consignments.payReturnShipping(3004L, id,
				new ConsignmentReturnPaymentRequest(5092L, "other-method"));
		assertEquals(5092L, payment.medioPagoId());
		assertEquals("aprobado", payment.estado());
	}

	@Test void retiroDeDevolucionNoExigePagoDeEnvio() {
		Long id = rejectedReturn();
		var result = consignments.selectReturn(3004L, id,
				new ConsignmentReturnRequest("retiro", null, null, null, null, null, null));
		assertEquals("pendiente_retiro", result.estado());
		assertEquals(BigDecimal.ZERO.setScale(2), result.costo());
		assertEquals(0, count("""
				SELECT COUNT(*) FROM app_pagos p JOIN app_consignacion_devoluciones d
				ON d.id=p.consignacion_devolucion_id WHERE d.solicitud_id=?
				""", id));
	}

	@Test void liquidacionSeGeneraUnaSolaVez() {
		jdbc.update("UPDATE app_medios_pago SET estado='rechazado',verificado_hasta=DATEADD('DAY',-1,CURRENT_TIMESTAMP) WHERE id=5006");
		var liquidation = consignments.liquidate(16007L, 1002, 5006L);
		assertEquals(new BigDecimal("85500.00"), liquidation.montoNeto());
		assertEquals("Cuenta bancaria registrada #5006", liquidation.cuentaDestino());
		BusinessException exception = assertThrows(BusinessException.class,
				() -> consignments.liquidate(16007L, 1002, 5006L));
		assertEquals("LIQUIDATION_ALREADY_EXISTS", exception.getErrors().get(0).code());
		assertEquals(1, count("SELECT COUNT(*) FROM app_liquidaciones_consignacion WHERE solicitud_id=16007"));
	}

	@Test void detalleAjenoEs403YEndpointSinTokenEs401Uniforme() throws Exception {
		request(get("/api/consignaciones/16001"), "aprobado@quickbid.demo").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_OWNED"));
		mvc.perform(get("/api/consignaciones")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
	}

	private Long accepted() {
		Long id = create(3004L);
		toAgreement(id);
		return consignments.acceptAgreement(3004L, id, new ConsignmentAgreementAcceptanceRequest(true, true)).id();
	}

	private Long rejectedReturn() {
		Long id = create(3004L);
		consignments.approveDigitalReview(id, 1002);
		consignments.markPhysicalReception(id, 1002);
		consignments.rejectPhysicalReview(id, 1002, "Daño detectado");
		return id;
	}

	private ConsignmentReturnRequest shippingReturn() {
		return new ConsignmentReturnRequest("envio", "Av. Demo 400", null, "C1000", "Buenos Aires",
				"Buenos Aires", null);
	}

	private void assertCode(String code, Runnable action) {
		BusinessException exception = assertThrows(BusinessException.class, action::run);
		assertEquals(code, exception.getErrors().get(0).code());
	}

	private void toAgreement(Long id) {
		consignments.approveDigitalReview(id, 1002);
		consignments.markPhysicalReception(id, 1002);
		consignments.approvePhysicalReview(id, 1002);
		consignments.verifyConsignor(3004L, 1002, true, true, 2);
		consignments.proposeAgreement(id, 1002, new BigDecimal("25000"), "ARS", null, null, "Acuerdo demo");
	}

	private Long create(Long accountId) {
		return consignments.create(accountId, "comun", true, true, "Articulo demo", "Descripcion demo", null,
				"1980", false, null, null, photos()).id();
	}

	private List<MultipartFile> photos() {
		List<MultipartFile> result = new ArrayList<>();
		for (int i = 0; i < 6; i++) result.add(photo("fotos", i));
		return result;
	}

	private MockMultipartFile photo(String part, int index) {
		return new MockMultipartFile(part, "foto-" + index + ".png", "image/png",
				new byte[] { (byte) 137, 80, 78, 71, 13, 10, 26, 10 });
	}

	private MockMultipartFile pdf(String part) {
		return new MockMultipartFile(part, "origen.pdf", "application/pdf", "%PDF-1.4\nDemo".getBytes());
	}

	private void params(org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder builder) {
		builder.param("segmento", "comun").param("aceptaTyC", "true")
				.param("declaracionPropiedadYOrigenLicito", "true").param("titulo", "Articulo demo")
				.param("descripcion", "Descripcion demo");
	}

	private long count(String sql, Object... args) {
		return jdbc.queryForObject(sql, Long.class, args);
	}

	private ResultActions request(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
			String email) throws Exception {
		return mvc.perform(builder.header("Authorization", "Bearer " + token(email)));
	}

	private ResultActions request(
			org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder builder, String email)
			throws Exception {
		return mvc.perform(builder.header("Authorization", "Bearer " + token(email)));
	}

	private String token(String email) throws Exception {
		String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"clave\":\"Demo123!\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.data.accessToken");
	}
}
