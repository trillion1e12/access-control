package com.tmc.access.attendance;

/** Hands an entry or exit over for recording as attendance. */
public interface AttendanceEventPublisher {
    /**
     * Accepts the message for delivery without waiting for it to be recorded as
     * attendance. When called inside a transaction, the message is accepted only if
     * that transaction commits.
     */
    void publish(AttendanceMessage message);
}
