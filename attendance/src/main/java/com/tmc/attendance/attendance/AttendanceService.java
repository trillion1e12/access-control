package com.tmc.attendance.attendance;
// https://www.baeldung.com/jackson-object-mapper-tutorial

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import com.tmc.attendance.attendance.dto.AttendanceResponse;
import com.tmc.attendance.attendance.dto.CreateAttendanceRequest;
import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;
import com.tmc.attendance.user.UserRole;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    @KafkaListener(topics = "attendance", groupId = "demo")
    public void listen(String rawMessage) {
        logger.info("Message received from kafka: " + rawMessage);
        AttendanceMessage message = objectMapper.readValue(rawMessage, AttendanceMessage.class);
        AppUser appUser = userRepository.findById(message.userId()).orElseThrow();
        Attendance attendance = attendanceMapper.toEntity(message, appUser);
        attendanceRepository.save(attendance);
    }

    public List<AttendanceResponse> getAttendances(Instant start, Instant end) {
        List<Attendance> attendances = attendanceRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(start, end);
        return attendances.stream().map(attendanceMapper::toResponse).toList();
    }

}
