-- Hot Search Aggregator Database Initialization Script
-- Database: hot_search
-- Charset: utf8mb4

-- Create database
CREATE DATABASE IF NOT EXISTS hot_search
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hot_search;

-- Modify existing table if description column is VARCHAR (migration)
-- 2026-03-14: Changed description from VARCHAR(2000) to TEXT to support longer content
ALTER TABLE hot_search
    MODIFY COLUMN description TEXT DEFAULT NULL COMMENT 'Description';

-- Create hot_search table
CREATE TABLE IF NOT EXISTS hot_search (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform VARCHAR(50) NOT NULL COMMENT 'Platform name',
    title VARCHAR(500) NOT NULL COMMENT 'Hot search title',
    url VARCHAR(1000) NOT NULL COMMENT 'Hot search URL',
    heat_value BIGINT DEFAULT NULL COMMENT 'Heat value',
    category VARCHAR(50) DEFAULT NULL COMMENT 'Category',
    rank_num INT DEFAULT NULL COMMENT 'Rank number',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT 'Icon URL',
    description TEXT DEFAULT NULL COMMENT 'Description',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_platform (platform),
    INDEX idx_category (category),
    INDEX idx_created_time (created_time),
    INDEX idx_platform_created (platform, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert test data - Weibo
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('weibo', 'Spring Festival Holiday', 'https://s.weibo.com/weibo?q=spring', 12500000, 'society', 1, 'Spring Festival holiday arrangement', NOW()),
('weibo', 'Movie Box Office Record', 'https://s.weibo.com/weibo?q=movie', 9800000, 'entertainment', 2, 'Movie box office hits record', NOW()),
('weibo', 'New Energy Vehicle Sales', 'https://s.weibo.com/weibo?q=car', 8500000, 'tech', 3, 'NEV sales growth', NOW()),
('weibo', 'Postgraduate Results', 'https://s.weibo.com/weibo?q=exam', 7200000, 'education', 4, 'Postgraduate exam results announced', NOW()),
('weibo', 'Travel Guide', 'https://s.weibo.com/weibo?q=travel', 6500000, 'travel', 5, 'Travel destination recommendations', NOW()),
('weibo', 'New Phone Launch', 'https://s.weibo.com/weibo?q=phone', 5800000, 'digital', 6, 'New flagship phone released', NOW()),
('weibo', 'TV Drama Finale', 'https://s.weibo.com/weibo?q=drama', 5200000, 'entertainment', 7, 'Popular TV drama finale', NOW()),
('weibo', 'Food Discovery', 'https://s.weibo.com/weibo?q=food', 4800000, 'food', 8, 'Popular restaurant recommendations', NOW()),
('weibo', 'Fitness Check-in', 'https://s.weibo.com/weibo?q=fitness', 4200000, 'health', 9, 'New Year fitness plan', NOW()),
('weibo', 'Cute Pets', 'https://s.weibo.com/weibo?q=pet', 3800000, 'pets', 10, 'Cute pet photos', NOW());

-- Insert test data - Zhihu
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('zhihu', 'How to evaluate new tech products?', 'https://www.zhihu.com/question/1', 850000, 'tech', 1, 'New product features and experience', NOW()),
('zhihu', 'Books worth reading in 2024', 'https://www.zhihu.com/question/2', 720000, 'reading', 2, 'Annual book recommendations', NOW()),
('zhihu', 'How can new employees grow fast?', 'https://www.zhihu.com/question/3', 680000, 'career', 3, 'Workplace experience sharing', NOW()),
('zhihu', 'Why are young people reluctant to marry?', 'https://www.zhihu.com/question/4', 650000, 'society', 4, 'Analysis of marriage concept changes', NOW()),
('zhihu', 'How to learn programming?', 'https://www.zhihu.com/question/5', 580000, 'programming', 5, 'Programming learning path', NOW()),
('zhihu', 'Tools to improve efficiency', 'https://www.zhihu.com/question/6', 520000, 'productivity', 6, 'Efficiency tools recommendations', NOW()),
('zhihu', 'How to maintain healthy lifestyle?', 'https://www.zhihu.com/question/7', 480000, 'health', 7, 'Healthy living habits', NOW()),
('zhihu', 'Investment and financial guide', 'https://www.zhihu.com/question/8', 450000, 'finance', 8, 'Basic investment knowledge', NOW()),
('zhihu', 'Interesting travel stories', 'https://www.zhihu.com/question/9', 380000, 'travel', 9, 'Unforgettable travel experiences', NOW()),
('zhihu', 'Food cooking tutorial', 'https://www.zhihu.com/question/10', 320000, 'food', 10, 'Home cooking recipes', NOW());

-- Insert test data - Bilibili
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('bilibili', 'New Year Special Program', 'https://www.bilibili.com/video/BV1', 5200000, 'general', 1, 'UP主', NOW()),
('bilibili', 'Game Strategy Guide', 'https://www.bilibili.com/video/BV2', 4800000, 'game', 2, 'Gaming expert', NOW()),
('bilibili', 'Tech Review New Product', 'https://www.bilibili.com/video/BV3', 4500000, 'tech', 3, 'Tech reviewer', NOW()),
('bilibili', 'Food Cooking Tutorial', 'https://www.bilibili.com/video/BV4', 4200000, 'food', 4, 'Food blogger', NOW()),
('bilibili', 'Study Tips Postgraduate', 'https://www.bilibili.com/video/BV5', 3800000, 'study', 5, 'Study expert', NOW()),
('bilibili', 'Funny Compilation Daily', 'https://www.bilibili.com/video/BV6', 3500000, 'funny', 6, 'Comedy channel', NOW()),
('bilibili', 'Music Recommendation', 'https://www.bilibili.com/video/BV7', 3200000, 'music', 7, 'Music channel', NOW()),
('bilibili', 'Animation Review Classic', 'https://www.bilibili.com/video/BV8', 2800000, 'anime', 8, 'Anime fan', NOW()),
('bilibili', 'Fitness Exercise Home', 'https://www.bilibili.com/video/BV9', 2500000, 'sports', 9, 'Fitness coach', NOW()),
('bilibili', 'Pet Daily Cute Moments', 'https://www.bilibili.com/video/BV10', 2200000, 'pets', 10, 'Pet owner', NOW());

-- Insert test data - Baidu
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('baidu', 'Weather Forecast', 'https://www.baidu.com/s?wd=weather', 12500000, 'life', 1, 'National weather forecast', NOW()),
('baidu', 'Stock Market', 'https://www.baidu.com/s?wd=stock', 9800000, 'finance', 2, 'Stock market updates', NOW()),
('baidu', 'Epidemic Latest', 'https://www.baidu.com/s?wd=epidemic', 8500000, 'health', 3, 'Epidemic prevention info', NOW()),
('baidu', 'Lottery Results', 'https://www.baidu.com/s?wd=lottery', 7200000, 'lottery', 4, 'Lottery winning numbers', NOW()),
('baidu', 'Exchange Rate', 'https://www.baidu.com/s?wd=exchange', 6500000, 'finance', 5, 'Real-time exchange rates', NOW()),
('baidu', 'Express Tracking', 'https://www.baidu.com/s?wd=express', 5800000, 'life', 6, 'Package tracking service', NOW()),
('baidu', 'Recipe Collection', 'https://www.baidu.com/s?wd=recipe', 5200000, 'food', 7, 'Home cooking recipes', NOW()),
('baidu', 'Horoscope', 'https://www.baidu.com/s?wd=horoscope', 4800000, 'entertainment', 8, 'Daily horoscope reading', NOW()),
('baidu', 'Calendar', 'https://www.baidu.com/s?wd=calendar', 4200000, 'tool', 9, 'Lunar calendar converter', NOW()),
('baidu', 'Translation', 'https://www.baidu.com/s?wd=translate', 3800000, 'tool', 10, 'Online translation service', NOW());

-- Insert test data - Hupu
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('hupu', 'NBA Today Lakers vs Warriors', 'https://bbs.hupu.com/1.html', 85000, 'NBA', 1, 'NBA zone', NOW()),
('hupu', 'CBA Playoffs Prediction', 'https://bbs.hupu.com/2.html', 72000, 'CBA', 2, 'CBA zone', NOW()),
('hupu', 'National Team Preview', 'https://bbs.hupu.com/3.html', 68000, 'football', 3, 'Football zone', NOW()),
('hupu', 'Street Discussion Today', 'https://bbs.hupu.com/4.html', 65000, 'street', 4, 'Street zone', NOW()),
('hupu', 'Gear Review New Shoes', 'https://bbs.hupu.com/5.html', 58000, 'gear', 5, 'Sports gear', NOW()),
('hupu', 'Fitness Check-in Today', 'https://bbs.hupu.com/6.html', 52000, 'fitness', 6, 'Fitness zone', NOW()),
('hupu', 'Game Discussion LOL', 'https://bbs.hupu.com/7.html', 48000, 'game', 7, 'Game zone', NOW()),
('hupu', 'Digital Review Phone', 'https://bbs.hupu.com/8.html', 45000, 'digital', 8, 'Digital zone', NOW()),
('hupu', 'Car Discussion New Release', 'https://bbs.hupu.com/9.html', 38000, 'car', 9, 'Car zone', NOW()),
('hupu', 'Movie Recommendation', 'https://bbs.hupu.com/10.html', 32000, 'movie', 10, 'Movie zone', NOW());

-- Verify data
SELECT platform, COUNT(*) as count FROM hot_search GROUP BY platform;
