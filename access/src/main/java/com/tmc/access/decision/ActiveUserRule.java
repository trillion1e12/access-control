package com.tmc.access.decision;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tmc.access.user.AppUser;

@Component
public class ActiveUserRule implements AccessRule {
    @Override
    public void check(AppUser appUser, Instant now) {
        if (!appUser.isActive()) {
            throw new AccessRejectedException("User is inactive");
        }
    }
}
