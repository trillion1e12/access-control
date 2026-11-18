package com.tmc.access.decision;

import java.time.Instant;

import com.tmc.access.user.AppUser;

/**
 * One condition a user must meet to pass, whatever kind of credential was
 * presented. Every rule must pass before a gate is opened.
 */
public interface AccessRule {
    /**
     * @throws AccessRejectedException
     *             when the condition is not met
     */
    void check(AppUser appUser, Instant now);
}
