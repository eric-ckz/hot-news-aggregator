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
 * 博客园热门文章爬虫
 * 抓取博客园头条热门文章数据
 */
@Component
public class CnblogsScraper extends AbstractHotSearchScraper {

    public CnblogsScraper(WebClient webClient) {
        super(webClient);
    }

    @Value("${scraper.platforms.cnblogs.enabled:true}")
    public void setEnabledValue(boolean enabled) {
        this.enabled = enabled;
    }

    @Value("${scraper.platforms.cnblogs.url:https://www.cnblogs.com/aggsite/headline}")
    public void setUrlValue(String url) {
        this.url = url;
    }

    @Override
    public String getPlatform() {
        return "cnblogs";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        return doScrape(url, this::parseHtmlResponse);
    }

    @Override
    protected void addHeaders(org.springframework.http.HttpHeaders headers) {
        // 设置HTML页面请求头
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Referer", "https://www.cnblogs.com");
        // 不设置Accept-Encoding，让WebClient自动处理
    }

    private List<HotSearchDTO> parseHtmlResponse(String html) {
        List<HotSearchDTO> result = new ArrayList<>();

        // 添加调试日志，查看返回的HTML前2000字符
        log.debug("Cnblogs HTML response preview: {}", html.substring(0, Math.min(2000, html.length())));

        try {
            Document document = Jsoup.parse(html);
            log.info("Cnblogs HTML title: '{}'", document.title());

            // 博客园头条文章在 .post-item 或 .article-item 等元素中
            // 尝试多种可能的选择器
            Elements items = document.select(".post-item, .article-item, .headline-item, .item");

            // 如果没找到，尝试其他常见结构
            if (items.isEmpty()) {
                items = document.select(".titlelnk");  // 直接选择标题链接
            }

            log.info("Found {} items from Cnblogs", items.size());

            if (items.isEmpty()) {
                log.warn("No items found in Cnblogs page. HTML length: {}, body preview: {}",
                        html.length(),
                        document.body() != null
                                ? document.body().text().substring(0, Math.min(500, document.body().text().length()))
                                : "null");
                return result;
            }

            int rank = 1;
            for (Element item : items) {
                HotSearchDTO dto = parseArticleItem(item, rank);
                if (dto != null) {
                    result.add(dto);
                    rank++;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Cnblogs HTML: {}", e.getMessage(), e);
        }

        log.info("Parsed {} hot articles from Cnblogs", result.size());
        return result;
    }

    /**
     * 解析单个文章条目
     */
    private HotSearchDTO parseArticleItem(Element item, int rank) {
        try {
            // 获取标题和链接 - 尝试多种可能的选择器
            Element titleElement = item.selectFirst("a.titlelnk, h2 a, .title a, a[href*=/p/]");

            // 如果item本身就是标题链接
            if (titleElement == null && item.tagName().equals("a") && item.hasAttr("href")) {
                titleElement = item;
            }

            if (titleElement == null) {
                return null;
            }

            String title = titleElement.text().trim();
            String url = titleElement.attr("href");

            // 处理相对链接
            if (!url.startsWith("http")) {
                url = "https://www.cnblogs.com" + url;
            }

            // 数据验证
            if (!isValidHotSearch(title, url)) {
                log.debug("Skipping invalid Cnblogs hot search: title={}", title);
                return null;
            }

            // 获取热度值（阅读数/推荐数）
            long heatValue = parseHeatValueFromItem(item);

            // 获取作者和描述信息
            String author = extractAuthor(item);
            String viewCount = extractViewCount(item);
            String description = buildDescription(author, viewCount);

            log.debug("Cnblogs hot article: rank={}, title={}, heat={}, author={}, url={}",
                    rank, title, heatValue, author, url);

            return HotSearchDTO.builder()
                    .title(title)
                    .url(url)
                    .heatValue(heatValue)
                    .rankNum(rank)
                    .description(truncate(description, 500))
                    .category("博客园")
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Cnblogs article item: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析热度值（阅读数/推荐数）
     */
    private long parseHeatValueFromItem(Element item) {
        try {
            // 尝试从父元素或兄弟元素获取阅读数/推荐数
            Element parent = item.parent();
            if (parent != null) {
                // 尝试查找阅读数元素
                Element viewElement = parent.selectFirst(".post-view, .article-view, .view-count, .views");
                if (viewElement != null) {
                    return parseHeatValue(viewElement.text());
                }

                // 尝试查找推荐数元素
                Element diggElement = parent.selectFirst(".post-digg, .article-digg, .digg-count, .diggs");
                if (diggElement != null) {
                    return parseHeatValue(diggElement.text());
                }

                // 尝试从文本中提取数字
                String parentText = parent.text();
                if (parentText.contains("阅读") || parentText.contains("推荐")) {
                    return parseHeatValue(parentText);
                }
            }

            // 在item自身中查找
            Element heatElement = item.selectFirst(".post-view, .article-view, .view-count, .views, .heat");
            if (heatElement != null) {
                return parseHeatValue(heatElement.text());
            }
        } catch (Exception e) {
            log.debug("Failed to parse heat value: {}", e.getMessage());
        }
        return 0L;
    }

    /**
     * 提取作者信息
     */
    private String extractAuthor(Element item) {
        try {
            // 尝试多种可能的选择器
            Element authorElement = item.selectFirst(".post-author, .article-author, .author, .nickname, .user-name");
            if (authorElement != null) {
                return authorElement.text().trim();
            }

            // 尝试从父元素中查找
            Element parent = item.parent();
            if (parent != null) {
                authorElement = parent.selectFirst(".post-author, .article-author, .author, .nickname, .user-name, a[href*=/u/]");
                if (authorElement != null) {
                    return authorElement.text().trim();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract author: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 提取阅读数文本
     */
    private String extractViewCount(Element item) {
        try {
            Element viewElement = item.selectFirst(".post-view, .article-view, .view-count, .views");
            if (viewElement != null) {
                return viewElement.text().trim();
            }

            // 尝试从父元素中查找
            Element parent = item.parent();
            if (parent != null) {
                viewElement = parent.selectFirst(".post-view, .article-view, .view-count, .views");
                if (viewElement != null) {
                    return viewElement.text().trim();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract view count: {}", e.getMessage());
        }
        return "";
    }

    /**
     * 构建描述信息
     */
    private String buildDescription(String author, String viewCount) {
        StringBuilder desc = new StringBuilder();
        if (!author.isEmpty()) {
            desc.append("作者: ").append(author);
        }
        if (!viewCount.isEmpty()) {
            if (desc.length() > 0) {
                desc.append(" | ");
            }
            desc.append(viewCount);
        }
        return desc.toString();
    }
}
