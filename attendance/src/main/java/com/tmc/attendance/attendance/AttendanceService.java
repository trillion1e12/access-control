package com.tmc.attendance.attendance;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.tmc.attendance.attendance.dto.AttendanceResponse;
import com.tmc.attendance.attendance.dto.CreateAttendanceRequest;
import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            AttendanceMapper attendanceMapper
    ) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.attendanceMapper = attendanceMapper;
    }

    public AttendanceResponse createAttendance(CreateAttendanceRequest request) {
        AppUser appUser = userRepository.findById(request.userId()).orElseThrow();
        Attendance attendance = attendanceMapper.toEntity(request, appUser);
        Attendance createAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(createAttendance);
    }

    /**
     * Records the entry or exit carried by a message. Recording the same event id
     * twice keeps a single row.
     *
     * @return false when the event had already been recorded
     * @throws InvalidAttendanceMessageException
     *             when a required field is missing or the user does not exist
     */
    public boolean recordAttendance(AttendanceMessage message) {
        if (message.eventId() == null || message.userId() == null || message.event() == null) {
            throw new InvalidAttendanceMessageException("Attendance message is missing a required field");
        }
        if (attendanceRepository.existsBySourceEventId(message.eventId())) {
            return false;
        }
        AppUser appUser = userRepository.findById(message.userId())
                .orElseThrow(() -> new InvalidAttendanceMessageException(
                        "Attendance message refers to unknown user " + message.userId()));
        try {
            attendanceRepository.saveAndFlush(attendanceMapper.toEntity(message, appUser));
            return true;
        } catch (DataIntegrityViolationException e) {
            // Another consumer stored the same event between the check and the insert.
            if (attendanceRepository.existsBySourceEventId(message.eventId())) {
                return false;
            }
            throw e;
        }
    }
}
