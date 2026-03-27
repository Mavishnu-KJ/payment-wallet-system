package com.example.walletservice.model.entity;

import com.example.walletservice.model.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
public class Wallet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;                        // Link to User Service

    @Column(nullable = false)
    private Double balance;

    @Column
    private String currency = "INR";

    @Enumerated(EnumType.STRING) // Always use STRING for enums in production
    private WalletStatus walletStatus = WalletStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking

}
