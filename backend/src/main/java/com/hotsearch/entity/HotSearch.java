package com.hotsearch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hot_search", indexes = {
    @Index(name = "idx_platform", columnList = "platform"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_time", columnList = "createdTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", nullable = false, length = 50)
    private String platform;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "heat_value")
    private Long heatValue;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "rank_num")
    private Integer rankNum;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
