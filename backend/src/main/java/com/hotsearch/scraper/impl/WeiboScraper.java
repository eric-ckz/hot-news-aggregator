package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 微博热搜爬虫
 * 抓取微博实时热搜榜数据
 */
@Component
@RequiredArgsConstructor
public class WeiboScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(WeiboScraper.class);
    private final WebClient webClient;

    // 是否启用该爬虫，可通过配置文件控制
    @Value("${scraper.platforms.weibo.enabled:true}")
    private boolean enabled;

    // 微博热搜API地址
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
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
                .header("Referer", "https://s.weibo.com/top/summary")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseResponse)
                .doOnError(error -> log.error("Failed to scrape Weibo hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    /**
     * 解析微博API响应数据
     * 微博官方API格式: {"data": {"realtime": [...]}}
     */
    private List<HotSearchDTO> parseResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        JsonNode realtimeNode = jsonNode.path("data").path("realtime");
        if (realtimeNode.isArray()) {
            for (JsonNode node : realtimeNode) {
                String title = node.path("word").asText();
                String wordScheme = node.path("word_scheme").asText();
                long hotValue = node.path("num").asLong();
                int rank = node.path("rank").asInt();
                String labelName = node.path("label_name").asText();

                // 构建完整的URL，对参数值进行URL编码
                String queryParam = wordScheme.isEmpty() ? title : wordScheme;
                try {
                    queryParam = java.net.URLEncoder.encode(queryParam, "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    log.error("Failed to encode URL parameter: {}", e.getMessage());
                }
                String hotSearchUrl = "https://s.weibo.com/weibo?q=" + queryParam;

                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(title)
                        .url(hotSearchUrl)
                        .heatValue(hotValue)
                        .rankNum(rank)
                        .category(labelName.isEmpty() ? "hot" : labelName)
                        .build();
                result.add(dto);
            }

            // 按排名排序
            result.sort(Comparator.comparingInt(HotSearchDTO::getRankNum));
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
