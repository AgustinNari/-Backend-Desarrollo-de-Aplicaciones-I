package com.example.quickbid.quickbid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class SimulatedMailService implements MailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedMailService.class);
	private static final String REDACTED_TOKEN = "token-redactado";

	private final MailTemplates templates;
	private final List<Delivery> deliveries = new CopyOnWriteArrayList<>();

	public SimulatedMailService(MailTemplates templates) {
		this.templates = templates;
	}

	@Override
	public void sendToken(String purpose, String email, String token) {
		MailTemplates.Message template = templates.token(purpose, REDACTED_TOKEN);
		deliveries.add(new Delivery("token", purpose, email, template.subject(), template.body()));
		LOGGER.warn("SIMULATED LOCAL MAIL kind=token purpose={} email={} token delivery omitted because real mail is disabled",
				purpose, email);
	}

	@Override
	public void sendNotification(String email, String type) {
		MailTemplates.Message template = templates.notification(type);
		deliveries.add(new Delivery("notification", type, email, template.subject(), template.body()));
		LOGGER.warn("SIMULATED LOCAL MAIL kind=notification purpose={} email={} because real mail is disabled", type, email);
	}

	public List<Delivery> deliveries() {
		return List.copyOf(deliveries);
	}

	public void clear() {
		deliveries.clear();
	}

	public record Delivery(String kind, String purpose, String recipient, String subject, String preview) {
	}
}
