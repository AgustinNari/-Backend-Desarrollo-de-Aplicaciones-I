package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConsignmentReturnRequest(
		@NotBlank String modalidad,
		Long direccionEnvioId,
		String direccion,
		String piso,
		String codigoPostal,
		String localidad,
		String provincia,
		String telefonoContacto) {
	public ConsignmentReturnRequest(String modalidad, String direccion, String piso, String codigoPostal,
			String localidad, String provincia, String telefonoContacto) {
		this(modalidad, null, direccion, piso, codigoPostal, localidad, provincia, telefonoContacto);
	}
}
