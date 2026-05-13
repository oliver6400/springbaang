package com.codesfree.prueba.dto;

import java.util.List;

public class AuthResponse {

    private final String username;
    private final String role;
    private final List<String> authorities;

    public AuthResponse(String username, String role, List<String> authorities) {
        this.username = username;
        this.role = role;
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public List<String> getAuthorities() {
        return authorities;
    }
}
