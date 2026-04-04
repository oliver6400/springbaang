package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.TenantDto;
import com.codesfree.prueba.model.Subscription;
import com.codesfree.prueba.model.Tenant;
import com.codesfree.prueba.service.TenantService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody TenantDto tenantDto) {
        Tenant tenant = new Tenant();
        tenant.setName(tenantDto.getName());
        tenant.setDomain(tenantDto.getDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenant(tenant));
    }

    @GetMapping
    public List<Tenant> listTenants() {
        return tenantService.getAllTenants();
    }

    @PostMapping("/{tenantId}/subscriptions")
    public ResponseEntity<Subscription> createSubscription(
            @PathVariable Long tenantId,
            @RequestBody Subscription subscription) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.createSubscription(tenantId, subscription));
    }
}

