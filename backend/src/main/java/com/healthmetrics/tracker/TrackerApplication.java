package com.healthmetrics.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot application class.
 *
 * @EnableJpaAuditing enables automatic population of audit fields
 * like createdAt and updatedAt timestamps.
 */
@SpringBootApplication
@EnableJpaAuditing
public class TrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackerApplication.class, args);
	}
}