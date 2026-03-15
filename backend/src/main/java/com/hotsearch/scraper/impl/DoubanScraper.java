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
 * 豆瓣热门话题爬虫
 * 抓取豆瓣小组讨论精选热门话题数据
 */
@Component
public class DoubanScraper extends AbstractHotSearchScraper {

    public DoubanScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.douban.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.douban.url:https://www.douban.com/group/explore}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "douban";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseHtmlResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        // 不设置父类的默认Accept，而是指定HTML
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Referer", "https://www.douban.com");
        // 不设置Accept-Encoding，让WebClient自动处理
    }

    private List<HotSearchDTO> parseHtmlResponse(String html) {
        List<HotSearchDTO> result = new ArrayList<>();

        // 添加调试日志，查看返回的HTML前2000字符
        log.debug("Douban HTML response preview: {}", html.substring(0, Math.min(2000, html.length())));

        try {
            Document document = Jsoup.parse(html);
            log.info("Douban HTML title: '{}'", document.title());

            // 豆瓣热门话题在 .channel-item 元素中
            Elements items = document.select(".channel-item");
            log.info("Found {} channel items from Douban", items.size());

            if (items.isEmpty()) {
                log.warn("No channel items found in Douban page. HTML length: {}, body: {}",
                        html.length(), document.body() != null ? document.body().text().substring(0, Math.min(500, document.body().text().length())) : "null");
                return result;
            }

            int rank = 1;
            for (Element item : items) {
                HotSearchDTO dto = parseChannelItem(item, rank);
                if (dto != null) {
                    result.add(dto);
                    rank++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Douban HTML: {}", e.getMessage(), e);
        }

        log.info("Parsed {} hot topics from Douban", result.size());
        return result;
    }

    /**
     * 解析单个频道条目
     */
    private HotSearchDTO parseChannelItem(Element item, int rank) {
        try {
            // 获取标题和链接
            Element titleElement = item.selectFirst("h3 a");
            if (titleElement == null) {
                return null;
            }

            String title = titleElement.text().trim();
            String url = titleElement.attr("href");

            if (!url.startsWith("http")) {
                url = "https://www.douban.com" + url;
            }

            // 数据验证
            if (!isValidHotSearch(title, url)) {
                log.debug("Skipping invalid Douban hot search: title={}", title);
                return null;
            }

            // 获取热度值（喜欢数）
            long heatValue = parseDoubanHeatValue(item);

            // 获取描述（来自 .block 中的内容）
            String description = extractDescription(item);

            // 获取来源信息
            String source = extractSource(item);
            if (!source.isEmpty()) {
                description = source + (description.isEmpty() ? "" : " | " + description);
            }

            log.debug("Douban hot topic: rank={}, title={}, heat={}, url={}",
                    rank, title, heatValue, url);

            return HotSearchDTO.builder()
                    .title(title)
                    .url(url)
                    .heatValue(heatValue)
                    .rankNum(rank)
                    .description(truncate(description, 500))
                    .category("豆瓣小组")
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Douban channel item: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析热度值（喜欢数）
     */
    private long parseDoubanHeatValue(Element item) {
        try {
            Element likesElement = item.selectFirst(".likes");
            if (likesElement != null) {
                String likesText = likesElement.text().trim();
                // 提取数字部分
                return parseHeatValue(likesText);
            }
        } catch (Exception e) {
            log.debug("Failed to parse heat value: {}", e.getMessage());
        }
        return 0L;
    }

    /**
     * 提取描述信息
     */
    private String extractDescription(Element item) {
        try {
            // 尝试从 .block 获取内容
            Element blockElement = item.selectFirst(".block");
            if (blockElement != null) {
                // 获取文本内容，去掉多余的空白
                String text = blockElement.text().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }

            // 尝试从 .abstract 获取（如果有的话）
            Element abstractElement = item.selectFirst(".abstract");
            if (abstractElement != null) {
                return abstractElement.text().trim();
            }
        } catch (Exception e) {
            log.debug("Failed to extract description: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 提取来源信息
     */
    private String extractSource(Element item) {
        try {
            // 获取来源小组信息
            Element fromElement = item.selectFirst(".source .from");
            if (fromElement != null) {
                return fromElement.text().trim();
            }
        } catch (Exception e) {
            log.debug("Failed to extract source: {}", e.getMessage());
        }
        return "";
    }
}
