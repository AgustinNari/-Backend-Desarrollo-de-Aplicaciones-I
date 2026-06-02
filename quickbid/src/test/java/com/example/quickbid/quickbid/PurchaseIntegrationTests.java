package com.example.quickbid.quickbid;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.security.AuthRateLimitService;
import com.example.quickbid.quickbid.service.PurchaseService;
import com.example.quickbid.quickbid.service.PurchaseService.PaymentOutcome;
import com.example.quickbid.quickbid.service.SimulatedMailService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/auth-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PurchaseIntegrationTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;
	@Autowired AuthRateLimitService limits;
	@Autowired PurchaseService purchases;
	@Autowired SimulatedMailService mail;

	@BeforeEach void clearLimits() {
		limits.clear();
		mail.clear();
	}

	@Test void cierreConGanadorCreaCompraPagoYMarcaPujaLegacy() {
		jdbc.update("UPDATE productos SET duenio=7001 WHERE identificador=8001");
		var purchase = purchases.closeLot(6001, PaymentOutcome.AUTO);

		assertEquals("pagos_extra_pendientes", purchase.estado());
		assertEquals(3001L, jdbc.queryForObject("SELECT cuenta_comprador_id FROM app_compras WHERE id=?", Long.class, purchase.id()));
		assertEquals("ganadora", jdbc.queryForObject("SELECT estado FROM app_pujas_live WHERE id=12501", String.class));
		assertEquals("si", jdbc.queryForObject("SELECT ganador FROM pujos WHERE identificador=12001", String.class));
		assertEquals("si", jdbc.queryForObject("SELECT subastado FROM \"itemsCatalogo\" WHERE identificador=9001", String.class));
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_pagos WHERE compra_id=? AND estado='aprobado'", Integer.class, purchase.id()));
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_movimientos_puntos WHERE referencia_tipo='compra' AND referencia_id=?", Integer.class, purchase.id()));
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM \"registroDeSubasta\" WHERE subasta=6001 AND producto=8001 AND cliente=2001", Integer.class));
		assertTrue(delivered("lote_ganado"));
		assertTrue(delivered("pago_adjudicacion_exitoso"));
	}

	@Test void cierreSinPujasCreaCompraInternaEmpresa() {
		liveAuction(6201, 9201);

		var purchase = purchases.closeLot(6201, PaymentOutcome.AUTO);

		assertEquals(true, jdbc.queryForObject("SELECT comprador_empresa FROM app_compras WHERE id=?", Boolean.class, purchase.id()));
		assertEquals(null, jdbc.queryForObject("SELECT cuenta_comprador_id FROM app_compras WHERE id=?", Long.class, purchase.id()));
		assertEquals("pagos_extra_pendientes", purchase.estado());
		assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM app_pagos WHERE compra_id=?", Integer.class, purchase.id()));
	}

	@Test void loteNoPuedeCerrarseDosVeces() {
		purchases.closeLot(6001, PaymentOutcome.AUTO);
		BusinessException exception = assertThrows(BusinessException.class,
				() -> purchases.closeLot(6001, PaymentOutcome.AUTO));
		assertEquals("LOT_ALREADY_CLOSED", exception.getErrors().get(0).code());
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_compras WHERE item_catalogo_id=9001", Integer.class));
	}

	@Test void fallaAutomaticaGeneraMultaDelDiezPorCientoYRestriccion() {
		var purchase = purchases.closeLot(6001, PaymentOutcome.FAILURE);

		assertEquals("multa_activa", purchase.estado());
		assertEquals("restriccion_multa", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3001", String.class));
		assertEquals("2510.00", jdbc.queryForObject("SELECT monto FROM app_multas WHERE compra_id=?", java.math.BigDecimal.class, purchase.id()).toPlainString());
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_pagos WHERE compra_id=? AND estado='rechazado'", Integer.class, purchase.id()));
		assertTrue(delivered("multa_generada"));
	}

	@Test void pagarConMultaExitosoReactivaCuentaSiNoQuedanOtras() {
		var payment = purchases.simulateSuccessfulFine(3002L, 13001L, 5004L);

		assertEquals("aprobado", payment.estado());
		assertEquals("pagos_extra_pendientes", payment.compraEstado());
		assertEquals("pagada", jdbc.queryForObject("SELECT estado FROM app_multas WHERE id=14001", String.class));
		assertEquals("activa", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3002", String.class));
		assertTrue(delivered("multa_pagada"));
	}

	@Test void pagarConMultaFallidoNoDuplicaMulta() {
		var payment = purchases.simulateFailedFine(3002L, 13001L, 5004L);

		assertEquals("rechazado", payment.estado());
		assertEquals("multa_activa", payment.compraEstado());
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_multas WHERE compra_id=13001", Integer.class));
	}

	@Test void multaVencidaBloqueaPermanentemente() {
		purchases.expireFine(14001L, true);

		assertEquals("vencida", jdbc.queryForObject("SELECT estado FROM app_multas WHERE id=14001", String.class));
		assertEquals("bloqueada_permanente", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3002", String.class));
		assertTrue(delivered("multa_vencida"));
	}

	@Test void listadoSoloIncluyeComprasPropiasYDetalleAjenoEs403() throws Exception {
		auth(get("/api/compras"), "aprobado@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content", hasSize(1)))
				.andExpect(jsonPath("$.data.content[0].id").value(13002));
		auth(get("/api/compras/13001"), "aprobado@quickbid.demo").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_OWNED"));
	}

	@Test void compraInexistenteDevuelve404Uniforme() throws Exception {
		auth(get("/api/compras/99999"), "aprobado@quickbid.demo")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_FOUND"));
	}

	@Test void entregaPorEnvioYPagoExtrasDejanEntregaPendiente() throws Exception {
		auth(put("/api/compras/13002/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"envio\",\"direccionEnvioId\":5101}"), "aprobado@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.costoEnvio").value(5000))
				.andExpect(jsonPath("$.data.perdioCoberturaSeguro").value(false));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5001,\"idempotencyKey\":\"extras-envio\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobado"))
				.andExpect(jsonPath("$.data.monto").value(13000))
				.andExpect(jsonPath("$.data.compraEstado").value("entrega_pendiente"))
				.andExpect(jsonPath("$.data.numeroTarjeta").doesNotExist())
				.andExpect(jsonPath("$.data.cvv").doesNotExist())
				.andExpect(jsonPath("$.data.hashIdentificador").doesNotExist());
	}

	@Test void entregaEnEstadoInvalidoDevuelve409Uniforme() throws Exception {
		auth(put("/api/compras/13001/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"retiro\"}"), "multa@quickbid.demo")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_STATE_TRANSITION"));
	}

	@Test void retiroPierdeCoberturaYPuedeCompletarseInternamente() throws Exception {
		auth(put("/api/compras/13002/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"retiro\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.costoEnvio").value(0))
				.andExpect(jsonPath("$.data.perdioCoberturaSeguro").value(true));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5001,\"idempotencyKey\":\"extras-retiro\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.compraEstado").value("retiro_pendiente"));

		purchases.completePickup(13002L);
		assertEquals("completada", jdbc.queryForObject("SELECT estado FROM app_compras WHERE id=13002", String.class));
	}

	@Test void pagoExtrasEsIdempotenteYDetectaCambioDeMedio() throws Exception {
		jdbc.update("""
				INSERT INTO app_medios_pago(id,cuenta_id,tipo,moneda,estado,principal,nacional,alias_visible,titular,
					hash_identificador,limite_monto,consumo_actual,verificado_hasta)
				VALUES (5097,3001,'tarjeta','ARS','verificado',false,true,'Alternativa','Ana','hash-alternative',
					500000,0,DATEADD('DAY',7,CURRENT_TIMESTAMP))
				""");
		auth(put("/api/compras/13002/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"retiro\"}"), "aprobado@quickbid.demo").andExpect(status().isOk());
		String payment = "{\"medioPagoId\":5001,\"idempotencyKey\":\"extras-repeatable\"}";
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON).content(payment),
				"aprobado@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.idempotentReplay").value(false));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON).content(payment),
				"aprobado@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.idempotentReplay").value(true));
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_pagos WHERE idempotency_key='extras-repeatable'", Integer.class));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5097,\"idempotencyKey\":\"extras-repeatable\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].code").value("IDEMPOTENCY_CONFLICT"));
	}

	@Test void pagoConMedioInexistenteOAjenoDevuelveErroresUniformes() throws Exception {
		auth(put("/api/compras/13002/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"retiro\"}"), "aprobado@quickbid.demo").andExpect(status().isOk());
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":9999,\"idempotencyKey\":\"missing-payment\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_FOUND"));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5006,\"idempotencyKey\":\"foreign-payment\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_OWNED"));
	}

	@Test void pagoDuplicadoConNuevaClaveSeRechazaPorEstado() throws Exception {
		auth(put("/api/compras/13002/entrega").contentType(MediaType.APPLICATION_JSON)
				.content("{\"tipo\":\"retiro\"}"), "aprobado@quickbid.demo").andExpect(status().isOk());
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5001,\"idempotencyKey\":\"extras-first\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobado"));
		auth(post("/api/compras/13002/pagar").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5001,\"idempotencyKey\":\"extras-second\"}"), "aprobado@quickbid.demo")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_STATE_TRANSITION"));
	}

	@Test void documentosDevuelveSoloComprobantesDisponiblesDeCompraPropia() throws Exception {
		jdbc.update("""
				INSERT INTO app_archivos(id,tipo_contexto,filename_original,content_type,size_bytes,storage_path,checksum)
				VALUES (5201,'documento_compra','factura-demo.pdf','application/pdf',1234,'demo/factura.pdf','checksum')
				""");
		jdbc.update("""
				INSERT INTO app_archivos(id,tipo_contexto,filename_original,content_type,size_bytes,storage_path,checksum)
				VALUES (5202,'documento_compra','pendiente-demo.pdf','application/pdf',1234,'demo/pendiente.pdf','checksum-pending')
				""");
		jdbc.update("INSERT INTO app_documentos(id,tipo,referencia_tipo,referencia_id,archivo_id,estado) VALUES (5301,'factura_compra','compra',13002,5201,'disponible')");
		jdbc.update("INSERT INTO app_documentos(id,tipo,referencia_tipo,referencia_id,archivo_id,estado) VALUES (5302,'comprobante_interno','compra',13002,5202,'pendiente')");

		auth(get("/api/compras/13002/documentos"), "aprobado@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].filename").value("factura-demo.pdf"));
		auth(get("/api/compras/13002/documentos"), "multa@quickbid.demo").andExpect(status().isForbidden());
		auth(get("/api/compras/99999/documentos"), "aprobado@quickbid.demo").andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0].code").value("RESOURCE_NOT_FOUND"));
	}

	@Test void pagarConMultaPublicoFuncionaParaUsuarioRestringido() throws Exception {
		auth(post("/api/compras/13001/pagar-con-multa").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5004,\"idempotencyKey\":\"fine-public-failed\"}"), "multa@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("rechazado"));
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_multas WHERE compra_id=13001", Integer.class));
	}

	@Test void pagarConMultaPublicoExitosoReactivaUsuarioRestringido() throws Exception {
		jdbc.update("UPDATE app_medios_pago SET limite_monto=400000 WHERE id=5004");
		auth(post("/api/compras/13001/pagar-con-multa").contentType(MediaType.APPLICATION_JSON)
				.content("{\"medioPagoId\":5004,\"idempotencyKey\":\"fine-public-ok\"}"), "multa@quickbid.demo")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.estado").value("aprobado"))
				.andExpect(jsonPath("$.data.compraEstado").value("pagos_extra_pendientes"));
		assertEquals("activa", jdbc.queryForObject("SELECT estado FROM app_cuentas WHERE id=3002", String.class));
	}

	@Test void cierreDeSubastaYAbandonoQuedanDisponiblesComoServiciosInternos() {
		liveAuction(6203, 9203);
		var purchase = purchases.closeLot(6203, PaymentOutcome.AUTO);
		purchases.closeAuction(6203);
		assertEquals("finalizada", jdbc.queryForObject("SELECT estado_operativo FROM app_subasta_ext WHERE subasta_id=6203", String.class));

		purchases.abandon(purchase.id(), false);
		assertEquals("abandonada_por_incumplimiento_pago",
				jdbc.queryForObject("SELECT estado FROM app_compras WHERE id=?", String.class, purchase.id()));
	}

	@Test void comprasSinTokenDevuelve401Uniforme() throws Exception {
		mvc.perform(get("/api/compras")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
	}

	@Test void comprasConTokenInvalidoDevuelve401Uniforme() throws Exception {
		mvc.perform(get("/api/compras").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test void dosCierresConcurrentesNoDuplicanCompra() throws Exception {
		liveAuction(6202, 9202);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		var executor = Executors.newFixedThreadPool(2);
		try {
			Future<?> first = executor.submit(() -> close(ready, start, 6202));
			Future<?> second = executor.submit(() -> close(ready, start, 6202));
			ready.await();
			start.countDown();
			int rejected = 0;
			for (Future<?> result : List.of(first, second)) {
				try {
					result.get();
				} catch (ExecutionException exception) {
					assertInstanceOf(BusinessException.class, exception.getCause());
					rejected++;
				}
			}
			assertEquals(1, rejected);
		} finally {
			executor.shutdownNow();
		}
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM app_compras WHERE item_catalogo_id=9202", Integer.class));
	}

	private void close(CountDownLatch ready, CountDownLatch start, int auctionId) {
		try {
			ready.countDown();
			start.await();
			purchases.closeLot(auctionId, PaymentOutcome.AUTO);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(exception);
		}
	}

	private boolean delivered(String purpose) {
		return mail.deliveries().stream().anyMatch(delivery ->
				delivery.kind().equals("notification") && delivery.purpose().equals(purpose));
	}

	private void liveAuction(int auctionId, int itemId) {
		LocalDateTime start = LocalDateTime.now().minusMinutes(5);
		int productId = itemId + 1000;
		int catalogId = itemId + 2000;
		jdbc.update("INSERT INTO subastas(identificador,fecha,hora,estado,ubicacion,categoria) VALUES (?,?,?,?,?,?)",
				auctionId, start.toLocalDate(), Time.valueOf(start.toLocalTime()), "abierta", "Demo", "especial");
		jdbc.update("INSERT INTO app_subasta_ext(subasta_id,titulo,descripcion,moneda,segmento,estado_operativo) VALUES (?,?,?,?,?,?)",
				auctionId, "Subasta " + auctionId, "Demo", "ARS", "demo", "en_vivo");
		jdbc.update("INSERT INTO productos(identificador,\"descripcionCatalogo\") VALUES (?,?)", productId, "Item demo");
		jdbc.update("INSERT INTO catalogos(identificador,descripcion,subasta) VALUES (?,?,?)", catalogId, "Catalogo demo", auctionId);
		jdbc.update("INSERT INTO \"itemsCatalogo\"(identificador,catalogo,producto,\"precioBase\",comision,subastado) VALUES (?,?,?,?,?,?)",
				itemId, catalogId, productId, 1000, 100, "no");
		jdbc.update("INSERT INTO app_subasta_estado_vivo(subasta_id,item_catalogo_activo_id,version,usuarios_conectados) VALUES (?,?,0,0)",
				auctionId, itemId);
	}

	private ResultActions auth(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
			String email) throws Exception {
		return mvc.perform(builder.header("Authorization", "Bearer " + token(email)));
	}

	private String token(String email) throws Exception {
		String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"clave\":\"Demo123!\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.data.accessToken");
	}
}
