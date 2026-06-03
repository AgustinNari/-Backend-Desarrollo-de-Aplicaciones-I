package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PurchasePaymentRequest(
		@NotNull Long medioPagoId,
		@Size(max = 100) String idempotencyKey) {
}
