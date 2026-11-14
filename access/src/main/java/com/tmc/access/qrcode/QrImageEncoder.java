package com.tmc.access.qrcode;

/** Renders text as a QR code image. */
public interface QrImageEncoder {
    /** @return the image as PNG bytes */
    byte[] encode(String content);
}
