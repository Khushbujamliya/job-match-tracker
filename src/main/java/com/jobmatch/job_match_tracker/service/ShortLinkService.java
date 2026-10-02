package com.jobmatch.job_match_tracker.service;

import java.security.SecureRandom;
import java.time.Instant;

import com.jobmatch.job_match_tracker.dto.ShortLinkRequest;
import com.jobmatch.job_match_tracker.dto.ShortLinkResponse;
import com.jobmatch.job_match_tracker.exception.ResourceNotFoundException;
import com.jobmatch.job_match_tracker.model.ShortLink;
import com.jobmatch.job_match_tracker.repository.ShortLinkRepository;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    private final ShortLinkRepository shortLinkRepository;

    public ShortLinkService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
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
        ShortLink shortLink = shortLinkRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Short link not found: " + code));

        shortLink.setClickCount(shortLink.getClickCount() + 1);
        shortLinkRepository.save(shortLink);

        return shortLink.getTargetUrl();
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