package com.hotsearch.mapper;

import com.hotsearch.entity.HotSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 热搜数据访问接口
 * 使用MyBatis注解方式实现数据库操作
 */
@Mapper
public interface HotSearchMapper {

    /**
     * 查询所有平台列表（去重）
     */
    @Select("SELECT DISTINCT platform FROM hot_search")
    List<String> findAllPlatforms();

    /**
     * 查询指定平台在指定时间之后的热搜数据，按排名升序
     */
    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND created_time >= #{startTime} ORDER BY rank_num ASC LIMIT #{limit}")
    @Results(id = "hotSearchMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "platform", column = "platform"),
            @Result(property = "title", column = "title"),
            @Result(property = "url", column = "url"),
            @Result(property = "heatValue", column = "heat_value"),
            @Result(property = "category", column = "category"),
            @Result(property = "rankNum", column = "rank_num"),
            @Result(property = "iconUrl", column = "icon_url"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdTime", column = "created_time"),
            @Result(property = "updatedTime", column = "updated_time")
    })
    List<HotSearch> findLatestByPlatform(@Param("platform") String platform, @Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    /**
     * 根据平台和标题查询热搜记录（用于去重判断）
     */
    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND title = #{title}")
    @ResultMap("hotSearchMap")
    HotSearch findByPlatformAndTitle(@Param("platform") String platform, @Param("title") String title);

    /**
     * 查询指定时间之后的热搜数据，按热度降序
     */
    @Select("SELECT * FROM hot_search WHERE created_time >= #{startTime} ORDER BY heat_value DESC LIMIT #{limit}")
    @ResultMap("hotSearchMap")
    List<HotSearch> findHotSearchesByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("limit") Integer limit);

    /**
     * 查询指定平台的所有热搜，按排名升序
     */
    @Select("SELECT * FROM hot_search WHERE platform = #{platform} ORDER BY rank_num ASC")
    @ResultMap("hotSearchMap")
    List<HotSearch> findByPlatformOrderByRankNumAsc(@Param("platform") String platform);

    /**
     * 查询指定平台在指定时间之后的热搜，按排名升序
     */
    @Select("SELECT * FROM hot_search WHERE platform = #{platform} AND created_time > #{createdTime} ORDER BY rank_num ASC")
    @ResultMap("hotSearchMap")
    List<HotSearch> findByPlatformAndCreatedTimeAfterOrderByRankNumAsc(@Param("platform") String platform, @Param("createdTime") LocalDateTime createdTime);

    /**
     * 插入新的热搜记录
     */
    @Insert("INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, icon_url, description, created_time, updated_time) VALUES (#{platform}, #{title}, #{url}, #{heatValue}, #{category}, #{rankNum}, #{iconUrl}, #{description}, NOW(), NOW())")
    int insert(HotSearch hotSearch);

    /**
     * 更新热搜记录
     */
    @Update("UPDATE hot_search SET url = #{url}, heat_value = #{heatValue}, category = #{category}, rank_num = #{rankNum}, icon_url = #{iconUrl}, description = #{description}, updated_time = NOW() WHERE id = #{id}")
    int updateById(HotSearch hotSearch);
}
