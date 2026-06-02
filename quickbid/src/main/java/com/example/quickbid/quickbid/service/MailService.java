package com.example.quickbid.quickbid.service;

public interface MailService {
	void sendToken(String purpose, String recipient, String token);

	void sendNotification(String recipient, String type);
}
