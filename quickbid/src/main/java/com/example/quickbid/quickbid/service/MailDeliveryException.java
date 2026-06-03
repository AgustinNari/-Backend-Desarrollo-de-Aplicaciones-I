package com.example.quickbid.quickbid.service;

public class MailDeliveryException extends RuntimeException {
	public MailDeliveryException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailDeliveryException(String message) {
		super(message);
	}
}
