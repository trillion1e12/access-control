package com.tmc.access.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

/** Read-only access to users. */
public interface UserRepository extends Repository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findFirstByActiveTrue();
}
