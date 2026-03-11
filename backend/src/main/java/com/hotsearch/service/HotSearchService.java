package com.hotsearch.service;

import com.hotsearch.dto.HotSearchDTO;

import java.util.List;
import java.util.Map;

/**
 * 热搜服务接口
 * 定义热搜数据的查询、保存和刷新操作
 */
public interface HotSearchService {

    /**
     * 获取所有支持的平台列表
     */
    List<String> getAllPlatforms();

    /**
     * 获取指定平台的热搜数据
     * @param platform 平台代码
     * @param limit 返回条数限制
     */
    List<HotSearchDTO> getHotSearchesByPlatform(String platform, int limit);

    /**
     * 获取所有平台的热搜数据
     * @param limit 每个平台返回条数限制
     */
    Map<String, List<HotSearchDTO>> getAllHotSearches(int limit);

    /**
     * 刷新所有热搜数据（清除缓存）
     */
    void refreshAllHotSearches();

    /**
     * 保存平台的热搜数据
     * @param platform 平台代码
     * @param hotSearches 热搜数据列表
     */
    void saveHotSearches(String platform, List<HotSearchDTO> hotSearches);
}
