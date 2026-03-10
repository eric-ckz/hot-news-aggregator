-- 热搜聚合平台数据库初始化脚本
-- 数据库: hot_search
-- 字符集: utf8mb4

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hot_search
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hot_search;

-- 创建热搜数据表
CREATE TABLE IF NOT EXISTS hot_search (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称 (weibo/zhihu/bilibili/baidu/hupu)',
    title VARCHAR(500) NOT NULL COMMENT '热搜标题',
    url VARCHAR(1000) NOT NULL COMMENT '热搜链接',
    heat_value BIGINT DEFAULT NULL COMMENT '热度值',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类标签',
    rank_num INT DEFAULT NULL COMMENT '排名',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '图标/图片URL',
    description VARCHAR(2000) DEFAULT NULL COMMENT '描述/摘要',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_platform (platform) COMMENT '平台索引',
    INDEX idx_category (category) COMMENT '分类索引',
    INDEX idx_created_time (created_time) COMMENT '创建时间索引',
    INDEX idx_platform_created (platform, created_time) COMMENT '平台和创建时间联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜数据表';

-- 插入初始测试数据 - 微博热搜
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('weibo', '春节放假安排', 'https://s.weibo.com/weibo?q=春节放假安排', 12500000, '社会', 1, '2024年春节放假安排公布', NOW()),
('weibo', '电影票房破纪录', 'https://s.weibo.com/weibo?q=电影票房破纪录', 9800000, '娱乐', 2, '春节档电影票房创新高', NOW()),
('weibo', '新能源汽车销量', 'https://s.weibo.com/weibo?q=新能源汽车销量', 8500000, '科技', 3, '新能源汽车销量大幅增长', NOW()),
('weibo', '考研成绩公布', 'https://s.weibo.com/weibo?q=考研成绩公布', 7200000, '教育', 4, '各地考研成绩陆续公布', NOW()),
('weibo', '旅游攻略推荐', 'https://s.weibo.com/weibo?q=旅游攻略推荐', 6500000, '旅游', 5, '春节旅游热门目的地推荐', NOW()),
('weibo', '新手机发布', 'https://s.weibo.com/weibo?q=新手机发布', 5800000, '数码', 6, '新款旗舰手机正式发布', NOW()),
('weibo', '电视剧大结局', 'https://s.weibo.com/weibo?q=电视剧大结局', 5200000, '娱乐', 7, '热门电视剧迎来大结局', NOW()),
('weibo', '美食探店', 'https://s.weibo.com/weibo?q=美食探店', 4800000, '美食', 8, '网红美食店打卡推荐', NOW()),
('weibo', '健身打卡', 'https://s.weibo.com/weibo?q=健身打卡', 4200000, '健康', 9, '新年健身计划开始', NOW()),
('weibo', '宠物萌照', 'https://s.weibo.com/weibo?q=宠物萌照', 3800000, '萌宠', 10, '可爱宠物照片分享', NOW());

-- 插入初始测试数据 - 知乎热榜
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('zhihu', '如何评价最新发布的科技产品？', 'https://www.zhihu.com/question/123456', 850000, '科技', 1, '新产品功能详解与使用体验', NOW()),
('zhihu', '2024年有哪些值得读的书？', 'https://www.zhihu.com/question/123457', 720000, '读书', 2, '年度书单推荐与阅读心得', NOW()),
('zhihu', '职场新人如何快速成长？', 'https://www.zhihu.com/question/123458', 680000, '职场', 3, '职场经验分享与建议', NOW()),
('zhihu', '为什么现在的年轻人不愿意结婚了？', 'https://www.zhihu.com/question/123459', 650000, '社会', 4, '婚姻观念变化的深层原因分析', NOW()),
('zhihu', '如何学习编程？', 'https://www.zhihu.com/question/123460', 580000, '编程', 5, '编程学习路径与资源推荐', NOW()),
('zhihu', '有哪些提升效率的工具？', 'https://www.zhihu.com/question/123461', 520000, '效率', 6, '高效工具推荐与使用技巧', NOW()),
('zhihu', '如何保持健康的生活方式？', 'https://www.zhihu.com/question/123462', 480000, '健康', 7, '健康生活的好习惯养成', NOW()),
('zhihu', '投资理财入门指南', 'https://www.zhihu.com/question/123463', 450000, '理财', 8, '理财基础知识与投资建议', NOW()),
('zhihu', '旅行中的趣事分享', 'https://www.zhihu.com/question/123464', 380000, '旅行', 9, '旅途中的难忘经历', NOW()),
('zhihu', '美食制作教程', 'https://www.zhihu.com/question/123465', 320000, '美食', 10, '家常菜制作步骤详解', NOW());

-- 插入初始测试数据 - B站热门
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('bilibili', '【官方】新年特别节目', 'https://www.bilibili.com/video/BV1xx411c7mD', 5200000, '综合', 1, 'UP主', NOW()),
('bilibili', '游戏攻略：通关秘籍', 'https://www.bilibili.com/video/BV1xx411c7mE', 4800000, '游戏', 2, '游戏达人', NOW()),
('bilibili', '科技评测：新品体验', 'https://www.bilibili.com/video/BV1xx411c7mF', 4500000, '科技', 3, '科技君', NOW()),
('bilibili', '美食制作：家常菜教程', 'https://www.bilibili.com/video/BV1xx411c7mG', 4200000, '美食', 4, '美食博主', NOW()),
('bilibili', '学习干货：考研经验分享', 'https://www.bilibili.com/video/BV1xx411c7mH', 3800000, '学习', 5, '学霸君', NOW()),
('bilibili', '搞笑合集：每日一笑', 'https://www.bilibili.com/video/BV1xx411c7mI', 3500000, '搞笑', 6, '搞笑菌', NOW()),
('bilibili', '音乐推荐：热门歌曲', 'https://www.bilibili.com/video/BV1xx411c7mJ', 3200000, '音乐', 7, '音乐台', NOW()),
('bilibili', '动画解说：经典回顾', 'https://www.bilibili.com/video/BV1xx411c7mK', 2800000, '动画', 8, '动画党', NOW()),
('bilibili', '运动健身：居家锻炼', 'https://www.bilibili.com/video/BV1xx411c7mL', 2500000, '运动', 9, '健身教练', NOW()),
('bilibili', '宠物日常：萌宠时刻', 'https://www.bilibili.com/video/BV1xx411c7mM', 2200000, '萌宠', 10, '铲屎官', NOW());

-- 插入初始测试数据 - 百度热搜
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('baidu', '天气预报', 'https://www.baidu.com/s?wd=天气预报', 12500000, '民生', 1, '全国天气预报查询', NOW()),
('baidu', '股票行情', 'https://www.baidu.com/s?wd=股票行情', 9800000, '财经', 2, '股市最新动态', NOW()),
('baidu', '疫情最新情况', 'https://www.baidu.com/s?wd=疫情最新情况', 8500000, '健康', 3, '疫情防控最新信息', NOW()),
('baidu', '彩票开奖结果', 'https://www.baidu.com/s?wd=彩票开奖结果', 7200000, '彩票', 4, '双色球大乐透开奖', NOW()),
('baidu', '汇率换算', 'https://www.baidu.com/s?wd=汇率换算', 6500000, '财经', 5, '实时汇率查询', NOW()),
('baidu', '快递查询', 'https://www.baidu.com/s?wd=快递查询', 5800000, '生活', 6, '快递物流跟踪', NOW()),
('baidu', '菜谱大全', 'https://www.baidu.com/s?wd=菜谱大全', 5200000, '美食', 7, '家常菜做法大全', NOW()),
('baidu', '星座运势', 'https://www.baidu.com/s?wd=星座运势', 4800000, '娱乐', 8, '今日星座运势查询', NOW()),
('baidu', '万年历', 'https://www.baidu.com/s?wd=万年历', 4200000, '工具', 9, '农历黄历查询', NOW()),
('baidu', '翻译', 'https://www.baidu.com/s?wd=翻译', 3800000, '工具', 10, '在线翻译服务', NOW());

-- 插入初始测试数据 - 虎扑热帖
INSERT INTO hot_search (platform, title, url, heat_value, category, rank_num, description, created_time) VALUES
('hupu', 'NBA今日战报：湖人vs勇士', 'https://bbs.hupu.com/1.html', 85000, 'NBA', 1, 'NBA专区', NOW()),
('hupu', 'CBA季后赛预测', 'https://bbs.hupu.com/2.html', 72000, 'CBA', 2, 'CBA专区', NOW()),
('hpu', '国足世预赛前瞻', 'https://bbs.hupu.com/3.html', 68000, '足球', 3, '足球专区', NOW()),
('hupu', '步行街：今日话题讨论', 'https://bbs.hupu.com/4.html', 65000, '步行街', 4, '步行街', NOW()),
('hupu', '装备测评：新款球鞋', 'https://bbs.hupu.com/5.html', 58000, '装备', 5, '运动装备', NOW()),
('hupu', '健身打卡：今日训练', 'https://bbs.hupu.com/6.html', 52000, '健身', 6, '健身交流', NOW()),
('hupu', '游戏讨论：英雄联盟', 'https://bbs.hupu.com/7.html', 48000, '游戏', 7, '游戏专区', NOW()),
('hupu', '数码评测：手机对比', 'https://bbs.hupu.com/8.html', 45000, '数码', 8, '数码专区', NOW()),
('hupu', '汽车讨论：新车发布', 'https://bbs.hupu.com/9.html', 38000, '汽车', 9, '汽车专区', NOW()),
('hupu', '影视推荐：近期好剧', 'https://bbs.hupu.com/10.html', 32000, '影视', 10, '影视交流', NOW());

-- 查看插入的数据
SELECT platform, COUNT(*) as count FROM hot_search GROUP BY platform;

-- 查询每个平台的前5条数据
SELECT platform, title, heat_value, rank_num 
FROM hot_search 
WHERE rank_num <= 5 
ORDER BY platform, rank_num;
