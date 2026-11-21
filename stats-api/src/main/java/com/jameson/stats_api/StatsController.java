package com.jameson.stats_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jameson.stats_api.leetcode.LeetCodeService;

@RestController
public class StatsController {

    private final LeetCodeService leetCode;

    public StatsController(LeetCodeService leetCode) {
        this.leetCode = leetCode;
    }

    @GetMapping("/api/stats")
    public StatsResponse stats() {
        return new StatsResponse(leetCode.getSolvedCount());
    }

    public record StatsResponse(Integer leetcodeSolved) {}
}