package com.example.userservice.service.impl;

import com.example.userservice.exceptions.ResourceNotFoundException;
import com.example.userservice.model.dto.LoginRequestDto;
import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;
import com.example.userservice.model.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    @Transactional
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

    @Override
    public UserResponseDto getUserById(Long id){
        logger.info("getUserById, id is {}", id);

        User user = userRepository.findById(id)
                        .orElseThrow(()->new ResourceNotFoundException("Resource not found for "+id));

        logger.info("getUserById, user is {}", user);

        //Map entity to response dto
        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
        logger.info("getUserById, userResponseDto is {}", userResponseDto);

        return userResponseDto;
    }

    @Override
    public List<UserResponseDto> getAllUsers(){
        logger.info("getAllUsers");

        List<User> userList = userRepository.findAll();
        logger.info("getAllUsers, userList is {}", userList);

        //Map entity to response to
        List<UserResponseDto> userResponseDtoList = userList.stream()
                .filter(Objects::nonNull)
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
        logger.info("getAllUsers, userResponseDtoList is {}", userResponseDtoList);

        return userResponseDtoList;
    }

    @Override
    @Transactional
    public String generateToken(LoginRequestDto loginRequestDto){
        logger.info("generateToken, loginRequestDto is {}", loginRequestDto);

        //Check if user not found
        User user = userRepository.findByUserName(loginRequestDto.getUserName())
                        .orElseThrow(() -> new ResourceNotFoundException(loginRequestDto.getUserName()));

        //Check password match
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }

        //Generate token
        String token = jwtUtil.generateToken(user.getUserName());
        logger.info("generateToken, token is {}", token);

        return token;
    }

    @Override
    public UserResponseDto getCurrentUser(){
        logger.info("getCurrentUser");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("getCurrentUser, username is {}", username);

        //Check if user not found
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException(username));

        //Map entity to response dto
        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
        logger.info("getCurrentUser, userResponseDto is {}", userResponseDto);

        return userResponseDto;
    }


}
