package com.tmc.access.decision;

import java.util.UUID;

import com.tmc.access.user.AppUser;

/**
 * The outcome of a successful credential check.
 *
 * @param credentialId
 *            identifies this use of the credential; it is the id of the
 *            attendance event that the use produces
 * @param appUser
 *            the user the credential belongs to
 */
public record VerifiedCredential(UUID credentialId, AppUser appUser) {
}
