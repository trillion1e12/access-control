package com.tmc.access.token;

/**
 * Produces random values that serve as secrets, such as the text a QR code
 * carries.
 */
public interface TokenGenerator {
    /**
     * @return a value that cannot be guessed and is not derived from any user data
     */
    String newToken();
}
