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
public class BilibiliScraper implements HotSearchScraper {

    private final WebClient webClient;

    @Value("${scraper.platforms.bilibili.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.bilibili.url:https://api.bilibili.com/x/web-interface/ranking/v2}")
    private String url;

    @Override
    public String getPlatform() {
        return "bilibili";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        return webClient.get()
                .uri(url + "?rid=0&type=all")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.bilibili.com")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Bilibili hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode listNode = jsonNode.path("data").path("list");
        if (listNode.isArray()) {
            int rank = 1;
            for (JsonNode node : listNode) {
                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(node.path("title").asText())
                        .url("https://www.bilibili.com/video/" + node.path("bvid").asText())
                        .heatValue(node.path("stat").path("view").asLong())
                        .rankNum(rank++)
                        .iconUrl(node.path("pic").asText())
                        .description(node.path("owner").path("name").asText())
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
