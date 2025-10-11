package com.microservice.auth.services;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Map<String, String>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOtp(String email, String otp) {
        Map<String, String> message = Map.of(email, otp);
        kafkaTemplate.send("otp-events", message);
    }
}
