package com.hotsearch.repository;

import com.hotsearch.entity.HotSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotSearchRepository extends JpaRepository<HotSearch, Long> {

    List<HotSearch> findByPlatformOrderByRankNumAsc(String platform);

    List<HotSearch> findByPlatformAndCreatedTimeAfterOrderByRankNumAsc(String platform, LocalDateTime createdTime);

    @Query("SELECT h FROM HotSearch h WHERE h.platform = :platform AND h.createdTime >= :startTime ORDER BY h.rankNum ASC")
    List<HotSearch> findLatestByPlatform(@Param("platform") String platform, @Param("startTime") LocalDateTime startTime, Pageable pageable);

    Optional<HotSearch> findByPlatformAndTitle(String platform, String title);

    @Query("SELECT DISTINCT h.platform FROM HotSearch h")
    List<String> findAllPlatforms();

    @Query("SELECT h FROM HotSearch h WHERE h.createdTime >= :startTime ORDER BY h.heatValue DESC")
    List<HotSearch> findHotSearchesByTimeRange(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    void deleteByCreatedTimeBefore(LocalDateTime createdTime);
}
