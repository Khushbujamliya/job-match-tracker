package com.jobmatch.job_match_tracker.service;

import java.time.Instant;
import java.util.List;

import com.jobmatch.job_match_tracker.dto.ResumeRequest;
import com.jobmatch.job_match_tracker.dto.ResumeResponse;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.Resume;
import com.jobmatch.job_match_tracker.repository.ResumeRepository;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final EmbeddingService embeddingService;

    public ResumeService(ResumeRepository resumeRepository, EmbeddingService embeddingService) {
        this.resumeRepository = resumeRepository;
        this.embeddingService = embeddingService;
    }

    public ResumeResponse create(ResumeRequest request) {
        List<Float> embedding = embeddingService.embed(buildEmbeddingText(request));

        Resume resume = Resume.builder()
                .name(request.getName())
                .email(request.getEmail())
                .skills(request.getSkills())
                .experienceYears(request.getExperienceYears())
                .rawText(request.getRawText())
                .embedding(embedding)
                .createdAt(Instant.now())
                .build();

        return toResponse(resumeRepository.save(resume));
    }

    public List<ResumeResponse> getAll() {
        return resumeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ResumeResponse getById(String id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));
        return toResponse(resume);
    }

    public void delete(String id) {
        if (!resumeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resume not found with id: " + id);
        }
        resumeRepository.deleteById(id);
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .name(resume.getName())
                .email(resume.getEmail())
                .skills(resume.getSkills())
                .experienceYears(resume.getExperienceYears())
                .createdAt(resume.getCreatedAt())
                .build();
    }

    private String buildEmbeddingText(ResumeRequest request) {
        return String.join(" ",
                request.getName(),
                String.join(", ", request.getSkills()),
                request.getExperienceYears() != null ? request.getExperienceYears() + " years experience" : "",
                request.getRawText() != null ? request.getRawText() : "");
    }
}