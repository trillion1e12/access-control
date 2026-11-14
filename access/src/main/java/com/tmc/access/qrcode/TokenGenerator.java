package com.tmc.access.qrcode;

/** Produces the text carried by a QR code. */
public interface TokenGenerator {
    /**
     * @return a value that cannot be guessed and is not derived from any user data
     */
    String newToken();
}
