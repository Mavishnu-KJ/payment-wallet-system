package com.example.userservice.controller;

import com.example.userservice.model.dto.LoginRequestDto;
import com.example.userservice.model.dto.RegisterRequestDto;
import com.example.userservice.model.dto.UserResponseDto;
import com.example.userservice.model.entity.User;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
                .replacePath("/api/users/{userId}")
                .buildAndExpand(userResponseDto.getId())
                .toUri();
        logger.info("register, location is {}", location);

        return ResponseEntity.created(location).body(userResponseDto);
    }

    @GetMapping("/{userId}")
    ResponseEntity<UserResponseDto> getUserById(@PathVariable(name = "userId") Long id){
        logger.info("getUserById, id is {}", id);

        UserResponseDto userResponseDto = userService.getUserById(id);
        logger.info("getUserById, userResponseDto is {}", userResponseDto);

        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping
    ResponseEntity<List<UserResponseDto>> getAllUsers(){
        logger.info("getAllUsers");

        List<UserResponseDto> userResponseDtoList = userService.getAllUsers();
        logger.info("getAllUsers, userResponseDtoList is {}", userResponseDtoList);

        return ResponseEntity.ok(userResponseDtoList);
    }

    @PostMapping("/login")
    ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        logger.info("login, loginRequestDto is {}", loginRequestDto);

        String token = userService.generateToken(loginRequestDto);
        logger.info("login, token is {}", token);

        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        logger.info("getCurrentUser");

        UserResponseDto userResponseDto = userService.getCurrentUser();
        logger.info("getCurrentUser, userResponseDto is {}", userResponseDto);

        return ResponseEntity.ok(userResponseDto);
    }

}
