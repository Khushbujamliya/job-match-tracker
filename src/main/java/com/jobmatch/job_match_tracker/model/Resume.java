package com.jobmatch.job_match_tracker.model;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resumes")
public class Resume {

    @Id
    private String id;

    private String name;
    private String email;
    private List<String> skills;
    private Integer experienceYears;
    private String rawText;
    private Instant createdAt;
}