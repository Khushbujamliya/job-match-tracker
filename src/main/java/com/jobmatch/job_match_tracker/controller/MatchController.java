package com.jobmatch.job_match_tracker.controller;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobMatchResult;
import com.jobmatch.job_match_tracker.model.Match;
import com.jobmatch.job_match_tracker.repository.MatchRepository;
import com.jobmatch.job_match_tracker.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController {

    private final MatchService matchService;
    private final MatchRepository matchRepository;

    public MatchController(MatchService matchService, MatchRepository matchRepository) {
        this.matchService = matchService;
        this.matchRepository = matchRepository;
    }

    @GetMapping("/api/resumes/{resumeId}/matches")
    public ResponseEntity<List<JobMatchResult>> findMatches(
            @PathVariable String resumeId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(matchService.findMatchingJobs(resumeId, limit));
    }

    @GetMapping("/api/resumes/{resumeId}/match-history")
    public ResponseEntity<List<Match>> getHistory(@PathVariable String resumeId) {
        return ResponseEntity.ok(matchRepository.findByResumeId(resumeId));
    }
}