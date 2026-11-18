package com.tmc.access.decision;

import java.time.Instant;

/**
 * Checks one kind of credential, such as a QR code. A verifier answers whether
 * the credential is genuine and usable and whose it is; whether that user may
 * pass is decided afterwards by the access rules, the same way for every kind.
 */
public interface CredentialVerifier {
    /** Name by which a request selects this kind of credential. */
    String method();

    /**
     * Checks the credential and, for a kind that can be used only once, uses it up.
     *
     * @throws AccessRejectedException
     *             when the credential is unknown, expired, used up or otherwise not
     *             valid
     */
    VerifiedCredential verify(String credential, Instant now);
}
