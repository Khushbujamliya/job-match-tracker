package com.jobmatch.job_match_tracker.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;

import com.jobmatch.job_match_tracker.dto.ShortLinkRequest;
import com.jobmatch.job_match_tracker.dto.ShortLinkResponse;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.ShortLink;
import com.jobmatch.job_match_tracker.repository.ShortLinkRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();
    private static final String CACHE_PREFIX = "shortlink:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final ShortLinkRepository shortLinkRepository;
    private final LinkClickProducer linkClickProducer;

    public ShortLinkService(ShortLinkRepository shortLinkRepository, LinkClickProducer linkClickProducer,
            StringRedisTemplate redisTemplate) {
        this.shortLinkRepository = shortLinkRepository;
        this.linkClickProducer = linkClickProducer;
        this.redisTemplate = redisTemplate;
    }

    public ShortLinkResponse create(ShortLinkRequest request) {
        String code = generateUniqueCode();

        ShortLink shortLink = ShortLink.builder()
                .code(code)
                .targetUrl(request.getTargetUrl())
                .jobId(request.getJobId())
                .clickCount(0)
                .createdAt(Instant.now())
                .build();

        return toResponse(shortLinkRepository.save(shortLink));
    }

    public String resolveAndRecordClick(String code) {
        String cacheKey = CACHE_PREFIX + code;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        String targetUrl;
        if (cachedUrl != null) {
            targetUrl = cachedUrl;
        } else {
            ShortLink shortLink = shortLinkRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Short link not found: " + code));
            targetUrl = shortLink.getTargetUrl();

            redisTemplate.opsForValue().set(cacheKey, targetUrl, CACHE_TTL);
        }

        linkClickProducer.publishClick(code);

        return targetUrl;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (shortLinkRepository.findByCode(code).isPresent());
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private ShortLinkResponse toResponse(ShortLink shortLink) {
        return ShortLinkResponse.builder()
                .code(shortLink.getCode())
                .shortUrl("http://localhost:8080/r/" + shortLink.getCode())
                .targetUrl(shortLink.getTargetUrl())
                .clickCount(shortLink.getClickCount())
                .createdAt(shortLink.getCreatedAt())
                .build();
    }
}