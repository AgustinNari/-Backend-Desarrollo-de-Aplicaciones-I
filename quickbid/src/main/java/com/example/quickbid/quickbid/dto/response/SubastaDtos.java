package com.example.quickbid.quickbid.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class SubastaDtos {
	private SubastaDtos() {
	}

	public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
	}

	public record PublicSummary(Integer id, String titulo, String descripcion, LocalDate fecha, LocalTime hora,
			String ubicacion, String categoria, String moneda, String segmento, String estadoOperativo) {
	}

	public record AuthenticatedSummary(Integer id, String titulo, String descripcion, LocalDate fecha, LocalTime hora,
			String ubicacion, String categoria, String moneda, String segmento, String estadoOperativo) {
	}

	public record PublicDetail(Integer id, String titulo, String descripcion, LocalDate fecha, LocalTime hora,
			String ubicacion, String categoria, String moneda, String segmento, String estadoOperativo) {
	}

	public record AuthenticatedDetail(Integer id, String titulo, String descripcion, LocalDate fecha, LocalTime hora,
			String ubicacion, String categoria, String moneda, String segmento, String estadoOperativo,
			Boolean permiteInscripcionOnline, Boolean autenticado) {
	}

	public record PublicCatalog(Integer subastaId, Integer catalogoId, String descripcion, List<PublicItem> items) {
	}

	public record AuthenticatedCatalog(Integer subastaId, Integer catalogoId, String descripcion,
			List<AuthenticatedItem> items) {
	}

	public record PublicItem(Integer id, Integer productoId, String descripcion, List<Integer> fotoIds) {
	}

	public record AuthenticatedItem(Integer id, Integer productoId, String descripcion, List<Integer> fotoIds,
			BigDecimal precioBase, BigDecimal comision) {
	}

	public record PaymentOption(Long id, String tipo, String moneda, String estado, Boolean principal,
			String aliasVisible, String ultimos4, Boolean verificacionVigente, Boolean requiereRevalidacion) {
	}

	public record Registration(Long id, Integer subastaId, Long medioPagoId, String estado, Boolean existente,
			Boolean requiereRevisionMedioPago, OffsetDateTime createdAt) {
	}

	public record Verification(
			Boolean puedeVerDetalleCompleto,
			Boolean puedeInscribirse,
			Boolean puedePujar,
			Boolean requiereLogin,
			Boolean requiereMedioPagoParaInscripcion,
			Boolean requiereMedioPagoVerificadoParaPujar,
			Boolean requiereRevalidacionMedioPagoParaPujar,
			Boolean categoriaInsuficienteParaInscripcion,
			Boolean categoriaInsuficienteParaPujar,
			Boolean monedaIncompatibleParaInscripcion,
			Boolean monedaIncompatibleParaPujar,
			Boolean cuentaRestringida,
			Boolean cuentaBloqueada,
			Boolean yaInscripto,
			Boolean inscripcionCerradaPorTiempo,
			Boolean subastaYaIniciada,
			Boolean subastaNoIniciadaParaPuja,
			Boolean sinLoteActivo,
			Boolean conectadoOParticipandoEnOtraSubasta,
			List<PaymentOption> mediosPagoCompatiblesParaInscripcion,
			List<PaymentOption> mediosPagoVerificadosVigentesCompatiblesParaPuja,
			List<PaymentOption> mediosPagoRevalidablesParaInscripcion) {
	}

	public record CurrentBid(Integer subastaId, Integer itemActivoId, BigDecimal mejorOfertaActual,
			String moneda, Long versionEstado, Boolean puedePujar, String motivo) {
	}

	public record Bid(Long id, Integer subastaId, Integer itemCatalogoId, String estado, BigDecimal monto,
			String moneda, Long secuencia, Long versionEstado, BigDecimal mejorOfertaActual, Integer numeroPostor,
			Boolean idempotentReplay) {
	}

	public record BidEvent(String tipo, Integer subastaId, Integer itemCatalogoId, Long pujaId, BigDecimal monto,
			String moneda, Long secuencia, Long versionEstado, Integer numeroPostor, String postorAlias) {
	}

	public record AuctionStateEvent(String tipo, Integer subastaId, Integer itemCatalogoActivoId,
			BigDecimal mejorOfertaActual, String moneda, Long versionEstado) {
	}

	public record RejectedBidEvent(String tipo, Integer subastaId, Integer itemCatalogoId, String code,
			String message) {
	}
}
