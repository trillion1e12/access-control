package com.tmc.access.qrcode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.tmc.access.user.AppUser;
import com.tmc.access.user.UserNotActiveException;
import com.tmc.access.user.UserRepository;

class QrCodeServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-13T01:00:00Z");
    private static final String EMAIL = "user@tmc.com";

    private final QrCodeRepository qrCodeRepository = mock(QrCodeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final QrCodeService qrCodeService = new QrCodeService(
            qrCodeRepository,
            userRepository,
            () -> "fixed-token",
            content -> content.getBytes(),
            Clock.fixed(NOW, ZoneOffset.UTC),
            60
    );

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(UUID.randomUUID());
        appUser.setEmail(EMAIL);
        appUser.setActive(true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(appUser));
    }

    @Test
    void issuesValidQrCodeThatExpiresAfterTimeToLive() {
        byte[] image = qrCodeService.getQrCode(EMAIL);

        ArgumentCaptor<QrCode> saved = ArgumentCaptor.forClass(QrCode.class);
        verify(qrCodeRepository).save(saved.capture());
        assertEquals("fixed-token", saved.getValue().getContent());
        assertEquals(NOW.plusSeconds(60), saved.getValue().getExpiresAt());
        assertTrue(saved.getValue().isValid());
        assertEquals("fixed-token", new String(image));
    }

    @Test
    void doesNotIssueQrCodeForInactiveUser() {
        appUser.setActive(false);

        assertThrows(UserNotActiveException.class, () -> qrCodeService.getQrCode(EMAIL));
    }

    @Test
    void doesNotIssueQrCodeForUnknownUser() {
        assertThrows(UserNotActiveException.class, () -> qrCodeService.getQrCode("nobody@tmc.com"));
    }
}
