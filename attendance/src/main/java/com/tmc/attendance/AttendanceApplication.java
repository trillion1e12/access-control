package com.tmc.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class AttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }

    @KafkaListener(topics = "greetings", groupId = "demo")
    public void listen(String message) {
        System.out.println("Received from kafka: " + message);
    }
}
