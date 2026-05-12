package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.AuthLoginRequest;
import com.codesfree.prueba.dto.AuthResponse;
import com.codesfree.prueba.dto.AuthTokenResponse;
import com.codesfree.prueba.dto.LogoutRequest;
import com.codesfree.prueba.dto.RefreshTokenRequest;
import com.codesfree.prueba.service.RefreshTokenService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody AuthLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));
        return refreshTokenService.issueTokens(authentication.getName());
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return Map.of("message", "Session closed successfully");
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return refreshTokenService.getProfile(authentication.getName());
    }
}
