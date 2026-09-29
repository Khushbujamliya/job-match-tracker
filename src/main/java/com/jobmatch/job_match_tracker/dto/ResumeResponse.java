package com.jobmatch.job_match_tracker.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResumeResponse {
    private String id;
    private String name;
    private String email;
    private List<String> skills;
    private Integer experienceYears;
    private Instant createdAt;
}