package com.tmc.attendance.attendance;

/**
 * Thrown for a message that can never be recorded, however many times it is
 * delivered.
 */
public class InvalidAttendanceMessageException extends RuntimeException {
    public InvalidAttendanceMessageException(String message) {
        super(message);
    }

    public InvalidAttendanceMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
