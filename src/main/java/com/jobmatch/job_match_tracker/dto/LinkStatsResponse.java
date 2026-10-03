package com.jobmatch.job_match_tracker.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LinkStatsResponse {
    private String code;
    private String targetUrl;
    private String jobId;
    private long clickCount;
    private Instant createdAt;
}