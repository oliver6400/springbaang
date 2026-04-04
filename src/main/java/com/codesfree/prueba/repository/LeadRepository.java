package com.codesfree.prueba.repository;

import com.codesfree.prueba.model.Lead;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByTenantId(Long tenantId);
}
