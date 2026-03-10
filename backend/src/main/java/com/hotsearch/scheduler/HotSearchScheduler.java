package com.hotsearch.scheduler;

import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.scraper.HotSearchScraper;
import com.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotSearchScheduler {

    private final List<HotSearchScraper> scrapers;
    private final HotSearchService hotSearchService;

    @Scheduled(fixedDelayString = "${scraper.interval:300000}")
    public void scheduledScrape() {
        log.info("Starting scheduled hot search scraping...");

        Flux.fromIterable(scrapers)
                .filter(HotSearchScraper::isEnabled)
                .flatMap(scraper -> scraper.scrape()
                        .flatMap(hotSearches -> {
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
                        .onErrorResume(error -> Mono.empty()))
                .collectList()
                .subscribe(
                        results -> log.info("Scheduled scraping completed. Results: {}", results),
                        error -> log.error("Scheduled scraping failed: {}", error.getMessage())
                );
    }
}
