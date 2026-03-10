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
public class ZhihuScraper implements HotSearchScraper {

    private final WebClient webClient;

    @Value("${scraper.platforms.zhihu.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.zhihu.url:https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total}")
    private String url;

    @Override
    public String getPlatform() {
        return "zhihu";
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
                .doOnError(error -> log.error("Failed to scrape Zhihu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode dataNode = jsonNode.path("data");
        if (dataNode.isArray()) {
            int rank = 1;
            for (JsonNode node : dataNode) {
                JsonNode targetNode = node.path("target");
                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(targetNode.path("title").asText())
                        .url(targetNode.path("url").asText())
                        .heatValue(node.path("detail_text").asText().replaceAll("[^0-9]", "").isEmpty() ? 
                                0 : Long.parseLong(node.path("detail_text").asText().replaceAll("[^0-9]", "")))
                        .rankNum(rank++)
                        .description(targetNode.path("excerpt").asText())
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
