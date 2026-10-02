package com.jobmatch.job_match_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShortLinkRequest {
    @NotBlank(message = "Target URL is required")
    private String targetUrl;

    private String jobId;
}