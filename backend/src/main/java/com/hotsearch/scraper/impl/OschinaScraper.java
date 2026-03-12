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
 * 开源中国热门资讯爬虫
 * 抓取开源中国热门技术资讯数据
 */
@Component
public class OschinaScraper implements HotSearchScraper {

    private static final Logger log = LoggerFactory.getLogger(OschinaScraper.class);
    private final WebClient webClient;

    // 构造器
    public OschinaScraper(WebClient webClient) {
        this.webClient = webClient;
    }

    // 是否启用该爬虫
    @Value("${scraper.platforms.oschina.enabled:true}")
    private boolean enabled;

    // 开源中国热门资讯页面地址
    @Value("${scraper.platforms.oschina.url}")
    private String url;

    @Override
    public String getPlatform() {
        return "oschina";
    }

    @Override
    public Mono<List<HotSearchDTO>> scrape() {
        if (!enabled) {
            return Mono.empty();
        }

        log.info("Starting to scrape Oschina hot news...");

        // TODO: 实现开源中国热门资讯抓取逻辑
        // 开源中国有API接口可以使用
        return Mono.empty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
