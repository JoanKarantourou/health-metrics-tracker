package com.healthmetrics.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI healthMetricsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Health Metrics Tracker API")
                        .description("REST API for tracking, aggregating, and visualizing health indicators across facilities")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Joan Karantourou")
                                .url("https://github.com/JoanKarantourou/health-metrics-tracker"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")));
    }
}
