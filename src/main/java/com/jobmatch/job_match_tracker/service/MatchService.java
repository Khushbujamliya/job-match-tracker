package com.jobmatch.job_match_tracker.service;

import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobMatchResult;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.Resume;
import com.jobmatch.job_match_tracker.repository.ResumeRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final ResumeRepository resumeRepository;
    private final MongoTemplate mongoTemplate;

    public MatchService(ResumeRepository resumeRepository, MongoTemplate mongoTemplate) {
        this.resumeRepository = resumeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<JobMatchResult> findMatchingJobs(String resumeId, int limit) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (resume.getEmbedding() == null) {
            throw new IllegalStateException("Resume has no embedding yet: " + resumeId);
        }

        String vectorSearchStage = """
                { "$vectorSearch": {
                    "index": "jobs_vector_index",
                    "path": "embedding",
                    "queryVector": %s,
                    "numCandidates": 50,
                    "limit": %d
                } }
                """.formatted(resume.getEmbedding().toString(), limit);

        String projectStage = """
                { "$project": {
                    "_id": 1, "title": 1, "company": 1,
                    "score": { "$meta": "vectorSearchScore" }
                } }
                """;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.stage(vectorSearchStage),
                Aggregation.stage(projectStage));

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "jobs", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> JobMatchResult.builder()
                        .jobId(doc.getObjectId("_id").toString())
                        .title(doc.getString("title"))
                        .company(doc.getString("company"))
                        .matchScore(doc.getDouble("score"))
                        .build())
                .toList();
    }
}