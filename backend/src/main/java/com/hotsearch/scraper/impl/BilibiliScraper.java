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
 * B站热搜爬虫
 * 抓取哔哩哔哩热门视频数据
 */
@Component
public class BilibiliScraper extends AbstractHotSearchScraper {

    public BilibiliScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.bilibili.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.bilibili.url:https://api.bilibili.com/x/web-interface/popular}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "bilibili";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Referer", "https://www.bilibili.com");
    }

    private List<HotSearchDTO> parseResponse(String responseBody) {
        List<HotSearchDTO> result = new ArrayList<>();
        JsonNode jsonNode = parseJson(responseBody);

        JsonNode listNode = jsonNode.path("data").path("list");
        if (!listNode.isArray()) {
            log.warn("Bilibili response does not contain valid list data");
            return result;
        }

        for (JsonNode node : listNode) {
            String title = node.path("title").asText();
            String bvid = node.path("bvid").asText();

            // 构建视频URL
            String itemUrl = node.path("short_link_v2").asText();
            if (itemUrl.isEmpty() && !bvid.isEmpty()) {
                itemUrl = "https://www.bilibili.com/video/" + bvid;
            }

            if (!isValidHotSearch(title, itemUrl)) {
                log.debug("Skipping invalid Bilibili hot search: title={}, bvid={}", title, bvid);
                continue;
            }

            long heatValue = node.path("stat").path("view").asLong();
            String iconUrl = node.path("pic").asText();

            // 构建描述：UP主名 + 推荐原因
            StringBuilder description = new StringBuilder(node.path("owner").path("name").asText());
            String reason = node.path("rcmd_reason").path("content").asText();
            if (!reason.isEmpty()) {
                description.append(" - ").append(reason);
            }

            HotSearchDTO dto = HotSearchDTO.builder()
                    .title(title)
                    .url(itemUrl)
                    .heatValue(heatValue)
                    .rankNum(0)  // 先设为0，稍后按播放量排序后重新设置
                    .iconUrl(iconUrl)
                    .description(truncate(description.toString(), 500))
                    .category("哔哩哔哩热门")
                    .build();
            result.add(dto);
        }

        // 按播放量降序排序
        result.sort((a, b) -> Long.compare(b.getHeatValue(), a.getHeatValue()));

        // 重新设置排名
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRankNum(i + 1);
        }

        return result;
    }
}
