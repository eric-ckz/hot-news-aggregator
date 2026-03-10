package com.hotsearch.controller;

import com.hotsearch.dto.ApiResponse;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hot-search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotSearchController {

    private final HotSearchService hotSearchService;

    @GetMapping("/platforms")
    public ApiResponse<List<String>> getAllPlatforms() {
        return ApiResponse.success(hotSearchService.getAllPlatforms());
    }

    @GetMapping("/{platform}")
    public ApiResponse<List<HotSearchDTO>> getHotSearchesByPlatform(
            @PathVariable String platform,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(hotSearchService.getHotSearchesByPlatform(platform, limit));
    }

    @GetMapping("/all")
    public ApiResponse<Map<String, List<HotSearchDTO>>> getAllHotSearches(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(hotSearchService.getAllHotSearches(limit));
    }

    @GetMapping("/refresh")
    public ApiResponse<String> refreshHotSearches() {
        hotSearchService.refreshAllHotSearches();
        return ApiResponse.success("刷新成功");
    }
}
