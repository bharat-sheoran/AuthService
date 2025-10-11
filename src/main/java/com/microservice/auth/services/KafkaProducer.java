package com.microservice.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservice.auth.entities.OTP;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, OTP> kafkaTemplate;

    public void sendOTP(String email, OTP otp) {
        kafkaTemplate.send("otp-events", email, otp);
    }
}
