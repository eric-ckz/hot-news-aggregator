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
 * 百度热搜爬虫
 * 抓取百度实时热点榜数据
 */
@Component
public class BaiduScraper extends AbstractHotSearchScraper {

    // 百度热搜API地址（实际API接口，不是配置中的页面URL）
    private static final String BAIDU_API_URL = "https://top.baidu.com/api/board?platform=wise&tab=realtime";

    public BaiduScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.baidu.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.baidu.url:https://top.baidu.com/board?tab=realtime}")
    public void setUrlValue(String url) {
        // 这里只是记录配置的URL，实际使用 BAIDU_API_URL
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "baidu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(BAIDU_API_URL, this::parseResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Referer", "https://top.baidu.com/board?tab=realtime");
    }

    private List<HotSearchDTO> parseResponse(String responseBody) {
        List<HotSearchDTO> result = new ArrayList<>();
        JsonNode jsonNode = parseJson(responseBody);

        // 百度热搜数据在 data.cards[0].content[0].content 中
        JsonNode cardsNode = jsonNode.path("data").path("cards");
        if (!cardsNode.isArray() || cardsNode.isEmpty()) {
            log.warn("Baidu response does not contain valid cards data");
            return result;
        }

        JsonNode wrapperNode = cardsNode.get(0).path("content");
        if (!wrapperNode.isArray() || wrapperNode.isEmpty()) {
            log.warn("Baidu response does not contain valid wrapper content data");
            return result;
        }

        JsonNode contentNode = wrapperNode.get(0).path("content");
        if (!contentNode.isArray()) {
            log.warn("Baidu response does not contain valid content data");
            return result;
        }

        for (JsonNode node : contentNode) {
            String title = node.path("word").asText();
            if (title.isEmpty()) {
                continue;
            }

            String itemUrl = normalizeBaiduUrl(node.path("url").asText(), title);
            long heatValue = parseBaiduHeatValue(node);
            int rank = node.path("index").asInt() + 1;
            String iconUrl = node.path("img").asText();
            String description = truncate(node.path("desc").asText(), 1000);

            if (!isValidHotSearch(title, itemUrl)) {
                log.debug("Skipping invalid Baidu hot search: title={}", title);
                continue;
            }

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

        return result;
    }

    /**
     * 获取百度的热度值
     */
    private long parseBaiduHeatValue(JsonNode node) {
        long heatValue = node.path("hotScore").asLong();
        if (heatValue == 0) {
            heatValue = parseHeatValue(node.path("hotScore").asText());
        }
        return heatValue;
    }

    /**
     * 规范化百度URL
     */
    private String normalizeBaiduUrl(String url, String title) {
        if (url == null || url.isEmpty()) {
            return "https://www.baidu.com/s?wd=" + encodeUrlParam(title) + "&sa=fyb_news&rsv_dl=fyb_news";
        }

        // 将 m.baidu.com 替换为 www.baidu.com
        url = url.replace("m.baidu.com", "www.baidu.com");

        // 将 word 参数替换为 wd 参数
        url = url.replace("word=", "wd=");

        // 添加 rsv_dl=fyb_news 参数
        if (!url.contains("rsv_dl=fyb_news")) {
            url = url.contains("?") ? url + "&rsv_dl=fyb_news" : url + "?rsv_dl=fyb_news";
        }

        return url;
    }
}
