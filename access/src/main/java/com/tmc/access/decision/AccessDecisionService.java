package com.tmc.access.decision;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tmc.access.attendance.AttendanceEventPublisher;
import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.qrcode.QrCode;
import com.tmc.access.qrcode.QrCodeRepository;
import com.tmc.access.turnstile.TurnstileClient;

/** Decides whether a scanned QR code opens the gate. */
@Service
public class AccessDecisionService {
    static final String ACCESS_GRANTED = "Access granted";

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessDecisionService.class);

    private final QrCodeRepository qrCodeRepository;
    private final List<AccessRule> accessRules;
    private final TurnstileClient turnstileClient;
    private final AttendanceEventPublisher attendanceEventPublisher;
    private final Clock clock;

    public AccessDecisionService(
            QrCodeRepository qrCodeRepository,
            List<AccessRule> accessRules,
            TurnstileClient turnstileClient,
            AttendanceEventPublisher attendanceEventPublisher,
            Clock clock
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.accessRules = List.copyOf(accessRules);
        this.turnstileClient = turnstileClient;
        this.attendanceEventPublisher = attendanceEventPublisher;
        this.clock = clock;
    }

    /**
     * Checks a scanned QR code against every access rule and, when all pass, stores
     * the entry or exit and opens the gate. A QR code is accepted at most once. The
     * gate is asked to open last: if it refuses, the QR code is not used up and no
     * entry or exit is stored.
     *
     * @throws AccessRejectedException
     *             when the QR code is unknown, already used, or fails a rule
     */
    @Transactional
    public String verifyQrCode(String content, AttendanceType event) {
        QrCode qrCode = qrCodeRepository.findByContent(content)
                .orElseThrow(() -> new AccessRejectedException("QR code is unknown"));
        Instant now = clock.instant();
        accessRules.forEach(rule -> rule.check(qrCode, now));
        if (qrCodeRepository.invalidate(qrCode.getId()) == 0) {
            throw new AccessRejectedException("QR code has already been used");
        }
        attendanceEventPublisher.publish(
                new AttendanceMessage(qrCode.getId(), qrCode.getAppUser().getId(), event, now)
        );
        turnstileClient.openGate(event);
        LOGGER.info("Gate opened: qrCodeId={}, userId={}, event={}", qrCode.getId(), qrCode.getAppUser().getId(),
                event);
        return ACCESS_GRANTED;
    }
}
