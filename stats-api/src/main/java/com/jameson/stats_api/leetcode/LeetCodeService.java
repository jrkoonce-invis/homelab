package com.jameson.stats_api.leetcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LeetCodeService {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeService.class);

    private final LeetCodeClient client;
    private final String username;

    private volatile Integer solvedCount;   // last known value, null until first success

    public LeetCodeService(LeetCodeClient client,
                           @Value("${leetcode.username}") String username) {
        this.client = client;
        this.username = username;
    }

    @Scheduled(fixedRateString = "PT30M")   // run at startup, then every 30 minutes
    public void refresh() {
        try {
            Integer latest = client.fetchSolvedCount(username);
            if (latest != null) {
                solvedCount = latest;
                log.info("LeetCode solved count refreshed: {}", latest);
            } else {
                log.warn("LeetCode returned no data for user {}", username);
            }
        } catch (Exception e) {
            log.warn("LeetCode refresh failed, keeping last value ({}): {}", solvedCount, e.getMessage());
        }
    }

    public Integer getSolvedCount() {
        return solvedCount;
    }
}