package com.jobmatch.job_match_tracker.service;

import java.time.Instant;
import java.util.List;

import com.jobmatch.job_match_tracker.dto.JobMatchResult;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.Match;
import com.jobmatch.job_match_tracker.model.Resume;
import com.jobmatch.job_match_tracker.repository.JobRepository;
import com.jobmatch.job_match_tracker.repository.MatchRepository;
import com.jobmatch.job_match_tracker.repository.ResumeRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final MatchRepository matchRepository;
    private final MongoTemplate mongoTemplate;

    public MatchService(ResumeRepository resumeRepository, JobRepository jobRepository,
            MatchRepository matchRepository, MongoTemplate mongoTemplate) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.matchRepository = matchRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<JobMatchResult> findMatchingJobs(String resumeId, int limit) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (resume.getEmbedding() == null) {
            throw new IllegalStateException("Resume has no embedding yet: " + resumeId);
        }

        List<JobMatchResult> results = runVectorSearch(resume.getEmbedding(), limit);

        for (JobMatchResult result : results) {
            if (jobRepository.existsById(result.getJobId())) {
                matchRepository.save(Match.builder()
                        .resumeId(resumeId)
                        .jobId(result.getJobId())
                        .score(result.getMatchScore())
                        .createdAt(Instant.now())
                        .build());
            }
        }

        return results;
    }

    private List<JobMatchResult> runVectorSearch(List<Float> embedding, int limit) {
        String vectorSearchStage = """
                { "$vectorSearch": {
                    "index": "jobs_vector_index",
                    "path": "embedding",
                    "queryVector": %s,
                    "numCandidates": 50,
                    "limit": %d
                } }
                """.formatted(embedding.toString(), limit);

        String projectStage = """
                { "$project": {
                    "_id": 1, "title": 1, "company": 1,
                    "score": { "$meta": "vectorSearchScore" }
                } }
                """;

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.stage(vectorSearchStage),
                Aggregation.stage(projectStage));

        AggregationResults<Document> agg = mongoTemplate.aggregate(aggregation, "jobs", Document.class);

        return agg.getMappedResults().stream()
                .map(doc -> JobMatchResult.builder()
                        .jobId(doc.getObjectId("_id").toString())
                        .title(doc.getString("title"))
                        .company(doc.getString("company"))
                        .matchScore(doc.getDouble("score"))
                        .build())
                .toList();
    }
}