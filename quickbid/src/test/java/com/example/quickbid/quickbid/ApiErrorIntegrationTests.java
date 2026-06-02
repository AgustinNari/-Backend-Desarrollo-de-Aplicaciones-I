package com.example.quickbid.quickbid;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/auth-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ApiErrorIntegrationTests {
	@Autowired MockMvc mvc;

	@Test
	void jsonMalformadoDevuelve400Uniforme() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"aprobado@quickbid.demo\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_JSON"));
	}

	@Test
	void parametroConTipoInvalidoDevuelve400Uniforme() throws Exception {
		mvc.perform(get("/api/subastas?page=no-es-un-numero"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors[0].field").value("page"))
				.andExpect(jsonPath("$.errors[0].code").value("INVALID_PARAMETER"));
	}
}
