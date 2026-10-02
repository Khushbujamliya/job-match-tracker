package com.jobmatch.job_match_tracker.repository;

import java.util.List;

import com.jobmatch.job_match_tracker.model.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByResumeId(String resumeId);
}