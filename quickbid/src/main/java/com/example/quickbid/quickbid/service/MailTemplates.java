package com.example.quickbid.quickbid.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailTemplates {
	private static final Map<String, Template> NOTIFICATIONS = Map.ofEntries(
			Map.entry("medio_pago_verificado", new Template("Medio de pago verificado",
					"Tu medio de pago fue verificado y ya puede utilizarse en QuickBid.")),
			Map.entry("medio_pago_rechazado", new Template("Medio de pago rechazado",
					"Revisa los datos de tu medio de pago desde la app QuickBid.")),
			Map.entry("multa_generada", new Template("Multa pendiente",
					"Se genero una multa asociada a una compra. Revisa el detalle y el plazo desde la app.")),
			Map.entry("multa_pagada", new Template("Multa pagada",
					"Registramos la regularizacion de tu multa.")),
			Map.entry("multa_vencida", new Template("Multa vencida",
					"Tu multa vencio y la cuenta quedo bloqueada. Revisa el detalle desde la app.")),
			Map.entry("lote_ganado", new Template("Ganaste un lote",
					"Ganaste un lote subastado. Revisa tu compra y los pasos pendientes desde la app.")),
			Map.entry("pago_adjudicacion_exitoso", new Template("Pago de adjudicacion aprobado",
					"El pago de adjudicacion fue aprobado. Revisa los pagos extra pendientes desde la app.")),
			Map.entry("entrega_pendiente", new Template("Entrega pendiente",
					"El pago de extras fue aprobado. Revisa el estado de entrega desde la app.")),
			Map.entry("retiro_pendiente", new Template("Retiro pendiente",
					"El pago de extras fue aprobado. Revisa el retiro pendiente desde la app.")),
			Map.entry("acuerdo_disponible", new Template("Acuerdo de consignacion disponible",
					"Ya puedes revisar el acuerdo propuesto para tu consignacion.")),
			Map.entry("consignacion_documentacion_requerida", new Template("Documentacion adicional requerida",
					"Debes adjuntar documentacion adicional para continuar con tu consignacion.")),
			Map.entry("consignacion_publicada", new Template("Articulo publicado",
					"Tu articulo consignado fue asignado a una subasta.")),
			Map.entry("liquidacion_disponible", new Template("Liquidacion disponible",
					"La liquidacion de tu articulo consignado ya fue emitida.")),
			Map.entry("subasta_inscripta_proxima_inicio", new Template("Tu subasta inscripta esta por iniciar",
					"Una subasta en la que manifestaste interes ya esta disponible en vivo.")));

	private final String frontendBaseUrl;

	public MailTemplates(@Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl) {
		this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
	}

	public Message token(String purpose, String token) {
		String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
		return switch (purpose) {
			case "registro" -> new Message("Completa tu registro en QuickBid",
					"Completa tu registro y configura tu clave desde este enlace:\n"
							+ frontendBaseUrl + "/completar-registro?token=" + encoded
							+ "\n\nSi no solicitaste esto, podes ignorar este mensaje.");
			case "recuperacion" -> new Message("Recupera tu clave de QuickBid",
					"Configura una nueva clave desde este enlace:\n"
							+ frontendBaseUrl + "/recuperar-clave?token=" + encoded
							+ "\n\nSi no solicitaste esto, podes ignorar este mensaje.");
			default -> throw new IllegalArgumentException("Tipo de token de mail desconocido");
		};
	}

	public Message notification(String type) {
		Template value = NOTIFICATIONS.get(type);
		if (value == null) throw new IllegalArgumentException("Tipo de notificacion de mail desconocido");
		return new Message(value.subject(), value.body());
	}

	public record Message(String subject, String body) {
	}

	private record Template(String subject, String body) {
	}
}
