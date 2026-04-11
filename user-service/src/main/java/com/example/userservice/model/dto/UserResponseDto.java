package com.example.userservice.model.dto;

import com.example.userservice.model.enums.UserRole;
import com.example.userservice.model.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private Long userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole userRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

}
