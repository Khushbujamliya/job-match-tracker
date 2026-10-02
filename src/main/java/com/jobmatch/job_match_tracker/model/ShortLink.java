package com.jobmatch.job_match_tracker.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "short_links")
public class ShortLink {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String targetUrl;
    private String jobId;
    private long clickCount;
    private Instant createdAt;
}