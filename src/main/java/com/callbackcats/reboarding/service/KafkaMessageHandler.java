package com.callbackcats.reboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaMessageHandler {


    private final String MESSAGE = "You position in queue is: ";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageHandler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String employeeId, Integer position) {
        log.info("Message send requested");
        String sendableMessage = MESSAGE + position;
        kafkaTemplate.send(employeeId, sendableMessage);
    }

    @KafkaListener(topics = "users", groupId = "group_id")
    public void consumeMessage(String message) {
        log.info("Message recieved:\t" + message);
    }

}
