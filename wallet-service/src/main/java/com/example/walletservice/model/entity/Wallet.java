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
    private Long userId;                        // Foreign key to User Service

    @Column(nullable = false)
    private Double balance = 0.0;               // Default value

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)// Always use STRING for enums in production
    private WalletStatus status = WalletStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking

    //Optional: Helper method
    public boolean isActive() {
        return this.status == WalletStatus.ACTIVE;
    }

}
