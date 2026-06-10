package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DireccionEnvioRequest(
		@NotBlank String alias,
		@NotBlank String destinatario,
		@NotBlank String calle,
		@NotBlank String numero,
		String piso,
		@NotBlank String codigoPostal,
		@NotBlank String localidad,
		@NotBlank String provincia,
		@NotBlank String pais,
		String telefono) {
}
