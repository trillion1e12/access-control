package com.tmc.access.qrcode;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.user.AppUser;
import com.tmc.access.user.UserRepository;

@Service
public class QrCodeService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public QrCodeService(UserRepository userRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String testKafka() {
        ObjectMapper objectMapper = new ObjectMapper();
        AppUser randomUser = userRepository.findAll().get(0);
        AttendanceMessage message = new AttendanceMessage(
                randomUser.getId(),
                AttendanceType.CHECK_IN,
                Instant.now()
        );
        kafkaTemplate.send("attendance", objectMapper.writeValueAsString(message));
        return "Check database for new attendace of user " + randomUser.getId();
    }
}
