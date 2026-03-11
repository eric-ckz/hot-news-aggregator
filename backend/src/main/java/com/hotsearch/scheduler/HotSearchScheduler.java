package com.hotsearch.scheduler;

import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import com.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 热搜定时抓取调度器
 * 负责定时触发各平台热搜数据的抓取任务
 */
@Component
@RequiredArgsConstructor
public class HotSearchScheduler {

    private static final Logger log = LoggerFactory.getLogger(HotSearchScheduler.class);

    // 所有启用的爬虫列表（Spring自动注入所有HotSearchScraper实现）
    private final List<HotSearchScraper> scrapers;
    private final HotSearchService hotSearchService;
    // 标记应用是否已就绪，避免启动时立即执行
    private final AtomicBoolean applicationReady = new AtomicBoolean(false);

    /**
     * 应用启动完成后执行一次抓取
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application is ready, enabling scheduled scraping...");
        applicationReady.set(true);
        executeScraping();
    }

    /**
     * 定时抓取任务
     * initialDelay: 首次延迟60秒执行（默认）
     * fixedDelay: 之后每5分钟执行一次（默认）
     */
    @Scheduled(initialDelayString = "${scraper.initial-delay:60000}", fixedDelayString = "${scraper.interval:300000}")
    public void scheduledScrape() {
        if (!applicationReady.get()) {
            log.debug("Application not ready yet, skipping scheduled scrape");
            return;
        }
        executeScraping();
    }

    /**
     * 执行实际的抓取逻辑
     * 使用Reactive编程并发处理各平台的抓取任务
     */
    private void executeScraping() {
        log.info("Starting hot search scraping...");

        Flux.fromIterable(scrapers)
                .filter(HotSearchScraper::isEnabled)  // 只处理启用的爬虫
                .flatMap(scraper -> scraper.scrape()
                        .flatMap(hotSearches -> {
                            // 抓取成功且数据不为空时，保存到数据库
                            if (hotSearches != null && !hotSearches.isEmpty()) {
                                hotSearchService.saveHotSearches(scraper.getPlatform(), hotSearches);
                                return Mono.just(scraper.getPlatform() + ": " + hotSearches.size());
                            }
                            return Mono.empty();
                        })
                        .doOnSuccess(result -> {
                            if (result != null) {
                                log.info("Successfully scraped {}", result);
                            }
                        })
                        .doOnError(error -> log.error("Error scraping {}: {}", scraper.getPlatform(), error.getMessage()))
                        .onErrorResume(error -> Mono.empty()))  // 单个平台失败不影响其他平台
                .collectList()
                .subscribe(
                        results -> log.info("Scraping completed. Results: {}", results),
                        error -> log.error("Scraping failed: {}", error.getMessage())
                );
    }
}
