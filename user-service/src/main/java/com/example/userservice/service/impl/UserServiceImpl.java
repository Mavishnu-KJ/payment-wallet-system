package com.example.userservice.service.impl;

import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;
import com.example.userservice.model.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    public UserResponseDto register(RegisterRequestDto registerRequestDto){
        logger.info("register, registerRequestDto is {}", registerRequestDto);

        //Check duplicates
        if(userRepository.existsByUserName(registerRequestDto.getUserName())){
            throw new RuntimeException("userName already exists");
        }
        if(userRepository.existsByEmail(registerRequestDto.getEmail())){
            throw new RuntimeException("email already exists");
        }

        //Map request Dto to Entity
        User user = modelMapper.map(registerRequestDto, User.class);

        //Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("register, user is {}", user);

        //Save
        User savedUser = userRepository.save(user);
        logger.info("register, savedUser is {}", savedUser);

        //Map Entity to Response Dto
        UserResponseDto userResponseDto = modelMapper.map(savedUser, UserResponseDto.class);
        logger.info("register, userResponseDto is {}", userResponseDto);

        return userResponseDto;
    }


}
