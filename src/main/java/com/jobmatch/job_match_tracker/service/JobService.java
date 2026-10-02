package com.jobmatch.job_match_tracker.service;

import java.time.Instant;
import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobRequest;
import com.jobmatch.job_match_tracker.dto.JobResponse;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.Job;
import com.jobmatch.job_match_tracker.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;

    public JobService(JobRepository jobRepository, EmbeddingService embeddingService) {
        this.jobRepository = jobRepository;
        this.embeddingService = embeddingService;
    }

    public JobResponse create(JobRequest request) {
        List<Float> embedding = embeddingService.embed(buildEmbeddingText(request));

        Job job = Job.builder()
                .title(request.getTitle())
                .company(request.getCompany())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .minExperienceYears(request.getMinExperienceYears())
                .embedding(embedding)
                .createdAt(Instant.now())
                .build();

        return toResponse(jobRepository.save(job));
    }

    public List<JobResponse> getAll() {
        return jobRepository.findAll().stream().map(this::toResponse).toList();
    }

    public JobResponse getById(String id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return toResponse(job);
    }

    public void delete(String id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .minExperienceYears(job.getMinExperienceYears())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private String buildEmbeddingText(JobRequest request) {
        return String.join(" ",
                request.getTitle(),
                request.getCompany(),
                request.getDescription(),
                String.join(", ", request.getRequiredSkills()));
    }
}