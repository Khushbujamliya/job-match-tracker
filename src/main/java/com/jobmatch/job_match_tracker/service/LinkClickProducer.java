package com.jobmatch.job_match_tracker.service;

import java.time.Instant;

import com.jobmatch.job_match_tracker.dto.LinkClickEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LinkClickProducer {

    public static final String TOPIC = "link-clicks";

    private final KafkaTemplate<String, LinkClickEvent> kafkaTemplate;

    public LinkClickProducer(KafkaTemplate<String, LinkClickEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishClick(String code) {
        kafkaTemplate.send(TOPIC, code, new LinkClickEvent(code, Instant.now()));
    }
}