package com.example.transactionservice.event;

import java.math.BigDecimal;

public class TransferFailedEvent extends TransferEvent{
    public TransferFailedEvent(String transactionId, Long fromWalletId, Long toWalletId,
                               BigDecimal amount, Long fromUserId, Long toUserId, String description) {
        super(transactionId, fromWalletId, toWalletId, amount, fromUserId, toUserId, description);
    }
}
