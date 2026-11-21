package com.jameson.stats_api.leetcode;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LeetCodeClient {

    private static final String QUERY = """
        query getUserProfile($username: String!) {
          matchedUser(username: $username) {
            submitStatsGlobal { acSubmissionNum { difficulty count } }
          }
        }""";

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://leetcode.com")
            .defaultHeader("Referer", "https://leetcode.com")
            .defaultHeader("User-Agent", "Mozilla/5.0 (stats-api)")
            .build();

    /** Returns the total number of solved problems, or null if it can't be found. */
    public Integer fetchSolvedCount(String username) {
        GraphQlResponse response = restClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("query", QUERY, "variables", Map.of("username", username)))
                .retrieve()
                .body(GraphQlResponse.class);

        if (response == null || response.data() == null || response.data().matchedUser() == null) {
            return null;
        }
        return response.data().matchedUser().submitStatsGlobal().acSubmissionNum().stream()
                .filter(c -> "All".equals(c.difficulty()))
                .map(Count::count)
                .findFirst()
                .orElse(null);
    }

    // The shape of LeetCode's JSON response, as Java records
    record GraphQlResponse(Data data) {}
    record Data(MatchedUser matchedUser) {}
    record MatchedUser(SubmitStats submitStatsGlobal) {}
    record SubmitStats(List<Count> acSubmissionNum) {}
    record Count(String difficulty, int count) {}
}