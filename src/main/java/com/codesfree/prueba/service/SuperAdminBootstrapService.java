package com.codesfree.prueba.service;

import com.codesfree.prueba.dto.BootstrapSuperAdminRequest;
import com.codesfree.prueba.dto.BootstrapSuperAdminResponse;
import com.codesfree.prueba.model.AppRole;
import com.codesfree.prueba.model.AppUser;
import com.codesfree.prueba.repository.AppUserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SuperAdminBootstrapService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminBootstrapService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public BootstrapSuperAdminResponse createFirstSuperAdmin(BootstrapSuperAdminRequest request) {
        if (appUserRepository.existsByRole(AppRole.ROLE_SUPERADMIN)) {
            throw new IllegalStateException("Superadmin already exists. Bootstrap is disabled.");
        }

        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Username already exists: " + request.getUsername());
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(AppRole.ROLE_SUPERADMIN);
        user.setActivo(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(user.getCreatedAt());
        AppUser savedUser = appUserRepository.save(user);

        return new BootstrapSuperAdminResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getActivo(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt());
    }
}
