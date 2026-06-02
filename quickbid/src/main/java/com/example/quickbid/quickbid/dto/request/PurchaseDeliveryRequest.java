package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PurchaseDeliveryRequest(
		@NotBlank @Pattern(regexp = "envio|retiro") String tipo,
		Long direccionEnvioId) {
}
