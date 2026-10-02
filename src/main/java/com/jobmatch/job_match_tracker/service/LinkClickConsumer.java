package com.jobmatch.job_match_tracker.service;

import com.jobmatch.job_match_tracker.dto.LinkClickEvent;
import com.jobmatch.job_match_tracker.model.ShortLink;
import com.jobmatch.job_match_tracker.repository.ShortLinkRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LinkClickConsumer {

    private final ShortLinkRepository shortLinkRepository;

    public LinkClickConsumer(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    @KafkaListener(topics = LinkClickProducer.TOPIC, groupId = "link-click-consumers")
    public void handleClick(LinkClickEvent event) {
        shortLinkRepository.findByCode(event.code()).ifPresent(shortLink -> {
            shortLink.setClickCount(shortLink.getClickCount() + 1);
            shortLinkRepository.save(shortLink);
        });
    }
}