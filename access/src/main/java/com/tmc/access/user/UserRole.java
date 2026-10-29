package com.tmc.access.user;

public enum UserRole {
    USER, ADMIN;

    public String toAuthorityString() {
        return "ROLE_" + toString();
    }
}
