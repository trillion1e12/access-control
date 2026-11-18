package com.tmc.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;

class AttendanceServiceTest {
    private final AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AttendanceService attendanceService = new AttendanceService(
            attendanceRepository,
            userRepository,
            new AttendanceMapper()
    );

    private final UUID eventId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final AttendanceMessage message = new AttendanceMessage(
            eventId,
            userId,
            AttendanceType.CHECK_IN,
            Instant.parse("2026-11-13T01:00:00Z")
    );

    @Test
    void recordsNewEventWithItsEventId() {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        when(attendanceRepository.existsBySourceEventId(eventId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(appUser));

        assertTrue(attendanceService.recordAttendance(message));

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).saveAndFlush(saved.capture());
        assertEquals(eventId, saved.getValue().getSourceEventId());
        assertEquals(AttendanceType.CHECK_IN, saved.getValue().getEvent());
        assertEquals(message.createdAt(), saved.getValue().getCreatedAt());
    }

    @Test
    void skipsEventThatWasAlreadyRecorded() {
        when(attendanceRepository.existsBySourceEventId(eventId)).thenReturn(true);

        assertFalse(attendanceService.recordAttendance(message));

        verify(attendanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMessageForUnknownUser() {
        when(attendanceRepository.existsBySourceEventId(eventId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(InvalidAttendanceMessageException.class, () -> attendanceService.recordAttendance(message));
    }

    @Test
    void rejectsMessageWithoutEventId() {
        AttendanceMessage withoutId = new AttendanceMessage(null, userId, AttendanceType.CHECK_OUT, Instant.now());

        assertThrows(InvalidAttendanceMessageException.class, () -> attendanceService.recordAttendance(withoutId));
    }
}
