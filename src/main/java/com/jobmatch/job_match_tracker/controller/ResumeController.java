package com.jobmatch.job_match_tracker.controller;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.ResumeRequest;
import com.jobmatch.job_match_tracker.dto.ResumeResponse;
import com.jobmatch.job_match_tracker.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(@Valid @RequestBody ResumeRequest request) {
        ResumeResponse response = resumeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAll() {
        return ResponseEntity.ok(resumeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(resumeService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}