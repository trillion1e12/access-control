package com.tmc.access.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.qrcode.QrCode;
import com.tmc.access.qrcode.QrCodeRepository;
import com.tmc.access.turnstile.TurnstileUnavailableException;
import com.tmc.access.user.AppUser;

class AccessDecisionServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-13T01:00:00Z");
    private static final String CONTENT = "scanned-token";

    private final QrCodeRepository qrCodeRepository = mock(QrCodeRepository.class);
    /** "published" and "gate" in the order they happened. */
    private final List<String> steps = new ArrayList<>();
    private final List<AttendanceMessage> published = new ArrayList<>();
    private boolean turnstileAvailable = true;

    private final AccessDecisionService accessDecisionService = new AccessDecisionService(
            qrCodeRepository,
            List.of(new QrCodeNotExpiredRule(), new ActiveUserRule()),
            direction -> {
                if (!turnstileAvailable) {
                    throw new TurnstileUnavailableException("down", null);
                }
                steps.add("gate");
            },
            message -> {
                published.add(message);
                steps.add("published");
            },
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

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
    void acceptedQrCodeStoresOneEventThenOpensGate() {
        String result = accessDecisionService.verifyQrCode(CONTENT, AttendanceType.CHECK_IN);

        assertEquals(AccessDecisionService.ACCESS_GRANTED, result);
        assertEquals(List.of("published", "gate"), steps);
        assertEquals(
                List.of(new AttendanceMessage(qrCode.getId(), appUser.getId(), AttendanceType.CHECK_IN, NOW)),
                published
        );
    }

    @Test
    void unknownQrCodeIsRejected() {
        assertRejected("other-token");
    }

    @Test
    void expiredQrCodeIsRejected() {
        qrCode.setExpiresAt(NOW);

        assertRejected(CONTENT);
    }

    @Test
    void inactiveUserIsRejected() {
        appUser.setActive(false);

        assertRejected(CONTENT);
    }

    @Test
    void qrCodeUsedOnceIsRejectedTheSecondTime() {
        when(qrCodeRepository.invalidate(qrCode.getId())).thenReturn(0);

        assertRejected(CONTENT);
    }

    @Test
    void addedRuleIsAppliedWithoutChangingTheService() {
        AccessRule denyEverything = (code, now) -> {
            throw new AccessRejectedException("denied by added rule");
        };
        AccessDecisionService withAddedRule = new AccessDecisionService(
                qrCodeRepository,
                List.of(new QrCodeNotExpiredRule(), denyEverything),
                direction -> steps.add("gate"),
                message -> steps.add("published"),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        assertThrows(
                AccessRejectedException.class,
                () -> withAddedRule.verifyQrCode(CONTENT, AttendanceType.CHECK_IN)
        );
        assertTrue(steps.isEmpty());
    }

    @Test
    void turnstileFailureIsReportedToTheCaller() {
        turnstileAvailable = false;

        assertThrows(
                TurnstileUnavailableException.class,
                () -> accessDecisionService.verifyQrCode(CONTENT, AttendanceType.CHECK_OUT)
        );
        assertEquals(List.of("published"), steps);
    }

    private void assertRejected(String content) {
        assertThrows(
                AccessRejectedException.class,
                () -> accessDecisionService.verifyQrCode(content, AttendanceType.CHECK_IN)
        );
        assertTrue(steps.isEmpty());
    }
}
