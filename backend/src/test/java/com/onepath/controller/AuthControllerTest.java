package com.onepath.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onepath.config.SecurityConfig;
import com.onepath.dto.AuthResponse;
import com.onepath.dto.LoginRequest;
import com.onepath.entity.Role;
import com.onepath.exception.GlobalExceptionHandler;
import com.onepath.repository.UserRepository;
import com.onepath.security.CustomUserDetailsService;
import com.onepath.security.JwtAuthenticationFilter;
import com.onepath.security.JwtUtils;
import com.onepath.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /auth/login reaches AuthController without being blocked by Spring Security (dual mapping)")
    void testLogin_Unprefixed_Success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test1@onepath.com");
        req.setPassword("test123");

        AuthResponse mockResponse = new AuthResponse(
                "mock-jwt-token-123",
                1L,
                "Demo Student",
                "test1@onepath.com",
                Role.USER,
                null
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test1@onepath.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login also reaches AuthController and succeeds (backward compatibility)")
    void testLogin_Prefixed_Success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@onepath.org");
        req.setPassword("admin123");

        AuthResponse mockResponse = new AuthResponse(
                "admin-jwt-token-456",
                2L,
                "Administrator",
                "admin@onepath.org",
                Role.SUPER_ADMIN,
                null
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token-456"))
                .andExpect(jsonPath("$.email").value("admin@onepath.org"))
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login with invalid credentials returns 401 Unauthorized, not 403 Forbidden")
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test1@onepath.com");
        req.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password credentials"));
    }

    @Test
    @DisplayName("POST /auth/login with invalid input format returns 400 Bad Request")
    void testLogin_InvalidInputFormat_Returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("not-a-valid-email");
        req.setPassword("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
