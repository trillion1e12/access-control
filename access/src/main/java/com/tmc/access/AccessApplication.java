package com.tmc.access;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class AccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> kafkaTemplate.send("greetings", "Hello Kafka!");
    }

}
