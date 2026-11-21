package com.jameson.stats_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jameson.stats_api.cluster.ClusterService;
import com.jameson.stats_api.cluster.ClusterService.ClusterStats;
import com.jameson.stats_api.leetcode.LeetCodeService;

@RestController
public class StatsController {

    private final LeetCodeService leetCode;
    private final ClusterService cluster;

    public StatsController(LeetCodeService leetCode, ClusterService cluster) {
        this.leetCode = leetCode;
        this.cluster = cluster;
    }

    @GetMapping("/api/stats")
    public StatsResponse stats() {
        return new StatsResponse(leetCode.getSolvedCount(), cluster.getStats());
    }

    public record StatsResponse(Integer leetcodeSolved, ClusterStats cluster) {}
}