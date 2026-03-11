package com.hotsearch.scraper;

import com.hotsearch.dto.HotSearchDTO;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 热搜爬虫接口
 * 定义各平台热搜数据抓取的规范
 * 所有平台爬虫都需要实现此接口
 */
public interface HotSearchScraper {

    /**
     * 获取平台代码（如weibo、zhihu等）
     */
    String getPlatform();

    /**
     * 执行数据抓取
     * @return 异步返回抓取到的热搜列表
     */
    Mono<List<HotSearchDTO>> scrape();

    /**
     * 检查该爬虫是否启用
     */
    boolean isEnabled();
}
