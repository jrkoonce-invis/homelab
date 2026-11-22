package com.jameson.stats_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jameson.stats_api.cluster.ClusterService;
import com.jameson.stats_api.cluster.ClusterService.ClusterStats;
import com.jameson.stats_api.leetcode.LeetCodeService;
import com.jameson.stats_api.uptime.UptimeService;
import com.jameson.stats_api.uptime.UptimeService.UptimeSummary;

@RestController
public class StatsController {

    private final LeetCodeService leetCode;
    private final ClusterService cluster;
    private final UptimeService uptime;

    public StatsController(LeetCodeService leetCode, ClusterService cluster, UptimeService uptime) {
        this.leetCode = leetCode;
        this.cluster = cluster;
        this.uptime = uptime;
    }

    @GetMapping("/api/stats")
    public StatsResponse stats() {
        return new StatsResponse(leetCode.getSolvedCount(), cluster.getStats(), uptime.getSummary());
    }

    @GetMapping("/api/uptime")
    public UptimeSummary uptime() {
        return uptime.getSummary();
    }

    public record StatsResponse(Integer leetcodeSolved, ClusterStats cluster, UptimeSummary uptime) {}
}