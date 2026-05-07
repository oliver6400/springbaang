package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.BootstrapSuperAdminRequest;
import com.codesfree.prueba.service.SuperAdminBootstrapService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SuperAdminBootstrapService superAdminBootstrapService;

    public AuthController(SuperAdminBootstrapService superAdminBootstrapService) {
        this.superAdminBootstrapService = superAdminBootstrapService;
    }

    @PostMapping("/bootstrap-superadmin")
    public ResponseEntity<Map<String, String>> bootstrapSuperAdmin(
            @Valid @RequestBody BootstrapSuperAdminRequest request) {
        superAdminBootstrapService.createFirstSuperAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Superadmin created successfully"));
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
