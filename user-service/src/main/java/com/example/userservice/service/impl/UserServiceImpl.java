package com.example.userservice.service.impl;

import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(RegisterRequestDto registerRequestDto){


        //TEMP
        return new UserResponseDto();
    }


}
