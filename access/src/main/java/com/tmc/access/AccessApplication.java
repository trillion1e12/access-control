package com.tmc.access;

import java.time.Instant;
import java.util.UUID;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;

@SpringBootApplication
public class AccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            ObjectMapper objectMapper = new ObjectMapper();
            AttendanceMessage message = new AttendanceMessage(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    AttendanceType.CHECK_IN,
                    Instant.now()
            );
            kafkaTemplate.send("attendance", objectMapper.writeValueAsString(message));
        };
    }

}
