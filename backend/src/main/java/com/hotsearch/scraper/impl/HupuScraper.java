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
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseHtmlResponse)
                .doOnError(error -> log.error("Failed to scrape Hupu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private List<HotSearchDTO> parseHtmlResponse(String html) {
        List<HotSearchDTO> result = new ArrayList<>();

        try {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(html);
            log.info("Hupu HTML parsed successfully");
            
            // 打印页面标题，确认页面是否正确加载
            log.info("Hupu page title: {}", document.title());
            
            // 尝试不同的选择器
            org.jsoup.select.Elements hotItems = document.select(".hot-topic-list .topic-item");
            if (hotItems.isEmpty()) {
                hotItems = document.select(".hot-list .hot-item");
            }
            if (hotItems.isEmpty()) {
                hotItems = document.select(".list-item");
            }
            
            log.info("Found {} hot items", hotItems.size());

            int rank = 1;
            for (org.jsoup.nodes.Element item : hotItems) {
                // 标题和链接
                org.jsoup.nodes.Element titleElement = item.selectFirst("a");
                if (titleElement == null) continue;

                String title = titleElement.text();
                String url = titleElement.attr("href");
                if (!url.startsWith("http")) {
                    url = "https://bbs.hupu.com" + url;
                }

                // 热度值
                org.jsoup.nodes.Element heatElement = item.selectFirst(".count, .hot-item-count, .topic-count");
                long heatValue = 0;
                if (heatElement != null) {
                    String heatText = heatElement.text().replaceAll("[^0-9]", "");
                    if (!heatText.isEmpty()) {
                        heatValue = Long.parseLong(heatText);
                    }
                }

                log.info("Hupu hot search: rank={}, title={}, url={}, heat={}", rank, title, url, heatValue);

                HotSearchDTO dto = HotSearchDTO.builder()
                        .title(title)
                        .url(url)
                        .heatValue(heatValue)
                        .rankNum(rank++)
                        .category("虎扑热搜")
                        .build();
                result.add(dto);
            }
        } catch (Exception e) {
            log.error("Failed to parse Hupu HTML: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("Parsed {} hot searches from Hupu", result.size());
        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
