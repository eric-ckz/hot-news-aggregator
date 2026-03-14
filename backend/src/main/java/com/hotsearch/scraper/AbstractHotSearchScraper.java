package com.hotsearch.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotsearch.dto.HotSearchDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * 热搜爬虫抽象基类
 * 提供通用的爬虫功能实现，减少重复代码
 */
public abstract class AbstractHotSearchScraper implements HotSearchScraper {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final WebClient webClient;

    // 是否启用该爬虫，可通过配置文件控制
    protected boolean enabled = true;

    // 爬虫URL
    protected String url;

    protected AbstractHotSearchScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态（用于配置注入）
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置URL（用于配置注入）
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 执行抓取的标准模板
     */
    protected Mono<List<HotSearchDTO>> doScrape(String requestUrl, Function<String, List<HotSearchDTO>> parser) {
        if (!enabled) {
            log.debug("{} scraper is disabled, skipping", getPlatform());
            return Mono.empty();
        }

        log.info("Starting to scrape {} hot search from: {}", getPlatform(), requestUrl);

        return webClient.get()
                .uri(requestUrl)
                .headers(this::addHeaders)
                .retrieve()
                .bodyToMono(String.class)
                .map(parser)
                .doOnSuccess(result -> {
                    if (result != null && !result.isEmpty()) {
                        log.info("Successfully scraped {} hot searches from {}", result.size(), getPlatform());
                    } else {
                        log.warn("No hot searches found from {}", getPlatform());
                    }
                })
                .doOnError(error -> log.error("Failed to scrape {} hot search: {}", getPlatform(), error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    /**
     * 添加请求头，子类可覆盖添加特定header
     */
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Connection", "keep-alive");
    }

    /**
     * 安全地对字符串进行URL编码
     */
    protected String encodeUrlParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to encode URL parameter '{}': {}", param, e.getMessage());
            return param;
        }
    }

    /**
     * 解析JSON字符串为JsonNode
     */
    protected JsonNode parseJson(String jsonString) {
        try {
            return OBJECT_MAPPER.readTree(jsonString);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            throw new RuntimeException("JSON parse failed", e);
        }
    }

    /**
     * 安全地解析数字字符串
     */
    protected long parseHeatValue(String heatText) {
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

    /**
     * 验证热搜数据是否有效
     */
    protected boolean isValidHotSearch(String title, String url) {
        return title != null && !title.isBlank() && title.length() <= 200
                && url != null && !url.isBlank()
                && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * 截断长文本到指定长度
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本，如果超长会添加 "..."
     */
    protected String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        // 预留3个字符给 "..."
        return text.substring(0, maxLength - 3) + "...";
    }
}
