package com.example.userservice.repository;

import com.example.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUserName(String username);
    boolean existsByEmail(String email);

}
