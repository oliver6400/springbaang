package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.BootstrapSuperAdminRequest;
import com.codesfree.prueba.dto.BootstrapSuperAdminResponse;
import com.codesfree.prueba.service.SuperAdminBootstrapService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final SuperAdminBootstrapService superAdminBootstrapService;

    public BootstrapController(SuperAdminBootstrapService superAdminBootstrapService) {
        this.superAdminBootstrapService = superAdminBootstrapService;
    }

    @PostMapping("/superadmin")
    public ResponseEntity<BootstrapSuperAdminResponse> createFirstSuperAdmin(
            @Valid @RequestBody BootstrapSuperAdminRequest request) {
        BootstrapSuperAdminResponse createdUser = superAdminBootstrapService.createFirstSuperAdmin(request);
        return ResponseEntity.ok(createdUser);
    }
}
