package com.example.quickbid.quickbid.controller.consignaciones;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.quickbid.quickbid.dto.request.ConsignmentAgreementAcceptanceRequest;
import com.example.quickbid.quickbid.dto.request.ConsignmentReturnPaymentRequest;
import com.example.quickbid.quickbid.dto.request.ConsignmentReturnRequest;
import com.example.quickbid.quickbid.dto.response.ApiResponse;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Detail;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Page;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Requirements;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Return;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.ReturnPayment;
import com.example.quickbid.quickbid.dto.response.ConsignmentDtos.Summary;
import com.example.quickbid.quickbid.service.ConsignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consignaciones")
public class ConsignmentController {
	private final ConsignmentService consignments;

	public ConsignmentController(ConsignmentService consignments) {
		this.consignments = consignments;
	}

	@GetMapping("/requisitos")
	public ApiResponse<Requirements> requirements(Authentication authentication) {
		return ApiResponse.success(consignments.requirements(accountId(authentication)), "Estado de requisitos");
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Detail>> create(Authentication authentication,
			@RequestParam String segmento,
			@RequestParam(required = false) String categoriaSubasta,
			@RequestParam Boolean aceptaTyC,
			@RequestParam Boolean declaracionPropiedadYOrigenLicito,
			@RequestParam String titulo,
			@RequestParam String descripcion,
			@RequestParam(required = false) String historia,
			@RequestParam(required = false) String fechaAproximada,
			@RequestParam(defaultValue = "false") Boolean esObraDeArte,
			@RequestParam(required = false) String autor,
			@RequestParam(required = false) String historiaExtendida,
			@RequestPart List<MultipartFile> fotos) {
		Detail result = consignments.create(accountId(authentication), segmento, categoriaSubasta, aceptaTyC,
				declaracionPropiedadYOrigenLicito, titulo, descripcion, historia, fechaAproximada,
				esObraDeArte, autor, historiaExtendida, fotos);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Consignacion creada"));
	}

	@PostMapping(value = "/{id}/documentacion-origen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<Detail> uploadOriginDocuments(Authentication authentication, @PathVariable Long id,
			@RequestPart(required = false) MultipartFile facturaCompra,
			@RequestPart(required = false) MultipartFile certificadoAutenticidad,
			@RequestParam(required = false) String observaciones) {
		return ApiResponse.success(consignments.uploadOriginDocuments(accountId(authentication), id,
				Arrays.asList(facturaCompra, certificadoAutenticidad), observaciones), "Documentacion enviada");
	}

	@GetMapping
	public ApiResponse<Page<Summary>> list(Authentication authentication,
			@RequestParam(required = false) String filtro,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.success(consignments.list(accountId(authentication), filtro, page, size),
				"Lista de consignaciones");
	}

	@GetMapping("/{id}")
	public ApiResponse<Detail> detail(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(consignments.detail(accountId(authentication), id), "Detalle de consignacion");
	}

	@PostMapping("/{id}/acuerdo/aceptar")
	public ApiResponse<Detail> acceptAgreement(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody ConsignmentAgreementAcceptanceRequest request) {
		return ApiResponse.success(consignments.acceptAgreement(accountId(authentication), id, request),
				"Acuerdo aceptado");
	}

	@PostMapping("/{id}/acuerdo/rechazar")
	public ApiResponse<Detail> rejectAgreement(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(consignments.rejectAgreement(accountId(authentication), id),
				"Rechazo registrado");
	}

	@PostMapping("/{id}/devolucion")
	public ResponseEntity<ApiResponse<Return>> returnSelection(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody ConsignmentReturnRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
				consignments.selectReturn(accountId(authentication), id, request), "Devolucion registrada"));
	}

	@PostMapping("/{id}/devolucion/pagar-envio")
	public ApiResponse<ReturnPayment> payReturnShipping(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody ConsignmentReturnPaymentRequest request) {
		return ApiResponse.success(consignments.payReturnShipping(accountId(authentication), id, request),
				"Pago procesado");
	}

	private Long accountId(Authentication authentication) {
		return (Long) authentication.getPrincipal();
	}
}
