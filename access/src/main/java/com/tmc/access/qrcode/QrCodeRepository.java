package com.tmc.access.qrcode;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QrCodeRepository extends Repository<QrCode, UUID> {
    QrCode save(QrCode qrCode);

    Optional<QrCode> findByContent(String content);

    /**
     * Marks a QR code as used.
     *
     * @return 1 when this call used it, 0 when it had already been used or
     *         invalidated
     */
    @Modifying
    @Transactional
    @Query("update QrCode q set q.valid = false where q.id = :id and q.valid = true")
    int invalidate(@Param("id") UUID id);
}
