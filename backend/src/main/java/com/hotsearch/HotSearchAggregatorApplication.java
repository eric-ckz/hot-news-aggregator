package com.hotsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 热搜聚合应用启动类
 * 用于聚合多个平台（微博、知乎、百度、B站、虎扑）的热搜数据
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务，用于定时抓取热搜数据
public class HotSearchAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotSearchAggregatorApplication.class, args);
    }
}
