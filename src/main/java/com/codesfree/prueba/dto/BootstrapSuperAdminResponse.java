package com.codesfree.prueba.dto;

import java.time.Instant;

public class BootstrapSuperAdminResponse {

    private Long id;
    private String username;
    private String role;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;

    public BootstrapSuperAdminResponse() {
    }

    public BootstrapSuperAdminResponse(
            Long id,
            String username,
            String role,
            Boolean activo,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.activo = activo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
