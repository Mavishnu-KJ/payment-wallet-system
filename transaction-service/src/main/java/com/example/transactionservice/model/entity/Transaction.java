package com.example.transactionservice.model.entity;

import com.example.transactionservice.model.enums.TransactionStatus;
import com.example.transactionservice.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
        indexes = {
            @Index(name = "idx_transaction_from_wallet", columnList = "fromWalletId"),
            @Index(name = "idx_transaction_to_wallet", columnList = "toWalletId"),
            @Index(name = "idx_transaction_date", columnList = "transactionDate")
        })
@Data
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 36)
    private String transactionId;           // UUID or custom txn ID (e.g., TXN123456789)

    @Column(nullable = false)
    private Long fromWalletId;

    @Column(nullable = false)
    private Long toWalletId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;           // TRANSFER, ADD_MONEY, WITHDRAWAL, REFUND

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String reference;               // UPI ref, payment gateway ref, etc.

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

}
