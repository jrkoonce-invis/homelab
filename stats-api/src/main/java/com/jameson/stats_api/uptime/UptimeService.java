package com.jameson.stats_api.uptime;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class UptimeService {

    private static final Logger log = LoggerFactory.getLogger(UptimeService.class);

    private final JdbcClient jdbc;
    private final String target;

    public UptimeService(JdbcClient jdbc, @Value("${uptime.target}") String target) {
        this.jdbc = jdbc;
        this.target = target;
    }

    /** Returns the uptime summary, or null if the database can't be reached. */
    public UptimeSummary getSummary() {
        try {
            return new UptimeSummary(
                    target,
                    percentage(Duration.ofHours(24)),
                    percentage(Duration.ofDays(7)),
                    percentage(Duration.ofDays(30)),
                    lastCheck().orElse(null));
        } catch (Exception e) {
            log.warn("Could not read uptime data: {}", e.getMessage());
            return null;
        }
    }

    /** Uptime % over the window, treating missing minutes as downtime. Null if no data yet. */
    private Double percentage(Duration window) {
        OffsetDateTime now = OffsetDateTime.now();
        Optional<OffsetDateTime> firstEver = jdbc.sql("SELECT MIN(checked_at) FROM uptime_checks")
                .query((rs, i) -> Optional.ofNullable(rs.getObject(1, OffsetDateTime.class)))
                .single();
        if (firstEver.isEmpty()) {
            return null;
        }

        // Don't count time before monitoring started as downtime
        OffsetDateTime windowStart = now.minus(window);
        OffsetDateTime start = firstEver.get().isAfter(windowStart) ? firstEver.get() : windowStart;

        long upChecks = jdbc.sql("""
                SELECT COUNT(*) FROM uptime_checks
                WHERE up AND checked_at >= :start
                """)
                .param("start", start)
                .query(Long.class)
                .single();

        long expected = Math.max(1, Duration.between(start, now).toMinutes());
        double pct = Math.min(100.0, 100.0 * upChecks / expected);
        return Math.round(pct * 100) / 100.0;   // 2 decimal places
    }

    private Optional<LastCheck> lastCheck() {
        return jdbc.sql("""
                SELECT checked_at, up, status_code, response_ms
                FROM uptime_checks ORDER BY checked_at DESC LIMIT 1
                """)
                .query((rs, i) -> new LastCheck(
                        rs.getObject("checked_at", OffsetDateTime.class).toString(),
                        rs.getBoolean("up"),
                        rs.getInt("status_code"),
                        rs.getInt("response_ms")))
                .optional();
    }

    public record UptimeSummary(String target, Double last24h, Double last7d, Double last30d,
                                LastCheck lastCheck) {}
    public record LastCheck(String checkedAt, boolean up, int statusCode, int responseMs) {}
}