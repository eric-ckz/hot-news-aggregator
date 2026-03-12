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
 * 博客园热门文章爬虫
 * 抓取博客园首页热门文章数据
 */
@Component
public class CnblogsScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(CnblogsScraper.class);
    private final WebClient webClient;

    // 构造器
    public CnblogsScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.cnblogs.enabled:true}")
    private boolean enabled;

    // 博客园热门文章页面地址
    @Value("${scraper.platforms.cnblogs.url}")
    private String url;

    @Override
    public String getPlatform() {
        return "cnblogs";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        log.info("Starting to scrape Cnblogs hot articles...");

        // TODO: 实现博客园热门文章抓取逻辑
        // 博客园页面需要解析HTML
        return Mono.empty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
