package com.onepath.service;

import com.onepath.entity.Role;
import com.onepath.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class AuthAccountTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Verify Student account password hashing and verification")
    void testStudentAccountCredentials() {
        String rawPassword = "test123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches("test123", encoded), "Correct password must match");
        assertFalse(passwordEncoder.matches("wrongpass", encoded), "Incorrect password must be rejected");
        assertFalse(passwordEncoder.matches("Test123", encoded), "Case-sensitive password must be rejected");

        User student = new User("Student", "test1@onepath.com", encoded, Role.USER);
        assertEquals("test1@onepath.com", student.getEmail());
        assertEquals(Role.USER, student.getRole());
        assertTrue(passwordEncoder.matches(rawPassword, student.getPasswordHash()));

        System.out.println("TEST123_BCRYPT=" + encoded);
    }

    @Test
    @DisplayName("Verify Admin account password hashing and verification")
    void testAdminAccountCredentials() {
        String rawPassword = "admin123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches("admin123", encoded), "Correct password must match");
        assertFalse(passwordEncoder.matches("wrongpassword", encoded), "Incorrect password must be rejected");

        User admin = new User("Administrator", "admin@onepath.com", encoded, Role.SUPER_ADMIN);
        assertEquals("admin@onepath.com", admin.getEmail());
        assertEquals(Role.SUPER_ADMIN, admin.getRole());
        assertTrue(passwordEncoder.matches(rawPassword, admin.getPasswordHash()));

        System.out.println("ADMIN123_BCRYPT=" + encoded);
    }
}
