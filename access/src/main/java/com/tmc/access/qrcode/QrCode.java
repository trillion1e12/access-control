package com.tmc.access.qrcode;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.tmc.access.user.AppUser;

@Entity
@Table(name = "qr_codes")
public class QrCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser appUser;

    @Column(unique = true, nullable = false)
    private String content;

    private Instant createdAt;

    private Instant expiresAt;

    private boolean valid;

    public static Builder builder() {
        return new Builder();
    }

    /** Builds a QR code that has not been used yet. */
    public static class Builder {
        private AppUser appUser;
        private String content;
        private Instant createdAt;
        private Instant expiresAt;

        public Builder appUser(AppUser appUser) {
            this.appUser = appUser;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public QrCode build() {
            QrCode qrCode = new QrCode();
            qrCode.setAppUser(appUser);
            qrCode.setContent(content);
            qrCode.setCreatedAt(createdAt);
            qrCode.setExpiresAt(expiresAt);
            qrCode.setValid(true);
            return qrCode;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

}
