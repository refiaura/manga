package com.example.mangareader.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Value("${jikan.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${jikan.read-timeout-ms}")
    private int readTimeoutMs;

    @Value("${jikan.base-url}")
    private String jikanBaseUrl;

    @Value("${jikan.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    @Bean
    public RestClient jikanRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(jikanBaseUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("mangaCatalog", "mangaDetail", "genres");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(2000));
        return manager;
    }
}
