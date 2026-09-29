package com.jobmatch.job_match_tracker.repository;

import com.jobmatch.job_match_tracker.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeRepository extends MongoRepository<Resume, String> {
}