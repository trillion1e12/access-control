package com.tmc.access.qrcode;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tmc.access.token.TokenGenerator;
import com.tmc.access.user.AppUser;
import com.tmc.access.user.UserNotActiveException;
import com.tmc.access.user.UserRepository;

/** Issues QR codes. */
@Service
public class QrCodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeService.class);

    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;
    private final TokenGenerator tokenGenerator;
    private final QrImageEncoder qrImageEncoder;
    private final Clock clock;
    private final Duration timeToLive;

    public QrCodeService(
            QrCodeRepository qrCodeRepository,
            UserRepository userRepository,
            TokenGenerator tokenGenerator,
            QrImageEncoder qrImageEncoder,
            Clock clock,
            @Value("${qr.ttl-seconds:60}") long ttlSeconds
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
        this.qrImageEncoder = qrImageEncoder;
        this.clock = clock;
        this.timeToLive = Duration.ofSeconds(ttlSeconds);
    }

    /**
     * Issues a short-lived, single-use QR code for the user and returns it as a PNG
     * image.
     *
     * @throws UserNotActiveException
     *             when the user is unknown or inactive
     */
    public byte[] getQrCode(String email) {
        AppUser appUser = userRepository.findByEmail(email)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new UserNotActiveException("User is unknown or inactive"));
        Instant now = clock.instant();
        QrCode qrCode = QrCode.builder()
                .appUser(appUser)
                .content(tokenGenerator.newToken())
                .createdAt(now)
                .expiresAt(now.plus(timeToLive))
                .build();
        qrCodeRepository.save(qrCode);
        LOGGER.info("QR code issued: userId={}, expiresAt={}", appUser.getId(), qrCode.getExpiresAt());
        return qrImageEncoder.encode(qrCode.getContent());
    }
}
