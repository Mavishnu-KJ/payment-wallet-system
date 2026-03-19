package com.example.userservice.controller;

import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto){
        logger.info("register, RegisterRequestDto is {}", registerRequestDto);

        UserResponseDto userResponseDto = userService.register(registerRequestDto);
        logger.info("register, userResponseDto is {}", userResponseDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("/api/users/{id}")
                .buildAndExpand(userResponseDto.getId())
                .toUri();
        logger.info("register, location is {}", location);

        return ResponseEntity.created(location).body(userResponseDto);
    }

}
