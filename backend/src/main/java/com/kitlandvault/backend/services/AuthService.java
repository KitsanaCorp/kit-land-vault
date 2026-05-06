package com.kitlandvault.backend.services;

import com.kitlandvault.backend.dto.AuthResponse;
import com.kitlandvault.backend.dto.LoginRequest;
import com.kitlandvault.backend.dto.RegisterRequest;
import com.kitlandvault.backend.entities.FamilyGroup;
import com.kitlandvault.backend.entities.User;
import com.kitlandvault.backend.repositories.UserRepository;
import com.kitlandvault.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EntityManager entityManager;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create or find family group
        FamilyGroup family = FamilyGroup.builder()
                .name(request.getFamilyName() != null ? request.getFamilyName() : request.getUsername() + "'s Family")
                .build();
        entityManager.persist(family);

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .familyGroup(family)
                .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
