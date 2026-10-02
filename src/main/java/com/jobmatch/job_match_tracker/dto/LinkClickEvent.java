package com.jobmatch.job_match_tracker.dto;

import java.time.Instant;

public record LinkClickEvent(String code, Instant clickedAt) {
}