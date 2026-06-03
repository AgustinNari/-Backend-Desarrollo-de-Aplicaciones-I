package com.example.quickbid.quickbid.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BidRequest(
		@NotNull Integer itemCatalogoId,
		@NotNull @DecimalMin("0.01") BigDecimal monto,
		@NotNull Long medioPagoId,
		@NotNull @PositiveOrZero Long clientStateVersion,
		@Size(max = 100) String idempotencyKey) {
}
