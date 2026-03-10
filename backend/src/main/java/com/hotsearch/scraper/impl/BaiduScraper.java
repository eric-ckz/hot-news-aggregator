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
public class BaiduScraper implements HotSearchScraper {

    private final WebClient webClient;

    @Value("${scraper.platforms.baidu.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.baidu.url:https://top.baidu.com/board?tab=realtime}")
    private String url;

    @Override
    public String getPlatform() {
        return "baidu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        return webClient.get()
                .uri("https://top.baidu.com/api/board?platform=wise&tab=realtime")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Baidu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode dataNode = jsonNode.path("data").path("cards");
        if (dataNode.isArray() && dataNode.size() > 0) {
            JsonNode contentNode = dataNode.get(0).path("content");
            if (contentNode.isArray()) {
                int rank = 1;
                for (JsonNode node : contentNode) {
                    HotSearchDTO dto = HotSearchDTO.builder()
                            .title(node.path("word").asText())
                            .url(node.path("rawUrl").asText())
                            .heatValue(parseHeatValue(node.path("hotScore").asText()))
                            .rankNum(rank++)
                            .iconUrl(node.path("img").asText())
                            .description(node.path("desc").asText())
                            .build();
                    result.add(dto);
                }
            }
        }

        return result;
    }

    private Long parseHeatValue(String heatText) {
        if (heatText == null || heatText.isEmpty()) {
            return 0L;
        }
        try {
            String numeric = heatText.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 0L : Long.parseLong(numeric);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
