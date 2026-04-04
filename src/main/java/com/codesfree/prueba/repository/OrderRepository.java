package com.codesfree.prueba.repository;

import com.codesfree.prueba.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTenantId(Long tenantId);
}

