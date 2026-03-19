package com.example.userservice.service;

import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;

public interface UserService {

    UserResponseDto register(RegisterRequestDto registerRequestDto);

}
