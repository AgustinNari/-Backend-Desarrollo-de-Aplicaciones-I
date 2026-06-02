package com.example.quickbid.quickbid.dto.request;

import jakarta.validation.constraints.AssertTrue;

public record ConsignmentAgreementAcceptanceRequest(
		@AssertTrue Boolean leyoContrato,
		@AssertTrue Boolean aceptaClausulasPlazos) {
}
