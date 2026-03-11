package com.hotsearch.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient配置类
 * 配置HTTP客户端，用于抓取各平台热搜数据
 * 支持连接超时、读取超时、写入超时配置
 */
@Configuration
public class WebClientConfig {

    // 从配置文件读取超时时间，默认30秒
    @Value("${scraper.timeout:30}")
    private int timeout;

    @Bean
    public WebClient webClient() {
        // 创建HTTP客户端，配置超时参数
        HttpClient httpClient = HttpClient.create()
                // 连接超时时间：5秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 响应超时时间
                .responseTimeout(Duration.ofSeconds(timeout))
                // 配置读写超时处理器
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // 设置最大内存缓冲区为10MB，用于处理大响应
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
