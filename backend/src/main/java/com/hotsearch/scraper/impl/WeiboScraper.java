package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeiboScraper implements HotSearchScraper {

    private final WebClient webClient;

    @Value("${scraper.platforms.weibo.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.weibo.url:https://weibo.com/ajax/side/hotSearch}")
    private String url;

    @Override
    public String getPlatform() {
        return "weibo";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        return webClient.get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Weibo hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode realtimeNode = jsonNode.path("data").path("realtime");
        if (realtimeNode.isArray()) {
            int rank = 1;
            for (JsonNode node : realtimeNode) {
                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(node.path("word").asText())
                        .url("https://s.weibo.com/weibo?q=" + node.path("word_scheme").asText())
                        .heatValue(node.path("raw_hot").asLong())
                        .rankNum(rank++)
                        .category(node.path("category").asText())
                        .build();
                result.add(dto);
            }
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
