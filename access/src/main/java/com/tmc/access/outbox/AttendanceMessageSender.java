package com.tmc.access.outbox;

import com.tmc.access.attendance.AttendanceMessage;

/**
 * Delivers one attendance message to the consumer side and confirms that it was
 * accepted.
 */
public interface AttendanceMessageSender {
    /**
     * @throws RuntimeException
     *             when delivery could not be confirmed; the message may be sent
     *             again later
     */
    void send(AttendanceMessage message);
}
