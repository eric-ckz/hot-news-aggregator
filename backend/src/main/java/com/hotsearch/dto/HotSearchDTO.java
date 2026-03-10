package com.hotsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotSearchDTO {

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
}
