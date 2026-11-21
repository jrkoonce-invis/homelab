package com.jameson.stats_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatsApiApplication.class, args);
    }
}