package com.jobmatch.job_match_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JobMatchResult {
    private String jobId;
    private String title;
    private String company;
    private double matchScore;
}