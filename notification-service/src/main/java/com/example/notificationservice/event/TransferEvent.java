package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@ToString
public abstract class TransferEvent {
    private final String transactionId;
    private final Long fromWalletId;
    private final Long toWalletId;
    private final BigDecimal amount;
    private final Long fromUserId;
    private final Long toUserId;
    private final String description;
}
