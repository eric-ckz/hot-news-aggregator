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
 * 懂车帝热门内容爬虫
 * 抓取懂车帝热门汽车资讯数据
 */
@Component
public class DongchediScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(DongchediScraper.class);
    private final WebClient webClient;

    // 构造器
    public DongchediScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.dongchedi.enabled:true}")
    private boolean enabled;

    // 懂车帝热门页面地址
    @Value("${scraper.platforms.dongchedi.url}")
    private String url;

    @Override
    public String getPlatform() {
        return "dongchedi";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        log.info("Starting to scrape Dongchedi hot content...");

        // TODO: 实现懂车帝热门内容抓取逻辑
        // 懂车帝有API接口可以使用
        return Mono.empty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
