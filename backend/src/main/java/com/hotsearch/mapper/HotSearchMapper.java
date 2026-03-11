package com.hotsearch.mapper;

import com.hotsearch.entity.HotSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HotSearchMapper {

    @Select("SELECT DISTINCT platform FROM hot_search")
    List<String> findAllPlatforms();

    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND created_time >= #{startTime} ORDER BY rank_num ASC LIMIT #{limit}")
    List<HotSearch> findLatestByPlatform(@Param("platform") String platform, @Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND title = #{title}")
    HotSearch findByPlatformAndTitle(@Param("platform") String platform, @Param("title") String title);

    @Select("SELECT * FROM hot_search WHERE created_time >= #{startTime} ORDER BY heat_value DESC LIMIT #{limit}")
    List<HotSearch> findHotSearchesByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    @Select("SELECT * FROM hot_search WHERE platform = #{platform} ORDER BY rank_num ASC")
    List<HotSearch> findByPlatformOrderByRankNumAsc(@Param("platform") String platform);

    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND created_time > #{createdTime} ORDER BY rank_num ASC")
    List<HotSearch> findByPlatformAndCreatedTimeAfterOrderByRankNumAsc(@Param("platform") String platform, @Param("createdTime") LocalDateTime createdTime);

    @Insert("INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, icon_url, description, created_time, updated_time) VALUES (#{platform}, #{title}, #{url}, #{heatValue}, #{category}, #{rankNum}, #{iconUrl}, #{description}, NOW(), NOW())")
    int insert(HotSearch hotSearch);

    @Update("UPDATE hot_search SET url = #{url}, heat_value = #{heatValue}, category = #{category}, rank_num = #{rankNum}, icon_url = #{iconUrl}, description = #{description}, updated_time = NOW() WHERE id = #{id}")
    int updateById(HotSearch hotSearch);
}
