package com.hotsearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 配置前端应用访问后端API的跨域规则
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许携带凭证（如Cookie）
        config.setAllowCredentials(true);
        // 允许所有来源访问
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有HTTP方法（GET、POST等）
        config.addAllowedMethod("*");
        // 预检请求缓存时间（1小时）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用上述跨域配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
