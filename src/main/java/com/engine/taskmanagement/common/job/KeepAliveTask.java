package com.engine.taskmanagement.common.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:8080}")
    private String port;

    /**
     * Runs every 10 minutes (600000 milliseconds) to keep the backend alive.
     * Hits the public OpenAPI docs endpoint to simulate traffic.
     */
    @Scheduled(fixedRate = 600000)
    public void pingSelf() {
        try {
            String url = "http://localhost:" + port + "/v3/api-docs";
            logger.info("Executing keep-alive ping to {}", url);
            restTemplate.getForObject(url, String.class);
            logger.info("Keep-alive ping successful.");
        } catch (Exception e) {
            logger.error("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
