package com.tmc.access.qrcode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tmc.access.decision.AccessRejectedException;
import com.tmc.access.decision.VerifiedCredential;
import com.tmc.access.user.AppUser;

class QrCredentialVerifierTest {
    private static final Instant NOW = Instant.parse("2026-11-13T01:00:00Z");
    private static final String CONTENT = "scanned-token";

    private final QrCodeRepository qrCodeRepository = mock(QrCodeRepository.class);
    private final QrCredentialVerifier verifier = new QrCredentialVerifier(qrCodeRepository);

    private AppUser appUser;
    private QrCode qrCode;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(UUID.randomUUID());
        appUser.setActive(true);

        qrCode = new QrCode();
        qrCode.setId(UUID.randomUUID());
        qrCode.setAppUser(appUser);
        qrCode.setContent(CONTENT);
        qrCode.setExpiresAt(NOW.plusSeconds(30));
        qrCode.setValid(true);

        when(qrCodeRepository.findByContent(CONTENT)).thenReturn(Optional.of(qrCode));
        when(qrCodeRepository.invalidate(qrCode.getId())).thenReturn(1);
    }

    @Test
    void validQrCodeIsUsedUpAndGivesItsUser() {
        VerifiedCredential verified = verifier.verify(CONTENT, NOW);

        assertEquals(qrCode.getId(), verified.credentialId());
        assertEquals(appUser, verified.appUser());
    }

    @Test
    void unknownQrCodeIsRejected() {
        assertThrows(AccessRejectedException.class, () -> verifier.verify("other-token", NOW));
    }

    @Test
    void qrCodeIsExpiredAtItsExpiryInstant() {
        qrCode.setExpiresAt(NOW);

        assertThrows(AccessRejectedException.class, () -> verifier.verify(CONTENT, NOW));
    }

    @Test
    void qrCodeUsedOnceIsRejectedTheSecondTime() {
        when(qrCodeRepository.invalidate(qrCode.getId())).thenReturn(0);

        assertThrows(AccessRejectedException.class, () -> verifier.verify(CONTENT, NOW));
    }
}
