package com.codesfree.prueba.repository;

import com.codesfree.prueba.model.AppRole;
import com.codesfree.prueba.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(AppRole role);
}
