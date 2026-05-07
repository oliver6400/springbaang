package com.codesfree.prueba.config;

import com.codesfree.prueba.model.AppRole;
import com.codesfree.prueba.model.AppUser;
import com.codesfree.prueba.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SuperAdminBootstrapConfig {

    @Bean
    CommandLineRunner superAdminBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String username = System.getenv().getOrDefault("BOOTSTRAP_SUPERADMIN_USERNAME", "superadmin");
            String password = System.getenv().getOrDefault("BOOTSTRAP_SUPERADMIN_PASSWORD", "ChangeMe123!");

            if (!appUserRepository.existsByUsername(username)) {
                AppUser user = new AppUser();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(AppRole.ROLE_SUPERADMIN);
                appUserRepository.save(user);
            }
        };
    }
}
