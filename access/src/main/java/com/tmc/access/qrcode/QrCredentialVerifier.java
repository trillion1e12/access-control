package com.tmc.access.qrcode;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.tmc.access.decision.AccessRejectedException;
import com.tmc.access.decision.CredentialVerifier;
import com.tmc.access.decision.VerifiedCredential;

/** Accepts a QR code that exists, has not expired and has not been used. */
@Component
public class QrCredentialVerifier implements CredentialVerifier {
    public static final String METHOD = "QR";

    private final QrCodeRepository qrCodeRepository;

    public QrCredentialVerifier(QrCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    @Override
    public String method() {
        return METHOD;
    }

    /**
     * Uses the QR code up in the same database transaction as the caller, so a
     * later rejection rolls the use back.
     */
    @Override
    public VerifiedCredential verify(String content, Instant now) {
        QrCode qrCode = qrCodeRepository.findByContent(content)
                .orElseThrow(() -> new AccessRejectedException("QR code is unknown"));
        if (!now.isBefore(qrCode.getExpiresAt())) {
            throw new AccessRejectedException("QR code has expired");
        }
        if (qrCodeRepository.invalidate(qrCode.getId()) == 0) {
            throw new AccessRejectedException("QR code has already been used");
        }
        return new VerifiedCredential(qrCode.getId(), qrCode.getAppUser());
    }
}
