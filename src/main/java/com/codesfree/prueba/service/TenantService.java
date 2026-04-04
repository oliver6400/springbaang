package com.codesfree.prueba.service;

import com.codesfree.prueba.exception.ResourceNotFoundException;
import com.codesfree.prueba.model.Subscription;
import com.codesfree.prueba.model.Tenant;
import com.codesfree.prueba.repository.SubscriptionRepository;
import com.codesfree.prueba.repository.TenantRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;

    public TenantService(TenantRepository tenantRepository, SubscriptionRepository subscriptionRepository) {
        this.tenantRepository = tenantRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Tenant createTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Subscription createSubscription(Long tenantId, Subscription subscription) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
        subscription.setTenant(tenant);
        return subscriptionRepository.save(subscription);
    }
}

