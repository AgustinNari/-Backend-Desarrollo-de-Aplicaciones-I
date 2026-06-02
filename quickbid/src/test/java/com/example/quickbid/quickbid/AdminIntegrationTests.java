package com.example.quickbid.quickbid;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import com.example.quickbid.quickbid.dto.request.ConsignmentAgreementAcceptanceRequest;
import com.example.quickbid.quickbid.security.AdminInternalAuthenticationFilter;
import com.example.quickbid.quickbid.security.AuthRateLimitService;
import com.example.quickbid.quickbid.service.ConsignmentService;
import com.example.quickbid.quickbid.service.SimulatedMailService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/auth-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminIntegrationTests {
	private static final String ADMIN_KEY = "test-admin-key";
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;
	@Autowired AuthRateLimitService limits;
	@Autowired ConsignmentService consignments;
	@Autowired SimulatedMailService mail;

	@BeforeEach void clearLimits() {
		limits.clear();
		mail.clear();
	}

	@Test void adminSinCredencialEs401YUsuarioNormalEs403() throws Exception {
		mvc.perform(get("/api/admin/consignaciones")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
		mvc.perform(get("/api/admin/consignaciones").header("Authorization", "Bearer " + token("aprobado@quickbid.demo")))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.errors[0].code").value("FORBIDDEN"));
	}

	@Test void adminApruebaRegistroYGuardaSetupTokenHasheado() throws Exception {
		long id = registration("aprobar@quickbid.demo");
		admin(post("/api/admin/solicitudes-registro/" + id + "/aprobar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"documento\":\"DNI-ADMIN-001\",\"categoriaInicial\":\"especial\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobada_pendiente_finalizacion"));
		String hash = jdbc.queryForObject("SELECT setup_token_hash FROM app_solicitudes_registro WHERE id=?", String.class, id);
		assertNotNull(hash);
		assertEquals(64, hash.length());
		assertNotEquals("DNI-ADMIN-001", hash);
		assertEquals("especial", jdbc.queryForObject("SELECT categoria FROM clientes WHERE identificador=(SELECT cliente_id FROM app_solicitudes_registro WHERE id=?)", String.class, id));
		assertTrue(delivered("token", "registro"));
	}

	@Test void adminRechazaRegistroConMotivo() throws Exception {
		long id = registration("rechazar@quickbid.demo");
		admin(post("/api/admin/solicitudes-registro/" + id + "/rechazar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"motivo\":\"Documento ilegible\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("rechazado"))
				.andExpect(jsonPath("$.data.motivoRechazo").value("Documento ilegible"));
	}

	@Test void adminBloqueaDesbloqueaYAjustaPuntosYCategoria() throws Exception {
		admin(post("/api/admin/usuarios/3001/bloquear")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("deshabilitada_admin"));
		admin(post("/api/admin/usuarios/3001/desbloquear")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("activa"));
		admin(patch("/api/admin/usuarios/3001/puntos").contentType(MediaType.APPLICATION_JSON)
				.content("{\"delta\":25}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.puntos").value(925));
		assertEquals(1, count("SELECT COUNT(*) FROM app_movimientos_puntos WHERE motivo='ajuste_admin' AND cuenta_id=3001"));
		admin(patch("/api/admin/usuarios/3001/categoria").contentType(MediaType.APPLICATION_JSON)
				.content("{\"categoria\":\"oro\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.categoria").value("oro"));
		assertEquals("oro", jdbc.queryForObject("SELECT categoria FROM clientes WHERE identificador=2001", String.class));
	}

	@Test void adminVerificaYRechazaMediosPendientes() throws Exception {
		int before = jdbc.queryForObject("SELECT puntos FROM app_cuentas WHERE id=3001", Integer.class);
		admin(post("/api/admin/medios-pago/5003/verificar")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("verificado"));
		assertEquals(before + 50, jdbc.queryForObject("SELECT puntos FROM app_cuentas WHERE id=3001", Integer.class));

		jdbc.update("""
				INSERT INTO app_medios_pago(id,cuenta_id,tipo,moneda,estado,principal,nacional,alias_visible,titular,
					hash_identificador,consumo_actual) VALUES (5099,3001,'tarjeta','ARS','pendiente_verificacion',
					false,true,'Pendiente rechazo','Ana','hash-reject-admin',0)
				""");
		admin(post("/api/admin/medios-pago/5099/rechazar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"motivo\":\"Datos inconsistentes\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("rechazado"));
		assertEquals("Datos inconsistentes",
				jdbc.queryForObject("SELECT motivo_rechazo FROM app_medios_pago WHERE id=5099", String.class));
		assertTrue(delivered("notification", "medio_pago_verificado"));
		assertTrue(delivered("notification", "medio_pago_rechazado"));
	}

	@Test void adminCreaAbreYSeteaItemActivo() throws Exception {
		String date = LocalDate.now().plusDays(20).toString();
		String body = """
				{"fecha":"%s","hora":"18:30:00","titulo":"Subasta admin","descripcion":"Demo",
				"ubicacion":"Casa central","categoria":"comun","moneda":"ARS","segmento":"demo"}
				""".formatted(date);
		String json = admin(post("/api/admin/subastas").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.estado").value("programada"))
				.andReturn().getResponse().getContentAsString();
		Integer id = ((Number) JsonPath.read(json, "$.data.id")).intValue();
		jdbc.update("""
				INSERT INTO app_inscripciones_subasta(subasta_id,cuenta_id,medio_pago_id,estado)
				VALUES (?,3001,5001,'aprobada')
				""", id);
		admin(post("/api/admin/subastas/" + id + "/abrir")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("en_vivo"));
		assertTrue(delivered("notification", "subasta_inscripta_proxima_inicio"));
		admin(post("/api/admin/subastas/6004/item-activo").contentType(MediaType.APPLICATION_JSON)
				.content("{\"itemCatalogoId\":9006}")).andExpect(status().isOk());
		assertEquals(9006, jdbc.queryForObject("SELECT item_catalogo_activo_id FROM app_subasta_estado_vivo WHERE subasta_id=6004",
				Integer.class));
	}

	@Test void adminCierraLoteConPujaYSinPujas() throws Exception {
		jdbc.update("UPDATE productos SET duenio=2004 WHERE identificador=8001");
		admin(post("/api/admin/subastas/6001/cerrar-lote")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("pagos_extra_pendientes"));
		assertEquals(false, jdbc.queryForObject("SELECT comprador_empresa FROM app_compras WHERE item_catalogo_id=9001",
				Boolean.class));
		assertTrue(delivered("notification", "lote_ganado"));

		admin(post("/api/admin/subastas/6004/abrir")).andExpect(status().isOk());
		admin(post("/api/admin/subastas/6004/item-activo").contentType(MediaType.APPLICATION_JSON)
				.content("{\"itemCatalogoId\":9006}")).andExpect(status().isOk());
		admin(post("/api/admin/subastas/6004/cerrar-lote")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("pagos_extra_pendientes"));
		assertEquals(true, jdbc.queryForObject("SELECT comprador_empresa FROM app_compras WHERE item_catalogo_id=9006",
				Boolean.class));
	}

	@Test void adminSimulaPagoExitosoYFallido() throws Exception {
		jdbc.update("INSERT INTO app_entregas(compra_id,tipo,costo_envio,estado) VALUES (13002,'retiro',0,'pendiente')");
		admin(post("/api/admin/compras/13002/simular-pago-exitoso").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cuentaId\":3001,\"medioPagoId\":5001}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobado"));

		jdbc.update("UPDATE app_medios_pago SET limite_monto=500000 WHERE id=5004");
		admin(post("/api/admin/compras/13001/simular-falla-pago").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cuentaId\":3002,\"medioPagoId\":5004,\"tipo\":\"multa\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("rechazado"));
	}

	@Test void adminVenceYMarcaPagadaMulta() throws Exception {
		admin(post("/api/admin/multas/14001/vencer")).andExpect(status().isOk());
		assertEquals("bloqueada_permanente", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3002", String.class));

		jdbc.update("UPDATE app_multas SET estado='pendiente' WHERE id=14001");
		admin(post("/api/admin/multas/14001/marcar-pagada")).andExpect(status().isOk());
		assertEquals("activa", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3002", String.class));
	}

	@Test void adminGestionaRevisionDocumentalYListaConsignaciones() throws Exception {
		admin(get("/api/admin/consignaciones")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(3)));
		admin(post("/api/admin/consignaciones/16001/pedir-documentacion")).andExpect(status().isOk());
		assertEquals("documentacion_adicional",
				jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=16001", String.class));
		assertTrue(delivered("notification", "consignacion_documentacion_requerida"));
		jdbc.update("UPDATE app_solicitudes_consignacion SET estado='documentacion_recibida' WHERE id=16001");
		admin(post("/api/admin/consignaciones/16001/revisar-documentacion").contentType(MediaType.APPLICATION_JSON)
				.content("{\"aprobada\":true}")).andExpect(status().isOk());
		assertEquals("recepcion_pendiente",
				jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=16001", String.class));
	}

	@Test void adminExigeDuenioAntesDeProponerAcuerdoYLuegoPublica() throws Exception {
		Long id = consignmentReadyForAgreement(3001L);
		admin(post("/api/admin/consignaciones/" + id + "/proponer-acuerdo").contentType(MediaType.APPLICATION_JSON)
				.content(agreement())).andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].code").value("CONSIGNOR_NOT_VERIFIED"));
		admin(post("/api/admin/consignadores/3001/verificar-duenio").contentType(MediaType.APPLICATION_JSON)
				.content("{\"financiera\":true,\"judicial\":true,\"riesgo\":2}")).andExpect(status().isOk());
		admin(post("/api/admin/consignaciones/" + id + "/proponer-acuerdo").contentType(MediaType.APPLICATION_JSON)
				.content(agreement())).andExpect(status().isOk());
		assertTrue(delivered("notification", "acuerdo_disponible"));
		consignments.acceptAgreement(3001L, id, new ConsignmentAgreementAcceptanceRequest(true, true));
		admin(post("/api/admin/consignaciones/" + id + "/asignar-subasta").contentType(MediaType.APPLICATION_JSON)
				.content("{\"subastaId\":6004,\"catalogoId\":7004}")).andExpect(status().isOk());
		assertEquals("publicada",
				jdbc.queryForObject("SELECT estado FROM app_solicitudes_consignacion WHERE id=?", String.class, id));
		assertTrue(delivered("notification", "consignacion_publicada"));
	}

	@Test void adminLiquidaConsignacionYSeedEsNoOpSeguro() throws Exception {
		admin(post("/api/admin/consignaciones/16007/liquidar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5006}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.montoNeto").value(85500));
		assertTrue(delivered("notification", "liquidacion_disponible"));
		admin(post("/api/admin/seed/base")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("sin_cambios"));
		admin(post("/api/admin/reset/demo")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.detalle", not(blankOrNullString())));
	}

	private long registration(String email) {
		jdbc.update("""
				INSERT INTO app_solicitudes_registro(email,nombre,apellido,domicilio_legal,id_pais_origen,estado)
				VALUES (?,'Admin','Demo','Calle 1',32,'pendiente_revision')
				""", email);
		return jdbc.queryForObject("SELECT id FROM app_solicitudes_registro WHERE email=?", Long.class, email);
	}

	private Long consignmentReadyForAgreement(Long accountId) {
		Long id = consignments.create(accountId, "comun", true, true, "Articulo admin", "Descripcion demo", null,
				"1980", false, null, null, photos()).id();
		consignments.approveDigitalReview(id, 1002);
		consignments.markPhysicalReception(id, 1002);
		consignments.approvePhysicalReview(id, 1002);
		return id;
	}

	private String agreement() {
		return """
				{"valorBase":25000,"moneda":"ARS","comisionCompradorPct":10,
				"comisionVendedorPct":10,"condiciones":"Acuerdo demo"}
				""";
	}

	private List<MultipartFile> photos() {
		List<MultipartFile> result = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			result.add(new MockMultipartFile("fotos", "foto-" + i + ".png", "image/png",
					new byte[] { (byte) 137, 80, 78, 71, 13, 10, 26, 10 }));
		}
		return result;
	}

	private ResultActions admin(MockHttpServletRequestBuilder builder) throws Exception {
		return mvc.perform(builder.header(AdminInternalAuthenticationFilter.HEADER, ADMIN_KEY));
	}

	private String token(String email) throws Exception {
		String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"clave\":\"Demo123!\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.data.accessToken");
	}

	private int count(String sql, Object... args) {
		return jdbc.queryForObject(sql, Integer.class, args);
	}

	private boolean delivered(String kind, String purpose) {
		return mail.deliveries().stream().anyMatch(delivery ->
				delivery.kind().equals(kind) && delivery.purpose().equals(purpose));
	}
}
