package com.tmc.access.decision;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tmc.access.qrcode.QrCode;

@Component
public class ActiveUserRule implements AccessRule {
    @Override
    public void check(QrCode qrCode, Instant now) {
        if (!qrCode.getAppUser().isActive()) {
            throw new AccessRejectedException("User is inactive");
        }
    }
}
