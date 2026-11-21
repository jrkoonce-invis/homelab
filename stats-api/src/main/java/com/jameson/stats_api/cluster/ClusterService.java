package com.jameson.stats_api.cluster;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
public class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

    private final KubernetesClient client;
    private final String siteNamespace;
    private final String siteDeployment;

    public ClusterService(KubernetesClient client,
                          @Value("${site.namespace}") String siteNamespace,
                          @Value("${site.deployment}") String siteDeployment) {
        this.client = client;
        this.siteNamespace = siteNamespace;
        this.siteDeployment = siteDeployment;
    }

    /** Returns cluster stats, or null if the cluster can't be reached. */
    public ClusterStats getStats() {
        try {
            int nodes = client.nodes().list().getItems().size();

            List<Pod> pods = client.pods().inAnyNamespace().list().getItems();
            long running = pods.stream()
                    .filter(p -> "Running".equals(p.getStatus().getPhase()))
                    .count();

            return new ClusterStats(nodes, running, pods.size(), getSiteStats());
        } catch (Exception e) {
            log.warn("Could not read cluster stats: {}", e.getMessage());
            return null;
        }
    }

    private SiteStats getSiteStats() {
        Deployment deployment = client.apps().deployments()
                .inNamespace(siteNamespace).withName(siteDeployment).get();
        if (deployment == null) {
            return null;
        }

        int desired = Objects.requireNonNullElse(deployment.getSpec().getReplicas(), 0);
        int ready = Objects.requireNonNullElse(deployment.getStatus().getReadyReplicas(), 0);

        // Find the site's pods using the same label selector the Deployment uses
        List<Pod> sitePods = client.pods().inNamespace(siteNamespace)
                .withLabels(deployment.getSpec().getSelector().getMatchLabels())
                .list().getItems();

        String runningSince = sitePods.stream()
                .map(p -> p.getStatus().getStartTime())
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        int restarts = sitePods.stream()
                .flatMap(p -> p.getStatus().getContainerStatuses().stream())
                .mapToInt(cs -> cs.getRestartCount())
                .sum();

        return new SiteStats(ready, desired, runningSince, restarts);
    }

    public record ClusterStats(int nodes, long podsRunning, int podsTotal, SiteStats site) {}
    public record SiteStats(int replicasReady, int replicasDesired, String runningSince, int restarts) {}
}