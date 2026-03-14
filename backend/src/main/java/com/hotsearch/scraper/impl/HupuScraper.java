package com.hotsearch.scraper.impl;

import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.AbstractHotSearchScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 虎扑热搜爬虫
 * 抓取虎扑热门话题数据
 */
@Component
public class HupuScraper extends AbstractHotSearchScraper {

    public HupuScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.hupu.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.hupu.url:https://bbs.hupu.com/all-gambia}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "hupu";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseHtmlResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        super.addHeaders(headers);
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
    }

    private List<HotSearchDTO> parseHtmlResponse(String html) {
        List<HotSearchDTO> result = new ArrayList<>();

        try {
            Document document = Jsoup.parse(html);
            log.debug("Hupu page title: {}", document.title());

            // 尝试不同的选择器匹配热门话题元素
            Elements hotItems = findHotItems(document);
            log.debug("Found {} hot items from Hupu", hotItems.size());

            int rank = 1;
            for (Element item : hotItems) {
                HotSearchDTO dto = parseHotItem(item, rank);
                if (dto != null) {
                    result.add(dto);
                    rank++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Hupu HTML: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 查找热门话题元素
     */
    private Elements findHotItems(Document document) {
        Elements items = document.select(".hot-topic-list .topic-item");
        if (items.isEmpty()) {
            items = document.select(".hot-list .hot-item");
        }
        if (items.isEmpty()) {
            items = document.select(".list-item");
        }
        return items;
    }

    /**
     * 解析单个热门话题元素
     */
    private HotSearchDTO parseHotItem(Element item, int rank) {
        // 标题和链接
        Element titleElement = item.selectFirst("a");
        if (titleElement == null) {
            return null;
        }

        String title = titleElement.text().trim();
        String url = titleElement.attr("href");

        if (!url.startsWith("http")) {
            url = "https://bbs.hupu.com" + url;
        }

        if (!isValidHotSearch(title, url)) {
            log.debug("Skipping invalid Hupu hot search: title={}", title);
            return null;
        }

        // 热度值
        long heatValue = parseHupuHeatValue(item);

        log.debug("Hupu hot search: rank={}, title={}, heat={}", rank, title, heatValue);

        return HotSearchDTO.builder()
                .title(title)
                .url(url)
                .heatValue(heatValue)
                .rankNum(rank)
                .category("虎扑热搜")
                .build();
    }

    /**
     * 解析热度值
     */
    private long parseHupuHeatValue(Element item) {
        Element heatElement = item.selectFirst(".count, .hot-item-count, .topic-count");
        if (heatElement != null) {
            return parseHeatValue(heatElement.text());
        }
        return 0L;
    }
}
