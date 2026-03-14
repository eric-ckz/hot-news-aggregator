package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.AbstractHotSearchScraper;
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
public class ZhihuScraper extends AbstractHotSearchScraper {

    public ZhihuScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.zhihu.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.zhihu.url:https://api.zhihu.com/topstory/hot-list}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "zhihu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Referer", "https://www.zhihu.com/hot");
        headers.set("DNT", "1");
        headers.set("Sec-GPC", "1");
    }

    private List<HotSearchDTO> parseResponse(String responseBody) {
        List<HotSearchDTO> result = new ArrayList<>();

        // 清理JSON字符串中的控制字符
        String cleanedJson = responseBody.replaceAll("[\\x00-\\x1F\\x7F]", "");
        JsonNode jsonNode = parseJson(cleanedJson);

        JsonNode dataNode = jsonNode.path("data");
        if (!dataNode.isArray()) {
            log.warn("Zhihu response does not contain valid data array");
            return result;
        }

        int rank = 1;
        for (JsonNode node : dataNode) {
            JsonNode target = node.path("target");
            String title = target.path("title").asText();
            String url = target.path("url").asText();

            // 处理知乎API返回的URL
            url = normalizeZhihuUrl(url);

            if (!isValidHotSearch(title, url)) {
                log.debug("Skipping invalid Zhihu hot search: title={}", title);
                continue;
            }

            long heatValue = parseHeatValue(node.path("detail_text").asText());
            String description = truncate(target.path("excerpt").asText(), 1000);

            HotSearchDTO dto = HotSearchDTO.builder()
                    .title(title)
                    .url(url)
                    .heatValue(heatValue)
                    .rankNum(rank++)
                    .description(description)
                    .category("知乎热榜")
                    .build();
            result.add(dto);
        }

        return result;
    }

    /**
     * 规范化知乎URL
     */
    private String normalizeZhihuUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        // 将 api.zhihu.com 替换为 www.zhihu.com
        url = url.replace("https://api.zhihu.com", "https://www.zhihu.com");

        // 处理相对路径
        if (!url.startsWith("http")) {
            url = url.startsWith("/") ? "https://www.zhihu.com" + url : "https://www.zhihu.com/" + url;
        }

        // 将 /questions/ 改为 /question/
        url = url.replace("/questions/", "/question/");

        return url;
    }
}
