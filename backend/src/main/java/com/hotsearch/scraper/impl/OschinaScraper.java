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
 * 开源中国热门资讯爬虫
 * 抓取开源中国热门技术资讯数据
 */
@Component
public class OschinaScraper extends AbstractHotSearchScraper {

    public OschinaScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.oschina.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.oschina.url:https://www.oschina.net/ApiHomeNew/homeListByNewType?type=9998&pageSize=30}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "oschina";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseHtmlResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        // 设置JSON API请求头
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Referer", "https://www.oschina.net/?type=9998");
        headers.set("X-Requested-With", "XMLHttpRequest");
        // 不设置Accept-Encoding，让WebClient自动处理
    }

    private List<HotSearchDTO> parseHtmlResponse(String json) {
        List<HotSearchDTO> result = new ArrayList<>();

        // 添加调试日志，查看返回的JSON
        log.info("OSChina API response length: {}", json.length());
        log.debug("OSChina API response: {}", json.substring(0, Math.min(2000, json.length())));

        try {
            // 解析JSON响应
            JsonNode root = OBJECT_MAPPER.readTree(json);

            // 检查响应码
            int code = root.path("code").asInt(-1);
            if (code != 1) {
                log.warn("OSChina API returned non-success code: {}", code);
                return result;
            }

            // 获取新闻列表（字段名为 result）
            JsonNode newsList = root.path("result");
            if (!newsList.isArray() || newsList.isEmpty()) {
                log.warn("OSChina API returned empty news list");
                return result;
            }

            log.info("OSChina API returned {} news items", newsList.size());

            int rank = 1;
            for (JsonNode news : newsList) {
                HotSearchDTO dto = parseNewsItem(news, rank);
                if (dto != null) {
                    result.add(dto);
                    rank++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse OSChina API response: {}", e.getMessage(), e);
        }

        log.info("Parsed {} hot news from OSChina", result.size());
        return result;
    }

    /**
     * 解析单个新闻条目（从JSON）
     */
    private HotSearchDTO parseNewsItem(JsonNode news, int rank) {
        try {
            String title = news.path("obj_title").asText("").trim();
            int objId = news.path("obj_id").asInt(0);
            String url = "";

            // 构造URL
            if (objId > 0) {
                url = "https://www.oschina.net/news/" + objId;
            }

            // 数据验证
            if (title.isEmpty() || url.isEmpty()) {
                log.debug("Skipping OSChina item with empty title or url");
                return null;
            }

            // 获取热度值（阅读数）
            long heatValue = news.path("view_count").asLong(0);

            // 获取作者（从userVo对象中）
            String author = "";
            JsonNode userVo = news.path("userVo");
            if (!userVo.isMissingNode()) {
                author = userVo.path("name").asText("").trim();
            }

            // 获取摘要
            String detail = news.path("detail").asText("").trim();

            // 构建描述
            StringBuilder desc = new StringBuilder();
            if (!detail.isEmpty()) {
                desc.append(detail);
            }
            if (!author.isEmpty()) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append("作者: ").append(author);
            }

            log.debug("OSChina hot news: rank={}, title={}, heat={}, url={}",
                    rank, title, heatValue, url);

            return HotSearchDTO.builder()
                    .title(title)
                    .url(url)
                    .heatValue(heatValue)
                    .rankNum(rank)
                    .description(truncate(desc.toString(), 500))
                    .category("开源中国")
                    .build();

        } catch (Exception e) {
            log.error("Error parsing OSChina news item: {}", e.getMessage());
            return null;
        }
    }

}
