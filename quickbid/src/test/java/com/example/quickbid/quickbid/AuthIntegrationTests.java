package com.example.quickbid.quickbid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import org.springframework.mock.web.MockMultipartFile;

import com.example.quickbid.quickbid.security.AuthRateLimitService;
import com.example.quickbid.quickbid.security.TokenService;
import com.example.quickbid.quickbid.service.SimulatedMailService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/auth-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthIntegrationTests {

	@Autowired MockMvc mvc;
	@Autowired AuthRateLimitService limits;
	@Autowired JdbcTemplate jdbc;
	@Autowired TokenService tokens;
	@Autowired SimulatedMailService mail;

	@BeforeEach
	void clearRateLimits() {
		limits.clear();
		mail.clear();
	}

	@Test
	void registroEtapa1Valido() throws Exception {
		mvc.perform(post("/api/auth/registro/etapa1").contentType(MediaType.APPLICATION_JSON)
				.content(etapa1("nuevo@quickbid.demo", 32)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.idRegistro").value(not(nullValue())))
				.andExpect(jsonPath("$.data.siguientePaso").value("etapa2"));
	}

	@Test
	void registroEtapa1RechazaPaisInvalido() throws Exception {
		mvc.perform(post("/api/auth/registro/etapa1").contentType(MediaType.APPLICATION_JSON)
				.content(etapa1("nuevo@quickbid.demo", 999)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_COUNTRY"));
	}

	@Test
	void registroEtapa1RechazaEmailRegistrado() throws Exception {
		mvc.perform(post("/api/auth/registro/etapa1").contentType(MediaType.APPLICATION_JSON)
				.content(etapa1("aprobado@quickbid.demo", 32)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void registroEtapa2RequiereFotos() throws Exception {
		mvc.perform(post("/api/auth/registro/etapa1").contentType(MediaType.APPLICATION_JSON)
				.content(etapa1("nuevo@quickbid.demo", 32))).andExpect(status().isCreated());
		mvc.perform(multipart("/api/auth/registro/etapa2").param("email", "nuevo@quickbid.demo"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("MISSING_FILE"));
	}

	@Test
	void registroEtapa2RechazaMimeFalseado() throws Exception {
		mvc.perform(post("/api/auth/registro/etapa1").contentType(MediaType.APPLICATION_JSON)
				.content(etapa1("nuevo@quickbid.demo", 32))).andExpect(status().isCreated());
		MockMultipartFile falso = new MockMultipartFile("fotoFrente", "dni.png", "image/png",
				"no-es-png".getBytes());
		MockMultipartFile dorso = new MockMultipartFile("fotoDorso", "dni.png", "image/png",
				new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10});
		mvc.perform(multipart("/api/auth/registro/etapa2").file(falso).file(dorso)
				.param("email", "nuevo@quickbid.demo"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_FILE"));
	}

	@Test
	void verificarTokenRechazaTokenInvalido() throws Exception {
		mvc.perform(post("/api/auth/registro/verificar-token").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"invalido\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
	}

	@Test
	void verificarTokenAceptaSolicitudAprobadaVigente() throws Exception {
		String raw = "setup-token-vigente";
		registrationReadyForSetup(9101, "setup-vigente@quickbid.demo", raw,
				"DATEADD('HOUR',2,CURRENT_TIMESTAMP)", "NULL", 2101);

		mvc.perform(post("/api/auth/registro/verificar-token").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + raw + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	void verificarTokenRechazaTokenVencidoOReutilizado() throws Exception {
		String expired = "setup-token-vencido";
		registrationReadyForSetup(9102, "setup-vencido@quickbid.demo", expired,
				"DATEADD('HOUR',-1,CURRENT_TIMESTAMP)", "NULL", 2102);
		mvc.perform(post("/api/auth/registro/verificar-token").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + expired + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));

		String used = "setup-token-usado";
		registrationReadyForSetup(9103, "setup-usado@quickbid.demo", used,
				"DATEADD('HOUR',2,CURRENT_TIMESTAMP)", "CURRENT_TIMESTAMP", 2103);
		mvc.perform(post("/api/auth/registro/verificar-token").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + used + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
	}

	@Test
	void loginPermiteUsuarioActivo() throws Exception {
		login("aprobado@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").isString())
				.andExpect(jsonPath("$.data.usuario.estado").value("activa"));
	}

	@Test
	void loginPermiteUsuarioConRestriccionPorMulta() throws Exception {
		login("multa@quickbid.demo").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.usuario.estado").value("restriccion_multa"));
	}

	@Test
	void loginRechazaCuentaBloqueada() throws Exception {
		login("bloqueado@quickbid.demo").andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errors[0].code").value("ACCOUNT_BLOCKED"));
	}

	@Test
	void refreshValidoRotaToken() throws Exception {
		String original = refreshToken(login("aprobado@quickbid.demo").andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString());
		mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(original)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.refreshToken").value(not(original)));
		mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(original)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void logoutRevocaRefreshToken() throws Exception {
		String token = refreshToken(login("aprobado@quickbid.demo").andReturn().getResponse().getContentAsString());
		mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(refreshBody(token)))
				.andExpect(status().isOk());
		mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshBody(token)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void recuperacionNoExponeExistenciaDelEmail() throws Exception {
		mvc.perform(post("/api/auth/recuperar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"aprobado@quickbid.demo\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Si el email existe, se envio un enlace"));
		mvc.perform(post("/api/auth/recuperar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ausente@quickbid.demo\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Si el email existe, se envio un enlace"));
		assertEquals(1, mail.deliveries().size());
		assertTrue(delivered("token", "recuperacion"));
	}

	@Test
	void reenvioLinkNoExponeUsuarioInexistenteNiEnviaMail() throws Exception {
		mvc.perform(post("/api/auth/registro/reenviar-link").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ausente@quickbid.demo\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors").isArray());

		assertEquals(0, mail.deliveries().size());
	}

	@Test
	void cambiarClaveRechazaTokenInvalido() throws Exception {
		mvc.perform(put("/api/auth/cambiar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"invalido\",\"clave\":\"Nueva123!\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
	}

	@Test
	void cambiarClaveRechazaTokenVencidoOReutilizado() throws Exception {
		String expired = "reset-token-vencido";
		jdbc.update("""
				INSERT INTO app_password_reset_tokens(cuenta_id,token_hash,expires_at)
				VALUES (3001,?,DATEADD('MINUTE',-1,CURRENT_TIMESTAMP))
				""", tokens.hash(expired));
		mvc.perform(put("/api/auth/cambiar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + expired + "\",\"clave\":\"Nueva123!\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));

		String used = "reset-token-usado";
		jdbc.update("""
				INSERT INTO app_password_reset_tokens(cuenta_id,token_hash,expires_at,used_at)
				VALUES (3001,?,DATEADD('MINUTE',30,CURRENT_TIMESTAMP),CURRENT_TIMESTAMP)
				""", tokens.hash(used));
		mvc.perform(put("/api/auth/cambiar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + used + "\",\"clave\":\"Nueva123!\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_TOKEN"));
	}

	@Test
	void cambiarClaveValidoPermiteLoginConClaveNueva() throws Exception {
		String raw = "reset-token-demo";
		jdbc.update("""
				INSERT INTO app_password_reset_tokens(cuenta_id,token_hash,expires_at)
				VALUES (3001,?,DATEADD('MINUTE',30,CURRENT_TIMESTAMP))
				""", tokens.hash(raw));

		mvc.perform(put("/api/auth/cambiar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"" + raw + "\",\"clave\":\"Nueva123!\"}"))
				.andExpect(status().isOk());
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"aprobado@quickbid.demo\",\"clave\":\"Nueva123!\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void endpointProtegidoSinTokenRespondeEnvelope401() throws Exception {
		mvc.perform(get("/api/auth/sesion"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
	}

	@Test
	void endpointProtegidoConTokenInvalidoRespondeEnvelope401() throws Exception {
		mvc.perform(get("/api/auth/sesion").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
	}

	@Test
	void endpointProtegidoConTokenExpiradoRespondeEnvelope401() throws Exception {
		mvc.perform(get("/api/auth/sesion").header("Authorization", "Bearer " + expiredToken()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("UNAUTHORIZED"));
	}

	@Test
	void jwtEmitidoParaCuentaLuegoDeshabilitadaRespondeEnvelope403() throws Exception {
		String access = accessToken(login("aprobado@quickbid.demo").andReturn().getResponse().getContentAsString());
		jdbc.update("UPDATE app_cuentas SET estado='deshabilitada_admin' WHERE id=3001");
		mvc.perform(get("/api/auth/sesion").header("Authorization", "Bearer " + access))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("ACCOUNT_BLOCKED"));
	}

	@Test
	void endpointAdminSinRolRespondeEnvelope403() throws Exception {
		String access = accessToken(login("aprobado@quickbid.demo").andReturn().getResponse().getContentAsString());
		mvc.perform(get("/api/admin/ping").header("Authorization", "Bearer " + access))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.data").value(nullValue()))
				.andExpect(jsonPath("$.errors[0].code").value("FORBIDDEN"));
	}

	@Test
	void loginExitosoQuedaAuditado() throws Exception {
		login("aprobado@quickbid.demo").andExpect(status().isOk());
		Integer eventos = jdbc.queryForObject(
				"SELECT count(*) FROM app_auditoria WHERE accion = 'auth.login_exitoso'", Integer.class);
		assertEquals(1, eventos);
	}

	@Test
	void loginAplicaRateLimit() throws Exception {
		for (int intento = 0; intento < 10; intento++) {
			mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"aprobado@quickbid.demo\",\"clave\":\"Incorrecta123!\"}"))
					.andExpect(status().isUnauthorized());
		}
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"aprobado@quickbid.demo\",\"clave\":\"Incorrecta123!\"}"))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.errors[0].code").value("RATE_LIMITED"));
	}

	@Test
	void recuperacionAplicaRateLimit() throws Exception {
		for (int intento = 0; intento < 3; intento++) {
			mvc.perform(post("/api/auth/recuperar-clave").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"ausente@quickbid.demo\"}")).andExpect(status().isOk());
		}
		mvc.perform(post("/api/auth/recuperar-clave").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ausente@quickbid.demo\"}"))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.errors[0].code").value("RATE_LIMITED"));
	}

	@Test
	void reenvioLinkAplicaRateLimit() throws Exception {
		for (int intento = 0; intento < 3; intento++) {
			mvc.perform(post("/api/auth/registro/reenviar-link").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"ausente@quickbid.demo\"}")).andExpect(status().isOk());
		}
		mvc.perform(post("/api/auth/registro/reenviar-link").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ausente@quickbid.demo\"}"))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.errors[0].code").value("RATE_LIMITED"));
	}

	@Test
	void loginFallidoConCuentaConocidaQuedaAuditado() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"aprobado@quickbid.demo\",\"clave\":\"Incorrecta123!\"}"))
				.andExpect(status().isUnauthorized());
		assertEquals(1, jdbc.queryForObject(
				"SELECT count(*) FROM app_auditoria WHERE accion='auth.login_fallido'", Integer.class));
	}

	private org.springframework.test.web.servlet.ResultActions login(String email) throws Exception {
		return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"clave\":\"Demo123!\"}"));
	}

	private String etapa1(String email, int pais) {
		return "{\"email\":\"" + email + "\",\"nombre\":\"Nora\",\"apellido\":\"Nueva\","
				+ "\"domicilioLegal\":\"Av. Demo 1\",\"idPaisOrigen\":" + pais + "}";
	}

	private String refreshToken(String json) {
		return JsonPath.read(json, "$.data.refreshToken");
	}

	private String accessToken(String json) {
		return JsonPath.read(json, "$.data.accessToken");
	}

	private String refreshBody(String token) {
		return "{\"refreshToken\":\"" + token + "\"}";
	}

	private void registrationReadyForSetup(long id, String email, String rawToken, String expiresSql, String usedSql,
			int personId) {
		jdbc.update("INSERT INTO personas (identificador,documento,nombre,direccion,estado) VALUES (?,?,?,?,?)",
				personId, "DNI-" + personId, "Persona Setup " + personId, "Av. Setup " + personId, "activo");
		jdbc.update("INSERT INTO clientes (identificador,\"numeroPais\",admitido,categoria,verificador) VALUES (?,?,?,?,?)",
				personId, 32, "si", "comun", 1001);
		jdbc.update("""
				INSERT INTO app_solicitudes_registro(
					id,email,nombre,apellido,domicilio_legal,id_pais_origen,estado,
					setup_token_hash,setup_token_expires_at,setup_token_used_at,persona_id,cliente_id
				)
				VALUES (?,?,?,?,?,32,'aprobada_pendiente_finalizacion',?,""" + expiresSql + "," + usedSql + ",?,?)",
				id, email, "Setup", "Demo", "Av. Setup", tokens.hash(rawToken), personId, personId);
	}

	private boolean delivered(String kind, String purpose) {
		return mail.deliveries().stream().anyMatch(delivery ->
				delivery.kind().equals(kind) && delivery.purpose().equals(purpose));
	}

	private String expiredToken() throws Exception {
		String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
		String payload = base64("{\"sub\":\"3001\",\"email\":\"aprobado@quickbid.demo\",\"estado\":\"activa\",\"iat\":1,\"exp\":1}");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec("test-only-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return header + "." + payload + "." + Base64.getUrlEncoder().withoutPadding()
				.encodeToString(mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8)));
	}

	private String base64(String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}
}
