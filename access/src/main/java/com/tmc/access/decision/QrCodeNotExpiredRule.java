package com.tmc.access.decision;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tmc.access.qrcode.QrCode;

@Component
public class QrCodeNotExpiredRule implements AccessRule {
    @Override
    public void check(QrCode qrCode, Instant now) {
        if (!now.isBefore(qrCode.getExpiresAt())) {
            throw new AccessRejectedException("QR code has expired");
        }
    }
}
