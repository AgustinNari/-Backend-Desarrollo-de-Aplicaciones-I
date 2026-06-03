package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConsignmentReturnRequest(
		@NotBlank String modalidad,
		String direccion,
		String piso,
		String codigoPostal,
		String localidad,
		String provincia,
		String telefonoContacto) {
}
