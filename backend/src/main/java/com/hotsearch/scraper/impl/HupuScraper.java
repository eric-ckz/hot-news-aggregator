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
public class HupuScraper implements HotSearchScraper {

    private final WebClient webClient;

    @Value("${scraper.platforms.hupu.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.hupu.url:https://bbs.hupu.com/all-gambia}")
    private String url;

    @Override
    public String getPlatform() {
        return "hupu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        return webClient.get()
                .uri("https://bbs.hupu.com/api/v1/hot-threads")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Hupu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode dataNode = jsonNode.path("data").path("threads");
        if (dataNode.isArray()) {
            int rank = 1;
            for (JsonNode node : dataNode) {
                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(node.path("title").asText())
                        .url("https://bbs.hupu.com/" + node.path("tid").asText() + ".html")
                        .heatValue(node.path("replies").asLong())
                        .rankNum(rank++)
                        .category(node.path("forum").path("name").asText())
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
