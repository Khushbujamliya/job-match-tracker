package com.jobmatch.job_match_tracker.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class JobRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Description is required")
    private String description;

    @NotEmpty(message = "At least one required skill is needed")
    private List<String> requiredSkills;

    private Integer minExperienceYears;
}