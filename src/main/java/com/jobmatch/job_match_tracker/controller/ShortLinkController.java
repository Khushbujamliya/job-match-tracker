package com.jobmatch.job_match_tracker.controller;

import com.jobmatch.job_match_tracker.dto.ShortLinkRequest;
import com.jobmatch.job_match_tracker.dto.ShortLinkResponse;
import com.jobmatch.job_match_tracker.service.ShortLinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/links")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    public ResponseEntity<ShortLinkResponse> create(@Valid @RequestBody ShortLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shortLinkService.create(request));
    }
}