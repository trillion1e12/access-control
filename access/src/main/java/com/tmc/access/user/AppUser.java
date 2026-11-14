package com.tmc.access.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The part of a user that issuing and checking QR codes needs: identity and
 * whether the account is active.
 */
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    private UUID id;

    @Column(unique = true)
    private String email;

    private boolean active;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
