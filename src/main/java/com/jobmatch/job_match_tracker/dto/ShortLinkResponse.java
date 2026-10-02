package com.jobmatch.job_match_tracker.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ShortLinkResponse {
    private String code;
    private String shortUrl;
    private String targetUrl;
    private long clickCount;
    private Instant createdAt;
}