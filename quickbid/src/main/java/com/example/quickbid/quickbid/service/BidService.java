package com.example.quickbid.quickbid.service;

import org.springframework.stereotype.Service;

import com.example.quickbid.quickbid.audit.AuditEvent;
import com.example.quickbid.quickbid.audit.AuditService;
import com.example.quickbid.quickbid.dto.request.BidRequest;
import com.example.quickbid.quickbid.dto.response.SubastaDtos.Bid;
import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.websocket.BidRealtimePublisher;

@Service
public class BidService {
	private final BidTransactionService transactions;
	private final AuditService audit;
	private final BidRealtimePublisher realtime;

	public BidService(BidTransactionService transactions, AuditService audit, BidRealtimePublisher realtime) {
		this.transactions = transactions;
		this.audit = audit;
		this.realtime = realtime;
	}

	public Bid bid(Long accountId, Integer auctionId, BidRequest request) {
		try {
			BidTransactionService.AcceptedBid accepted = transactions.bid(accountId, auctionId, request);
			if (!accepted.replay()) realtime.afterCommit(accepted.response(), accountId, accepted.surpassedAccountId());
			return accepted.response();
		} catch (BusinessException exception) {
			String code = exception.getErrors().get(0).code();
			audit.record(new AuditEvent("usuario", accountId, "subasta.puja_rechazada", "subasta",
					auctionId.longValue(), "{\"code\":\"" + code + "\"}"));
			realtime.publishRejected(accountId, auctionId, request.itemCatalogoId(), code, exception.getMessage());
			throw exception;
		}
	}
}
