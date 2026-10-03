package com.jobmatch.job_match_tracker.repository;

import java.util.List;
import java.util.Optional;

import com.jobmatch.job_match_tracker.model.ShortLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShortLinkRepository extends MongoRepository<ShortLink, String> {
    Optional<ShortLink> findByCode(String code);

    List<ShortLink> findByJobId(String jobId);

    List<ShortLink> findAllByOrderByClickCountDesc(Pageable pageable);
}