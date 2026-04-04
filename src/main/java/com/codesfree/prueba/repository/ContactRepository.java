package com.codesfree.prueba.repository;

import com.codesfree.prueba.model.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByTenantId(Long tenantId);
}
