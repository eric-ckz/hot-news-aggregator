package com.hotsearch.controller;

import com.hotsearch.dto.ApiResponse;
import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 热搜控制器
 * 提供热搜数据的RESTful API接口
 */
@RestController
@RequestMapping("/api/hot-search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotSearchController {

    private final HotSearchService hotSearchService;

    /**
     * 获取所有支持的平台列表
     */
    @GetMapping("/platforms")
    public ApiResponse<List<String>> getAllPlatforms() {
        return ApiResponse.success(hotSearchService.getAllPlatforms());
    }

    /**
     * 获取指定平台的热搜数据
     * @param platform 平台代码（如weibo、zhihu等）
     * @param limit 返回条数限制，默认50条
     */
    @GetMapping("/{platform}")
    public ApiResponse<List<HotSearchDTO>> getHotSearchesByPlatform(
            @PathVariable String platform,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(hotSearchService.getHotSearchesByPlatform(platform, limit));
    }

    /**
     * 获取所有平台的热搜数据
     * @param limit 每个平台返回条数限制，默认50条
     */
    @GetMapping("/all")
    public ApiResponse<Map<String, List<HotSearchDTO>>> getAllHotSearches(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(hotSearchService.getAllHotSearches(limit));
    }

    /**
     * 手动刷新所有平台的热搜数据（清除缓存）
     */
    @GetMapping("/refresh")
    public ApiResponse<String> refreshHotSearches() {
        hotSearchService.refreshAllHotSearches();
        return ApiResponse.success("刷新成功");
    }
}
