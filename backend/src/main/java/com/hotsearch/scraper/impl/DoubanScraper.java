package com.hotsearch.scraper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 豆瓣热门话题爬虫
 * 抓取豆瓣小组热门话题数据
 */
@Component
public class DoubanScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(DoubanScraper.class);
    private final WebClient webClient;

    // 构造器
    public DoubanScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.douban.enabled:true}")
    private boolean enabled;

    // 豆瓣热门话题页面地址
    @Value("${scraper.platforms.douban.url}")
    private String url;

    @Override
    public String getPlatform() {
        return "douban";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        log.info("Starting to scrape Douban hot topics...");

        // TODO: 实现豆瓣热门话题抓取逻辑
        // 豆瓣页面需要解析HTML，可能需要使用Jsoup
        return Mono.empty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
