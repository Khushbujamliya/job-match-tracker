package com.jobmatch.job_match_tracker.controller;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.LinkStatsResponse;
import com.jobmatch.job_match_tracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/top-links")
    public ResponseEntity<List<LinkStatsResponse>> topLinks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.topLinks(limit));
    }

    @GetMapping("/jobs/{jobId}/links")
    public ResponseEntity<List<LinkStatsResponse>> linksForJob(@PathVariable String jobId) {
        return ResponseEntity.ok(dashboardService.linksForJob(jobId));
    }
}