package com.tmc.access.decision;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmc.access.attendance.AttendanceEventPublisher;
import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.turnstile.TurnstileClient;

/** Decides whether a presented credential opens the gate. */
@Service
public class AccessDecisionService {
    static final String ACCESS_GRANTED = "Access granted";

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDecisionService.class);

    private final Map<String, CredentialVerifier> verifiers;
    private final List<AccessRule> accessRules;
    private final TurnstileClient turnstileClient;
    private final AttendanceEventPublisher attendanceEventPublisher;
    private final Clock clock;

    public AccessDecisionService(
            List<CredentialVerifier> credentialVerifiers,
            List<AccessRule> accessRules,
            TurnstileClient turnstileClient,
            AttendanceEventPublisher attendanceEventPublisher,
            Clock clock
    ) {
        this.verifiers = credentialVerifiers.stream()
                .collect(Collectors.toUnmodifiableMap(CredentialVerifier::method, Function.identity()));
        this.accessRules = List.copyOf(accessRules);
        this.turnstileClient = turnstileClient;
        this.attendanceEventPublisher = attendanceEventPublisher;
        this.clock = clock;
    }

    /**
     * Checks a credential with the verifier of its kind, then applies every access
     * rule to the user it belongs to. When all pass, stores the entry or exit and
     * opens the gate. The gate is asked to open last: if it refuses, the credential
     * is not used up and no entry or exit is stored.
     *
     * @throws AccessRejectedException
     *             when the kind of credential is not supported, the credential is
     *             not valid, or a rule fails
     */
    @Transactional
    public String verify(String method, String credential, AttendanceType event) {
        CredentialVerifier verifier = verifiers.get(method);
        if (verifier == null) {
            throw new AccessRejectedException("Unsupported credential method");
        }
        Instant now = clock.instant();
        VerifiedCredential verified = verifier.verify(credential, now);
        accessRules.forEach(rule -> rule.check(verified.appUser(), now));
        attendanceEventPublisher.publish(
                new AttendanceMessage(verified.credentialId(), verified.appUser().getId(), event, now)
        );
        turnstileClient.openGate(event);
        LOGGER.info("Gate opened: method={}, credentialId={}, userId={}, event={}", method,
                verified.credentialId(), verified.appUser().getId(), event);
        return ACCESS_GRANTED;
    }
}
