package com.codesfree.prueba.repository;

import com.codesfree.prueba.model.AppUser;
import com.codesfree.prueba.model.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByAppUserAndRevokedFalse(AppUser appUser);
}
