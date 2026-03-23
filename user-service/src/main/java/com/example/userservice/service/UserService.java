package com.example.userservice.service;

import com.example.userservice.model.dto.LoginRequestDto;
import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto register(RegisterRequestDto registerRequestDto);
    String generateToken(LoginRequestDto loginRequestDto);
    UserResponseDto getUserById(Long id);
    List<UserResponseDto> getAllUsers();

    UserResponseDto getCurrentUser();


}
