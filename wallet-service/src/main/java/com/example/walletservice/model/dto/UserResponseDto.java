package com.example.walletservice.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String userRole;
    private String userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

}
