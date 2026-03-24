package com.example.walletservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;

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

}
