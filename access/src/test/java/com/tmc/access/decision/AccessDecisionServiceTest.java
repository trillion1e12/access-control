package com.tmc.access.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.turnstile.TurnstileUnavailableException;
import com.tmc.access.user.AppUser;

class AccessDecisionServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-13T01:00:00Z");
    private static final String METHOD = "TEST";
    private static final String CREDENTIAL = "presented-credential";

    /** "published" and "gate" in the order they happened. */
    private final List<String> steps = new ArrayList<>();
    private final List<AttendanceMessage> published = new ArrayList<>();
    private boolean turnstileAvailable = true;

    private AppUser appUser;
    private UUID credentialId;
    private CredentialVerifier verifier;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setId(UUID.randomUUID());
        appUser.setActive(true);
        credentialId = UUID.randomUUID();
        verifier = verifierFor(METHOD, CREDENTIAL);
    }

    @Test
    void acceptedCredentialStoresOneEventThenOpensGate() {
        String result = serviceWith(List.of(verifier), List.of(new ActiveUserRule()))
                .verify(METHOD, CREDENTIAL, AttendanceType.CHECK_IN);

        assertEquals(AccessDecisionService.ACCESS_GRANTED, result);
        assertEquals(List.of("published", "gate"), steps);
        assertEquals(
                List.of(new AttendanceMessage(credentialId, appUser.getId(), AttendanceType.CHECK_IN, NOW)),
                published
        );
    }

    @Test
    void credentialRejectedByItsVerifierOpensNothing() {
        assertRejected(serviceWith(List.of(verifier), List.of(new ActiveUserRule())), METHOD, "other-credential");
    }

    @Test
    void unsupportedMethodIsRejected() {
        assertRejected(serviceWith(List.of(verifier), List.of(new ActiveUserRule())), "UNKNOWN", CREDENTIAL);
    }

    @Test
    void requestIsHandledByTheVerifierOfItsMethod() {
        UUID otherCredentialId = UUID.randomUUID();
        CredentialVerifier other = new CredentialVerifier() {
            @Override
            public String method() {
                return "OTHER";
            }

            @Override
            public VerifiedCredential verify(String credential, Instant now) {
                return new VerifiedCredential(otherCredentialId, appUser);
            }
        };

        serviceWith(List.of(verifier, other), List.of(new ActiveUserRule()))
                .verify("OTHER", "anything", AttendanceType.CHECK_IN);

        assertEquals(otherCredentialId, published.get(0).eventId());
    }

    @Test
    void inactiveUserIsRejectedWhateverTheCredential() {
        appUser.setActive(false);

        assertRejected(serviceWith(List.of(verifier), List.of(new ActiveUserRule())), METHOD, CREDENTIAL);
    }

    @Test
    void addedRuleIsAppliedWithoutChangingTheService() {
        AccessRule denyEverything = (user, now) -> {
            throw new AccessRejectedException("denied by added rule");
        };

        assertRejected(
                serviceWith(List.of(verifier), List.of(new ActiveUserRule(), denyEverything)),
                METHOD,
                CREDENTIAL
        );
    }

    @Test
    void twoVerifiersForOneMethodAreRefused() {
        assertThrows(
                IllegalStateException.class,
                () -> serviceWith(List.of(verifier, verifierFor(METHOD, "x")), List.of())
        );
    }

    @Test
    void turnstileFailureIsReportedToTheCaller() {
        turnstileAvailable = false;

        assertThrows(
                TurnstileUnavailableException.class,
                () -> serviceWith(List.of(verifier), List.of(new ActiveUserRule()))
                        .verify(METHOD, CREDENTIAL, AttendanceType.CHECK_OUT)
        );
        assertEquals(List.of("published"), steps);
    }

    private AccessDecisionService serviceWith(List<CredentialVerifier> verifiers, List<AccessRule> rules) {
        return new AccessDecisionService(
                verifiers,
                rules,
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
    }

    /** Accepts exactly one credential string and rejects any other. */
    private CredentialVerifier verifierFor(String method, String accepted) {
        return new CredentialVerifier() {
            @Override
            public String method() {
                return method;
            }

            @Override
            public VerifiedCredential verify(String credential, Instant now) {
                if (!accepted.equals(credential)) {
                    throw new AccessRejectedException("credential is not valid");
                }
                return new VerifiedCredential(credentialId, appUser);
            }
        };
    }

    private void assertRejected(AccessDecisionService service, String method, String credential) {
        assertThrows(
                AccessRejectedException.class,
                () -> service.verify(method, credential, AttendanceType.CHECK_IN)
        );
        assertTrue(steps.isEmpty());
    }
}
