package com.example.quickbid.quickbid.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.quickbid.quickbid.repository.app.CuentaAppRepository;

@Service
public class MailNotificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationService.class);
	private static final Set<String> EMAILED_TYPES = Set.of(
			"medio_pago_verificado", "medio_pago_rechazado",
			"multa_generada", "multa_pagada", "multa_vencida",
			"lote_ganado", "pago_adjudicacion_exitoso", "entrega_pendiente", "retiro_pendiente",
			"acuerdo_disponible", "consignacion_documentacion_requerida", "consignacion_publicada",
			"liquidacion_disponible", "subasta_inscripta_proxima_inicio");

	private final CuentaAppRepository accounts;
	private final MailService mail;
	private final boolean notificationsEnabled;

	public MailNotificationService(CuentaAppRepository accounts, MailService mail,
			@Value("${app.mail.notifications-enabled:true}") boolean notificationsEnabled) {
		this.accounts = accounts;
		this.mail = mail;
		this.notificationsEnabled = notificationsEnabled;
	}

	public void critical(Long accountId, String type) {
		if (!notificationsEnabled || !EMAILED_TYPES.contains(type)) return;
		Runnable delivery = () -> accounts.findById(accountId).ifPresent(account -> {
			try {
				mail.sendNotification(account.getEmail(), type);
			} catch (MailDeliveryException exception) {
				LOGGER.warn("Critical notification email delivery failed type={} accountId={}", type, accountId);
			}
		});
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					delivery.run();
				}
			});
		} else {
			delivery.run();
		}
	}
}
