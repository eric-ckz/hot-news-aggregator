package com.hotsearch.service.impl;

import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.entity.HotSearch;
import com.hotsearch.mapper.HotSearchMapper;
import com.hotsearch.service.HotSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 热搜服务实现类
 * 实现热搜数据的查询、缓存、保存等业务逻辑
 * 使用Redis缓存减少数据库访问，提高查询性能
 */
@Service
public class HotSearchServiceImpl implements HotSearchService {

    private static final Logger log = LoggerFactory.getLogger(HotSearchServiceImpl.class);
    private final HotSearchMapper hotSearchMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 构造器
    public HotSearchServiceImpl(HotSearchMapper hotSearchMapper, RedisTemplate<String, Object> redisTemplate) {
        this.hotSearchMapper = hotSearchMapper;
        this.redisTemplate = redisTemplate;
    }

    // Redis缓存键前缀
    private static final String REDIS_KEY_PREFIX = "hotsearch:";
    // Redis缓存过期时间：2小时
    private static final long REDIS_EXPIRE_HOURS = 2;

    @Override
    public List<String> getAllPlatforms() {
        String cacheKey = REDIS_KEY_PREFIX + "platforms";
        
        // 先尝试从Redis缓存获取
        try {
            @SuppressWarnings("unchecked")
            List<String> platforms = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (platforms != null && !platforms.isEmpty()) {
                return platforms;
            }
        } catch (DataAccessException e) {
            // Redis访问失败，降级到数据库查询
            log.warn("Redis access failed, falling back to database: {}", e.getMessage());
        }

        // 从数据库查询平台列表
        List<String> platforms = hotSearchMapper.findAllPlatforms();
        
        // 将查询结果写入Redis缓存
        try {
            if (!platforms.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, platforms, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
            }
        } catch (DataAccessException e) {
            log.warn("Failed to cache platforms to Redis: {}", e.getMessage());
        }

        return platforms;
    }

    @Override
    public List<HotSearchDTO> getHotSearchesByPlatform(String platform, int limit) {
        String cacheKey = REDIS_KEY_PREFIX + platform;
        
        // 先尝试从Redis缓存获取
        try {
            @SuppressWarnings("unchecked")
            List<HotSearchDTO> cached = (List<HotSearchDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (DataAccessException e) {
            // Redis访问失败，降级到数据库查询
            log.warn("Redis access failed, falling back to database: {}", e.getMessage());
        }

        // 查询最近2小时内的热搜数据
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        List<HotSearch> entities = hotSearchMapper.findLatestByPlatform(
                platform, twoHoursAgo, limit);

        // 将实体转换为DTO
        List<HotSearchDTO> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 将查询结果写入Redis缓存
        try {
            if (!dtos.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, dtos, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
            }
        } catch (DataAccessException e) {
            log.warn("Failed to cache hot searches to Redis: {}", e.getMessage());
        }

        return dtos;
    }

    @Override
    public Map<String, List<HotSearchDTO>> getAllHotSearches(int limit) {
        List<String> platforms = getAllPlatforms();
        if (platforms.isEmpty()) {
            return Collections.emptyMap();
        }
        return platforms.stream()
                .collect(Collectors.toMap(
                        platform -> platform,
                        platform -> getHotSearchesByPlatform(platform, limit)
                ));
    }

    @Override
    public void refreshAllHotSearches() {
        try {
            List<String> platforms = getAllPlatforms();
            for (String platform : platforms) {
                redisTemplate.delete(REDIS_KEY_PREFIX + platform);
            }
            redisTemplate.delete(REDIS_KEY_PREFIX + "platforms");
            log.info("Cleared all hot search cache");
        } catch (DataAccessException e) {
            log.warn("Failed to clear cache: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveHotSearches(String platform, List<HotSearchDTO> hotSearches) {
        if (hotSearches == null || hotSearches.isEmpty()) {
            log.debug("No hot searches to save for platform: {}", platform);
            return;
        }

        int savedCount = 0;
        for (HotSearchDTO dto : hotSearches) {
            // 查找是否已存在相同平台和标题的记录（去重逻辑）
            HotSearch existing = hotSearchMapper.findByPlatformAndTitle(platform, dto.getTitle());
            
            HotSearch entity;
            if (existing != null) {
                // 更新现有记录（热度、排名等可能变化）
                existing.setUrl(dto.getUrl());
                existing.setHeatValue(dto.getHeatValue());
                existing.setCategory(dto.getCategory());
                existing.setRankNum(dto.getRankNum());
                existing.setIconUrl(dto.getIconUrl());
                existing.setDescription(dto.getDescription());
                hotSearchMapper.updateById(existing);
            } else {
                // 创建新记录
                entity = convertToEntity(dto, platform);
                hotSearchMapper.insert(entity);
            }
            
            savedCount++;
        }

        // 清除该平台在Redis中的缓存，下次查询时从数据库重新加载
        try {
            redisTemplate.delete(REDIS_KEY_PREFIX + platform);
        } catch (DataAccessException e) {
            log.warn("Failed to clear platform cache: {}", e.getMessage());
        }

        log.info("Saved/Updated {} hot searches for platform: {}", savedCount, platform);
    }

    private HotSearchDTO convertToDTO(HotSearch entity) {
        return HotSearchDTO.builder()
                .id(entity.getId())
                .platform(entity.getPlatform())
                .title(entity.getTitle())
                .url(entity.getUrl())
                .heatValue(entity.getHeatValue())
                .category(entity.getCategory())
                .rankNum(entity.getRankNum())
                .iconUrl(entity.getIconUrl())
                .description(entity.getDescription())
                .createdTime(entity.getCreatedTime())
                .build();
    }

    private HotSearch convertToEntity(HotSearchDTO dto, String platform) {
        return HotSearch.builder()
                .platform(platform)
                .title(dto.getTitle())
                .url(dto.getUrl())
                .heatValue(dto.getHeatValue())
                .category(dto.getCategory())
                .rankNum(dto.getRankNum())
                .iconUrl(dto.getIconUrl())
                .description(dto.getDescription())
                .build();
    }
}
