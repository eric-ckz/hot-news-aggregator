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
 * 掘金热门文章爬虫
 * 抓取掘金热门文章数据
 */
@Component
public class JuejinScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(JuejinScraper.class);
    private final WebClient webClient;

    // 构造器
    public JuejinScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.juejin.enabled:true}")
    private boolean enabled;

    // 掘金热门文章页面地址
    @Value("${scraper.platforms.juejin.url}")
    private String url;

    @Override
    public String getPlatform() {
        return "juejin";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        log.info("Starting to scrape Juejin hot articles...");

        // TODO: 实现掘金热门文章抓取逻辑
        // 掘金有API接口可以使用
        return Mono.empty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
