package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ConsignmentAgreementAcceptanceRequest(
		@NotNull @AssertTrue Boolean leyoContrato,
		@NotNull @AssertTrue Boolean aceptaClausulasPlazos) {
}
