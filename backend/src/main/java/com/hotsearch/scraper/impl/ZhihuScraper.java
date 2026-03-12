package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 知乎热搜爬虫
 * 抓取知乎热榜数据
 */
@Component
public class ZhihuScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(ZhihuScraper.class);
    private final WebClient webClient;

    // 构造器
    public ZhihuScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.zhihu.enabled:true}")
    private boolean enabled;

    // 知乎热榜API地址
    @Value("${scraper.platforms.zhihu.url:https://api.zhihu.com/topstory/hot-list}")
    private String url;

    @Override
    public String getPlatform() {
        return "zhihu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        log.info("Starting to scrape Zhihu hot search...");
        if (!enabled) {
            log.info("Zhihu scraper is disabled");
            return Mono.empty();
        }
        log.info("Using Zhihu API URL: {}", url);

        return webClient.get()
                .uri(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .header("Referer", "https://www.zhihu.com/hot")
                .header("DNT", "1")
                .header("Sec-GPC", "1")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::cleanJsonString)
                .map(this::parseJsonString)
                .map(this::parseJsonResponse)
                .doOnError(error -> log.error("Failed to scrape Zhihu hot search: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    /**
     * 清理JSON字符串中的控制字符
     * 避免解析失败
     */
    private String cleanJsonString(String jsonString) {
        return jsonString.replaceAll("[\\x00-\\x1F\\x7F]", "");
    }

    private JsonNode parseJsonString(String jsonString) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonString);
        } catch (Exception e) {
            log.error("Failed to parse JSON string: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<HotSearchDTO> parseJsonResponse(JsonNode jsonNode) {
        List<HotSearchDTO> result = new ArrayList<>();

        try {
            JsonNode dataNode = jsonNode.path("data");
            if (dataNode.isArray()) {
                int rank = 1;
                for (JsonNode node : dataNode) {
                    JsonNode targetNode = node.path("target");
                    if (targetNode.isMissingNode()) continue;

                    String title = targetNode.path("title").asText();
                    String url = targetNode.path("url").asText();
                    
                    // 处理知乎API返回的URL
                    if (url.startsWith("https://api.zhihu.com")) {
                        // 将api.zhihu.com替换为www.zhihu.com
                        url = url.replace("https://api.zhihu.com", "https://www.zhihu.com");
                    } else if (!url.startsWith("http")) {
                        // 处理相对路径
                        if (url.startsWith("/")) {
                            url = "https://www.zhihu.com" + url;
                        } else {
                            url = "https://www.zhihu.com/" + url;
                        }
                    }
                    
                    // 确保URL是标准格式，特别是问题URL
                    if (url.contains("/questions/")) {
                        // 将questions改为question，确保URL格式正确
                        url = url.replace("/questions/", "/question/");
                    }
                    // 确保使用www.zhihu.com域名
                    url = url.replace("https://api.zhihu.com", "https://www.zhihu.com");

                    long heatValue = 0;
                    String heatText = node.path("detail_text").asText();
                    if (!heatText.isEmpty()) {
                        heatText = heatText.replaceAll("[^0-9]", "");
                        if (!heatText.isEmpty()) {
                            heatValue = Long.parseLong(heatText);
                        }
                    }

                    String description = targetNode.path("excerpt").asText();

                    HotSearchDTO dto = HotSearchDTO.builder()
                            .title(title)
                            .url(url)
                            .heatValue(heatValue)
                            .rankNum(rank++)
                            .description(description)
                            .build();
                    result.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Zhihu JSON: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
