package com.jobmatch.job_match_tracker.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JobResponse {
    private String id;
    private String title;
    private String company;
    private String description;
    private List<String> requiredSkills;
    private Integer minExperienceYears;
    private Instant createdAt;
}