package com.callbackcats.reboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaMessageHandler {

    @Value("${notifications.kafka.public-topic}")
    private String publicTopic;

    @Value("${notifications.kafka.message}")
    private String message;


    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageHandler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String employeeId, Integer position) {
        log.info("Message send requested");
        String sendableMessage = message + position;
        kafkaTemplate.send(publicTopic, sendableMessage);
    }

    @KafkaListener(topics = "employees", groupId = "group_id")
    public void consumeMessage(String message) {
        log.info("Message received:\t" + message);
    }

}
