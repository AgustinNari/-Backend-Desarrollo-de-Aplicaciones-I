package com.example.quickbid.quickbid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailService implements MailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SmtpMailService.class);

	private final JavaMailSender sender;
	private final MailTemplates templates;
	private final String from;

	public SmtpMailService(JavaMailSender sender, MailTemplates templates,
			@Value("${app.mail.from}") String from) {
		this.sender = sender;
		this.templates = templates;
		this.from = required(from, "APP_MAIL_FROM");
	}

	@Override
	public void sendToken(String purpose, String recipient, String token) {
		send(recipient, templates.token(purpose, token), purpose);
	}

	@Override
	public void sendNotification(String recipient, String type) {
		send(recipient, templates.notification(type), type);
	}

	private void send(String recipient, MailTemplates.Message template, String type) {
		String to = required(recipient, "destinatario");
		if (!to.contains("@")) throw new MailDeliveryException("Destinatario de correo invalido");
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(template.subject());
			message.setText(template.body());
			sender.send(message);
		} catch (MailException exception) {
			LOGGER.warn("SMTP delivery failed type={}", type);
			throw new MailDeliveryException("No se pudo enviar el correo", exception);
		}
	}

	private String required(String value, String field) {
		if (value == null || value.isBlank()) throw new MailDeliveryException("Falta configurar " + field);
		return value.trim();
	}
}
