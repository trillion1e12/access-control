package com.tmc.access.decision;

import java.time.Instant;

import com.tmc.access.qrcode.QrCode;

/**
 * One condition a scanned QR code must meet. Every rule must pass before a gate
 * is opened.
 */
public interface AccessRule {
    /**
     * @throws AccessRejectedException
     *             when the condition is not met
     */
    void check(QrCode qrCode, Instant now);
}
