package com.hotsearch.service;

import com.hotsearch.dto.HotSearchDTO;

import java.util.List;
import java.util.Map;

public interface HotSearchService {

    List<String> getAllPlatforms();

    List<HotSearchDTO> getHotSearchesByPlatform(String platform, int limit);

    Map<String, List<HotSearchDTO>> getAllHotSearches(int limit);

    void refreshAllHotSearches();

    void saveHotSearches(String platform, List<HotSearchDTO> hotSearches);
}
