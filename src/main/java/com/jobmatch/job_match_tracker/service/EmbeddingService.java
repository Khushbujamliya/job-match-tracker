package com.jobmatch.job_match_tracker.service;

import java.util.List;
import java.util.Map;

import com.jobmatch.job_match_tracker.config.OllamaProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmbeddingService {

    private final RestClient restClient;
    private final OllamaProperties properties;

    public EmbeddingService(OllamaProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create(properties.baseUrl());
    }

    public List<Float> embed(String text) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.embeddingModel(),
                "prompt", text);

        OllamaEmbeddingResponse response = restClient.post()
                .uri("/api/embeddings")
                .body(requestBody)
                .retrieve()
                .body(OllamaEmbeddingResponse.class);

        if (response == null || response.embedding() == null) {
            throw new IllegalStateException("Embedding service returned no data");
        }
        return response.embedding();
    }

    private record OllamaEmbeddingResponse(List<Float> embedding) {
    }
}