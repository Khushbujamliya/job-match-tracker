package com.jobmatch.job_match_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.jobmatch.job_match_tracker.config.OllamaProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class JobMatchTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobMatchTrackerApplication.class, args);
	}

}
