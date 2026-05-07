package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.BootstrapSuperAdminRequest;
import com.codesfree.prueba.service.SuperAdminBootstrapService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String, String>> createFirstSuperAdmin(
            @Valid @RequestBody BootstrapSuperAdminRequest request) {
        superAdminBootstrapService.createFirstSuperAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Superadmin created successfully"));
    }
}
