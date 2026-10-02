package com.jobmatch.job_match_tracker.controller;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobMatchResult;
import com.jobmatch.job_match_tracker.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/api/resumes/{resumeId}/matches")
    public ResponseEntity<List<JobMatchResult>> findMatches(
            @PathVariable String resumeId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(matchService.findMatchingJobs(resumeId, limit));
    }
}