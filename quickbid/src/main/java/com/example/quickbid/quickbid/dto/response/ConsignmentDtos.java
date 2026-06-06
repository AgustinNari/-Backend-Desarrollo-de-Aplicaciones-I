package com.example.quickbid.quickbid.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class ConsignmentDtos {
	private ConsignmentDtos() {
	}

	public record Requirement(String codigo, String descripcion, Boolean cumplido) {
	}

	public record Requirements(Boolean puedeContinuar, List<Requirement> requisitos, Integer minimoFotos) {
	}

	public record Summary(Long id, String titulo, String estado, BigDecimal valorBase, String moneda,
			String accionPendiente, Long fotoPrincipalArchivoId, OffsetDateTime updatedAt) {
	}

	public record Detail(Long id, String titulo, String descripcion, String segmento, String categoriaSubasta,
			String categoriaSugerida, String historia, String artistaDisenador, String fechaObjeto, String estado,
			Boolean requiereDocumentacionOrigen, String motivoRechazo, Integer productoId, Integer itemCatalogoId,
			Integer subastaId, BigDecimal valorBase, String moneda, BigDecimal comisionCompradorPct,
			BigDecimal comisionVendedorPct, BigDecimal netoEstimado, String acuerdoTexto, String ubicacionFisica,
			Policy poliza, List<File> fotos, List<File> documentosOrigen, Return devolucion, Liquidation liquidacion,
			OffsetDateTime createdAt, OffsetDateTime updatedAt) {
	}

	public record File(Long archivoId, String filename, String contentType, Long sizeBytes, String estado) {
	}

	public record Return(Long id, String modalidad, BigDecimal costo, String moneda, String estado, Long pagoId) {
	}

	public record ReturnPayment(Long id, Long devolucionId, Long medioPagoId, BigDecimal monto, String moneda,
			String estado, Boolean idempotentReplay) {
	}

	public record Policy(String numero, String compania, Boolean combinada, BigDecimal importe,
			String ubicacionFisica) {
	}

	public record Liquidation(Long id, Long compraId, BigDecimal montoBruto, BigDecimal comision,
			BigDecimal montoNeto, String cuentaDestino, String estado, OffsetDateTime paidAt) {
	}

	public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
	}
}
