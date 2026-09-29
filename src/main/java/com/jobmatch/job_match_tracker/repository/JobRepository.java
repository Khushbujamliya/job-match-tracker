package com.jobmatch.job_match_tracker.repository;

import com.jobmatch.job_match_tracker.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobRepository extends MongoRepository<Job, String> {
}