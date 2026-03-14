package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.AbstractHotSearchScraper;
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
public class WeiboScraper extends AbstractHotSearchScraper {

    public WeiboScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.weibo.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.weibo.url:https://weibo.com/ajax/side/hotSearch}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "weibo";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Referer", "https://s.weibo.com/top/summary");
        headers.set("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148");
    }

    private List<HotSearchDTO> parseResponse(String responseBody) {
        List<HotSearchDTO> result = new ArrayList<>();
        JsonNode jsonNode = parseJson(responseBody);

        JsonNode realtimeNode = jsonNode.path("data").path("realtime");
        if (!realtimeNode.isArray()) {
            log.warn("Weibo response does not contain valid realtime data");
            return result;
        }

        for (JsonNode node : realtimeNode) {
            String title = node.path("word").asText();
            String wordScheme = node.path("word_scheme").asText();
            long hotValue = node.path("num").asLong();
            int rank = node.path("rank").asInt();
            String labelName = node.path("label_name").asText();

            // 构建完整的URL
            String queryParam = wordScheme.isEmpty() ? title : wordScheme;
            String hotSearchUrl = "https://s.weibo.com/weibo?q=" + encodeUrlParam(queryParam);

            if (!isValidHotSearch(title, hotSearchUrl)) {
                log.debug("Skipping invalid Weibo hot search: title={}, url={}", title, hotSearchUrl);
                continue;
            }

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
        return result;
    }
}
