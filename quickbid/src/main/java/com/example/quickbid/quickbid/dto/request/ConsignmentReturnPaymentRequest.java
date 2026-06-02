package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConsignmentReturnPaymentRequest(@NotNull Long medioPagoId, String idempotencyKey) {
}
