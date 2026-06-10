package com.example.quickbid.quickbid.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "app.auctions.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class AuctionTimerScheduler {
	private final AuctionTimerService timers;

	public AuctionTimerScheduler(AuctionTimerService timers) {
		this.timers = timers;
	}

	@Scheduled(fixedDelayString = "${app.auctions.scheduler-delay-ms:2000}")
	public void processDueTimers() {
		timers.processDueTimers();
	}
}
