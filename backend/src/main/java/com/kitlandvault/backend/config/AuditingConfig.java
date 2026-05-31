package com.kitlandvault.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kitlandvault.backend.repositories.UserRepository;
import com.kitlandvault.backend.entities.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class AuditingConfig {

    private final UserRepository userRepository;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof Long) {
                        return userRepository.findById((Long) principal)
                                .map(User::getUsername)
                                .orElse("SYSTEM");
                    }
                    if (principal instanceof String) {
                        try {
                            Long id = Long.parseLong((String) principal);
                            return userRepository.findById(id)
                                    .map(User::getUsername)
                                    .orElse((String) principal);
                        } catch (NumberFormatException e) {
                            return (String) principal;
                        }
                    }
                    return auth.getName();
                })
                .or(() -> Optional.of("SYSTEM"));
    }
}
