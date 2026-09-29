package com.jobmatch.job_match_tracker.controller;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobRequest;
import com.jobmatch.job_match_tracker.dto.JobResponse;
import com.jobmatch.job_match_tracker.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAll() {
        return ResponseEntity.ok(jobService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}