package com.callbackcats.reboarding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaMessageHandler {

    //   @Value("${notifications.kafka.public-topic}")
//    private String publicTopic;

    @Value("${notifications.kafka.message}")
    private String message;


    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageHandler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * <p>Sends a predefined message to the given topic
     * </p>
     *
     * @param employeeId the employee's id as the topic
     * @param position the position to send the notification about
     */
    public void sendNotification(String employeeId, Integer position) {
        log.info("Message send requested");
        String sendableMessage = message + "\t" + position;
        kafkaTemplate.send(employeeId, sendableMessage);
    }

/*    @KafkaListener(topics = "employees", groupId = "group_id")
    public void consumeMessage(String message) {
        log.info("Message received:\t" + message);
    }*/

}
