package com.example.transactionservice.model.dto;

import com.example.transactionservice.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private Long userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String userRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

}
