package com.tmc.attendance.attendance;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import com.tmc.attendance.attendance.dto.AttendanceResponse;
import com.tmc.attendance.attendance.dto.CreateAttendanceRequest;
import com.tmc.attendance.user.AppUser;
import com.tmc.attendance.user.UserRepository;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceMapper attendanceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        System.out.println("Received from kafka: " + rawMessage);
        AttendanceMessage message = objectMapper.readValue(rawMessage, AttendanceMessage.class);
        System.out.println(message.toString());
    }
}
