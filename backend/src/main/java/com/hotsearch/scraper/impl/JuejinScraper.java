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
 * 掘金热门文章爬虫
 * 抓取掘金热门文章数据
 */
@Component
public class JuejinScraper extends AbstractHotSearchScraper {

    public JuejinScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.juejin.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.juejin.url:https://api.juejin.cn/content_api/v1/content/article_rank?category_id=1&type=hot}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "juejin";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Referer", "https://juejin.cn/hot/articles");
        headers.set("Origin", "https://juejin.cn");
    }

    private List<HotSearchDTO> parseResponse(String responseBody) {
        List<HotSearchDTO> result = new ArrayList<>();
        JsonNode jsonNode = parseJson(responseBody);

        // 掘金API返回结构: { "err_no": 0, "err_msg": "success", "data": [...] }
        if (jsonNode.path("err_no").asInt() != 0) {
            log.warn("Juejin API returned error: {}", jsonNode.path("err_msg").asText());
            return result;
        }

        JsonNode dataNode = jsonNode.path("data");
        if (!dataNode.isArray()) {
            log.warn("Juejin response does not contain valid data array");
            return result;
        }

        int rank = 1;
        for (JsonNode node : dataNode) {
            JsonNode contentNode = node.path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                continue;
            }

            String title = contentNode.path("title").asText();
            String contentId = contentNode.path("content_id").asText();

            // 构建文章URL
            String articleUrl = "https://juejin.cn/post/" + contentId;

            if (!isValidHotSearch(title, articleUrl)) {
                log.debug("Skipping invalid Juejin article: title={}", title);
                continue;
            }

            // 获取作者信息
            JsonNode authorNode = contentNode.path("author");
            String authorName = authorNode.path("name").asText();

            // 获取分类信息
            JsonNode categoryNode = contentNode.path("category");
            String categoryName = categoryNode.path("category_name").asText();
            if (categoryName.isEmpty()) {
                categoryName = "掘金热榜";
            }

            // 获取热度值（阅读数）
            JsonNode counterNode = node.path("content_counter");
            long heatValue = counterNode.path("view").asLong();
            if (heatValue == 0) {
                // 备用：使用点赞数
                heatValue = counterNode.path("like").asLong();
            }

            // 构建描述：作者 + 阅读数
            StringBuilder description = new StringBuilder();
            if (!authorName.isEmpty()) {
                description.append("作者: ").append(authorName);
            }
            long likeCount = counterNode.path("like").asLong();
            if (likeCount > 0) {
                if (description.length() > 0) {
                    description.append(" | ");
                }
                description.append("点赞: ").append(likeCount);
            }

            // 获取文章摘要（如果有）
            String briefContent = contentNode.path("brief_content").asText();
            if (!briefContent.isEmpty()) {
                if (description.length() > 0) {
                    description.append(" | ");
                }
                description.append(truncate(briefContent, 200));
            }

            HotSearchDTO dto = HotSearchDTO.builder()
                    .title(title)
                    .url(articleUrl)
                    .heatValue(heatValue)
                    .rankNum(rank++)
                    .description(truncate(description.toString(), 1000))
                    .category(categoryName)
                    .build();
            result.add(dto);
        }

        return result;
    }
}
