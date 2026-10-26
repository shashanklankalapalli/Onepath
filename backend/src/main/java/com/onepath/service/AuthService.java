package com.onepath.service;

import com.onepath.dto.AuthResponse;
import com.onepath.dto.LoginRequest;
import com.onepath.dto.RegisterRequest;
import com.onepath.dto.UserDto;
import com.onepath.entity.Role;
import com.onepath.entity.User;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.UserRepository;
import com.onepath.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + req.getEmail());
        }

        Role role = Role.USER;
        if (req.getRole() != null && !req.getRole().isBlank()) {
            try {
                role = Role.valueOf(req.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                role = Role.USER;
            }
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);
        user.setAssignedCollegeId(req.getAssignedCollegeId());

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getAssignedCollegeId()
        );
    }

    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAssignedCollegeId()
        );
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAssignedCollegeId(),
                user.getCreatedAt()
        );
    }
}
