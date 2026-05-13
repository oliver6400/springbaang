package com.codesfree.prueba.service;

import com.codesfree.prueba.dto.AuthResponse;
import com.codesfree.prueba.dto.AuthTokenResponse;
import com.codesfree.prueba.model.AppUser;
import com.codesfree.prueba.model.RefreshToken;
import com.codesfree.prueba.repository.AppUserRepository;
import com.codesfree.prueba.repository.RefreshTokenRepository;
import com.codesfree.prueba.security.JwtService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            AppUserRepository appUserRepository,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse issueTokens(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found in the database"));

        revokeActiveTokens(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAppUser(user);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setExpiresAt(jwtService.getRefreshTokenExpiryInstant());
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return buildTokenResponse(user, accessToken, refreshTokenValue);
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalStateException("Refresh token is not recognized"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new IllegalStateException("Refresh token expired or revoked");
        }

        AppUser user = storedToken.getAppUser();
        if (!jwtService.isRefreshTokenValid(refreshTokenValue, user)) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new IllegalStateException("Refresh token is invalid");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        return issueTokens(user.getUsername());
    }

    @Transactional
    public void revoke(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return;
        }

        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        });
    }

    public AuthResponse getProfile(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found in the database"));
        return buildProfile(user);
    }

    private AuthTokenResponse buildTokenResponse(AppUser user, String accessToken, String refreshToken) {
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                buildProfile(user));
    }

    private AuthResponse buildProfile(AppUser user) {
        return new AuthResponse(user.getUsername(), user.getRole().name(), List.of(user.getRole().name()));
    }

    private void revokeActiveTokens(AppUser user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByAppUserAndRevokedFalse(user);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.saveAll(activeTokens);
        }
    }
}
