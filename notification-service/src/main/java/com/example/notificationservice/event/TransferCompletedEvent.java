package com.example.notificationservice.event;

import com.example.notificationservice.event.TransferEvent;

import java.math.BigDecimal;

public class TransferCompletedEvent extends TransferEvent {
    public TransferCompletedEvent(String transactionId, Long fromWalletId, Long toWalletId,
                                  BigDecimal amount, Long fromUserId, Long toUserId, String description) {
        super(transactionId, fromWalletId, toWalletId, amount, fromUserId, toUserId, description);
    }
}
