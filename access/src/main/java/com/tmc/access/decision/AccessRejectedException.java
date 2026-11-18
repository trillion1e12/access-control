package com.tmc.access.decision;

/** A presented credential does not allow its holder through the gate. */
public class AccessRejectedException extends RuntimeException {
    public AccessRejectedException(String message) {
        super(message);
    }
}
