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
import java.util.List;

@Component
@RequiredArgsConstructor
public class BilibiliScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(BilibiliScraper.class);
    private final WebClient webClient;

    @Value("${scraper.platforms.bilibili.enabled:true}")
    private boolean enabled;

    @Value("${scraper.platforms.bilibili.url}")
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

        // 使用配置文件中的URL获取热门视频数据
        return webClient.get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
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
            for (JsonNode node : listNode) {
                String title = node.path("title").asText();
                String itemUrl = node.path("short_link_v2").asText();
                if (itemUrl.isEmpty()) {
                    String bvid = node.path("bvid").asText();
                    itemUrl = "https://www.bilibili.com/video/" + bvid;
                }
                long heatValue = node.path("stat").path("view").asLong();
                String iconUrl = node.path("pic").asText();
                String description = node.path("owner").path("name").asText();
                // 添加推荐原因
                String reason = node.path("rcmd_reason").path("content").asText();
                if (!reason.isEmpty()) {
                    description += " - " + reason;
                }

                log.info("Bilibili hot search: title={}, url={}, heat={}", title, itemUrl, heatValue);

                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(title)
                        .url(itemUrl)
                        .heatValue(heatValue)
                        .rankNum(0) // 暂时设置为0，稍后按播放量排序后重新设置
                        .iconUrl(iconUrl)
                        .description(description)
                        .category("哔哩哔哩热门")
                        .build();
                result.add(dto);
            }
        }

        // 按播放量降序排序
        result.sort((a, b) -> Long.compare(b.getHeatValue(), a.getHeatValue()));
        
        // 重新设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRankNum(i + 1);
        }

        log.info("Parsed {} hot searches from Bilibili", result.size());
        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
