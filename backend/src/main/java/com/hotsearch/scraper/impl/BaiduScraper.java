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
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .header("Referer", "https://top.baidu.com/board?tab=realtime")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Baidu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        try {
            List<JsonNode> candidates = new ArrayList<>();
            findNodesWithWord(jsonNode, candidates);
            
            for (JsonNode node : candidates) {
                String title = node.path("word").asText();
                if (title.isEmpty()) {
                    continue;
                }

                String itemUrl = node.path("url").asText();
                if (itemUrl.isEmpty()) {
                    try {
                        itemUrl = "https://www.baidu.com/s?wd=" + java.net.URLEncoder.encode(title, "UTF-8") + "&sa=fyb_news&rsv_dl=fyb_news";
                    } catch (java.io.UnsupportedEncodingException e) {
                        itemUrl = "https://www.baidu.com/s?wd=" + title + "&sa=fyb_news&rsv_dl=fyb_news";
                    }
                } else {
                    // 将m.baidu.com替换为www.baidu.com
                    itemUrl = itemUrl.replace("m.baidu.com", "www.baidu.com");
                    // 将word参数替换为wd参数
                    itemUrl = itemUrl.replace("word=", "wd=");
                    // 添加rsv_dl=fyb_news参数
                    if (!itemUrl.contains("rsv_dl=fyb_news")) {
                        if (itemUrl.contains("?")) {
                            itemUrl += "&rsv_dl=fyb_news";
                        } else {
                            itemUrl += "?rsv_dl=fyb_news";
                        }
                    }
                }

                long heatValue = node.path("hotScore").asLong();
                if (heatValue == 0) {
                    heatValue = parseHeatValue(node.path("hotScore").asText());
                }

                int rank = node.path("index").asInt() + 1;
                String iconUrl = node.path("img").asText();
                String description = node.path("desc").asText();

                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(title)
                        .url(itemUrl)
                        .heatValue(heatValue)
                        .rankNum(rank)
                        .iconUrl(iconUrl)
                        .description(description)
                        .category("百度热搜")
                        .build();
                result.add(dto);
            }
        } catch (Exception e) {
            log.error("Error parsing Baidu response: {}", e.getMessage(), e);
        }

        log.info("Parsed {} hot searches from Baidu", result.size());
        return result;
    }
    
    private void findNodesWithWord(JsonNode node, List<JsonNode> result) {
        if (node == null) {
            return;
        }
        
        if (node.has("word")) {
            result.add(node);
        }
        
        if (node.isArray()) {
            for (JsonNode child : node) {
                findNodesWithWord(child, result);
            }
        } else if (node.isObject()) {
            for (JsonNode child : node) {
                findNodesWithWord(child, result);
            }
        }
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
