package com.hotsearch.dto;

import java.time.LocalDateTime;

/**
 * 热搜数据传输对象（DTO）
 * 用于在各层之间传递热搜数据，避免直接暴露实体类
 */
public class HotSearchDTO {

    private Long id;                    // 主键ID
    private String platform;            // 平台代码
    private String title;               // 热搜标题
    private String url;                 // 热搜链接
    private Long heatValue;             // 热度值
    private String category;            // 分类标签
    private Integer rankNum;            // 排名
    private String iconUrl;             // 图标URL
    private String description;         // 描述信息
    private LocalDateTime createdTime;  // 创建时间

    public HotSearchDTO() {
    }

    public HotSearchDTO(Long id, String platform, String title, String url, Long heatValue, String category, Integer rankNum, String iconUrl, String description, LocalDateTime createdTime) {
        this.id = id;
        this.platform = platform;
        this.title = title;
        this.url = url;
        this.heatValue = heatValue;
        this.category = category;
        this.rankNum = rankNum;
        this.iconUrl = iconUrl;
        this.description = description;
        this.createdTime = createdTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getHeatValue() {
        return heatValue;
    }

    public void setHeatValue(Long heatValue) {
        this.heatValue = heatValue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getRankNum() {
        return rankNum;
    }

    public void setRankNum(Integer rankNum) {
        this.rankNum = rankNum;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String platform;
        private String title;
        private String url;
        private Long heatValue;
        private String category;
        private Integer rankNum;
        private String iconUrl;
        private String description;
        private LocalDateTime createdTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder heatValue(Long heatValue) {
            this.heatValue = heatValue;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder rankNum(Integer rankNum) {
            this.rankNum = rankNum;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public HotSearchDTO build() {
            return new HotSearchDTO(id, platform, title, url, heatValue, category, rankNum, iconUrl, description, createdTime);
        }
    }
}
