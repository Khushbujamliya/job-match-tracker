package com.jobmatch.job_match_tracker.service;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.LinkStatsResponse;
import com.jobmatch.job_match_tracker.model.ShortLink;
import com.jobmatch.job_match_tracker.repository.ShortLinkRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ShortLinkRepository shortLinkRepository;

    public DashboardService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    public List<LinkStatsResponse> topLinks(int limit) {
        return shortLinkRepository.findAllByOrderByClickCountDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toStats)
                .toList();
    }

    public List<LinkStatsResponse> linksForJob(String jobId) {
        return shortLinkRepository.findByJobId(jobId)
                .stream()
                .map(this::toStats)
                .toList();
    }

    private LinkStatsResponse toStats(ShortLink shortLink) {
        return LinkStatsResponse.builder()
                .code(shortLink.getCode())
                .targetUrl(shortLink.getTargetUrl())
                .jobId(shortLink.getJobId())
                .clickCount(shortLink.getClickCount())
                .createdAt(shortLink.getCreatedAt())
                .build();
    }
}