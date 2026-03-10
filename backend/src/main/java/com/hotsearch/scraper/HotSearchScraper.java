package com.hotsearch.scraper;

import com.hotsearch.dto.HotSearchDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HotSearchScraper {

    String getPlatform();

    Mono<List<HotSearchDTO>> scrape();

    boolean isEnabled();
}
