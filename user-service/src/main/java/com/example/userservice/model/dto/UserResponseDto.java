package com.example.userservice.model.dto;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String userName;
    private String password;
    private String email;
    private String fullName;
    private String role;

}
