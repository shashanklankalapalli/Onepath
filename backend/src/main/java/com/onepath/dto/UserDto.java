package com.onepath.dto;

import com.onepath.entity.Role;
import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long assignedCollegeId;
    private LocalDateTime createdAt;

    public UserDto() {
    }

    public UserDto(Long id, String name, String email, Role role, Long assignedCollegeId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.assignedCollegeId = assignedCollegeId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getAssignedCollegeId() {
        return assignedCollegeId;
    }

    public void setAssignedCollegeId(Long assignedCollegeId) {
        this.assignedCollegeId = assignedCollegeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
