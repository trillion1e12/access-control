package com.tmc.attendance.attendance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads the attendance topic and hands each message to
 * {@link AttendanceService}.
 */
@Component
public class AttendanceMessageListener {
    private final AttendanceService attendanceService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AttendanceMessageListener(AttendanceService attendanceService, ObjectMapper objectMapper) {
        this.attendanceService = attendanceService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.attendance-topic:attendance}", groupId = "${app.kafka.attendance-group:attendance-service}"
    )
    public void onMessage(String rawMessage) {
        AttendanceMessage message = parse(rawMessage);
        boolean recorded = attendanceService.recordAttendance(message);
        if (!recorded) {
            logger.info("Attendance event {} was already recorded, skipped", message.eventId());
        }
    }

    private AttendanceMessage parse(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, AttendanceMessage.class);
        } catch (JacksonException e) {
            throw new InvalidAttendanceMessageException("Attendance message is not valid JSON", e);
        }
    }
}
