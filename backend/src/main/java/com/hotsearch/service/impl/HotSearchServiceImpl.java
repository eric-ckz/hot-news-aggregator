package com.hotsearch.service.impl;

import com.hotsearch.dto.HotSearchDTO;
import com.hotsearch.entity.HotSearch;
import com.hotsearch.repository.HotSearchRepository;
import com.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotSearchServiceImpl implements HotSearchService {

    private final HotSearchRepository hotSearchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "hotsearch:";
    private static final long REDIS_EXPIRE_HOURS = 2;

    @Override
    public List<String> getAllPlatforms() {
        String cacheKey = REDIS_KEY_PREFIX + "platforms";
        
        try {
            @SuppressWarnings("unchecked")
            List<String> platforms = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (platforms != null && !platforms.isEmpty()) {
                return platforms;
            }
        } catch (DataAccessException e) {
            log.warn("Redis access failed, falling back to database: {}", e.getMessage());
        }

        List<String> platforms = hotSearchRepository.findAllPlatforms();
        
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
        
        try {
            @SuppressWarnings("unchecked")
            List<HotSearchDTO> cached = (List<HotSearchDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (DataAccessException e) {
            log.warn("Redis access failed, falling back to database: {}", e.getMessage());
        }

        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        List<HotSearch> entities = hotSearchRepository.findLatestByPlatform(
                platform, twoHoursAgo, PageRequest.of(0, limit));

        List<HotSearchDTO> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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

        List<HotSearch> entities = hotSearches.stream()
                .map(dto -> convertToEntity(dto, platform))
                .collect(Collectors.toList());

        hotSearchRepository.saveAll(entities);

        try {
            redisTemplate.delete(REDIS_KEY_PREFIX + platform);
        } catch (DataAccessException e) {
            log.warn("Failed to clear platform cache: {}", e.getMessage());
        }

        log.info("Saved {} hot searches for platform: {}", entities.size(), platform);
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
