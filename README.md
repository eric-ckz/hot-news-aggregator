# 热搜聚合平台

一个实时聚合各大平台热门资讯的应用，支持微博、知乎、B站、百度、虎扑等平台的热搜数据抓取和展示。

## 技术栈

### 后端
- **Spring Boot 3.2+** - 主框架
- **Java 17** - 编程语言
- **Maven** - 构建工具
- **Spring Data JPA** - 数据持久化
- **Redis** - 缓存
- **MySQL** - 数据库
- **WebClient** - 异步HTTP客户端

### 前端
- **Vue 3** - 前端框架
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Element Plus** - UI组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理

## 项目结构

```
hot-search-aggregator/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/hotsearch/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # API控制器
│   │       ├── dto/         # 数据传输对象
│   │       ├── entity/      # 实体类
│   │       ├── repository/  # 数据访问层
│   │       ├── scheduler/   # 定时任务
│   │       ├── scraper/     # 爬虫实现
│   │       └── service/     # 业务逻辑层
│   ├── src/main/resources/
│   │   └── application.yml  # 配置文件
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── components/      # 组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── types/           # TypeScript类型
│   │   ├── views/           # 页面视图
│   │   ├── App.vue
│   │   └── main.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.ts
└── docker-compose.yml       # Docker编排
```

## 快速开始

### 方式一：使用 Docker Compose（推荐）

```bash
# 克隆项目
git clone <repository-url>
cd hot-search-aggregator

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

访问地址：
- 前端：http://localhost:3000
- 后端API：http://localhost:8080

### 方式二：本地开发

#### 后端启动

```bash
cd backend

# 安装依赖并编译
mvn clean install

# 启动应用（需要先启动MySQL和Redis）
mvn spring-boot:run
```

#### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问地址：
- 前端：http://localhost:3000
- 后端API：http://localhost:8080

## 配置说明

### 后端配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hot_search?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root

  # Redis配置
  redis:
    host: localhost
    port: 6379

# 爬虫配置
scraper:
  interval: 300000  # 抓取间隔（毫秒）
  timeout: 30       # 超时时间（秒）
```

## API 接口

### 热搜接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/hot-search/platforms` | GET | 获取所有平台列表 |
| `/api/hot-search/{platform}` | GET | 获取指定平台热搜 |
| `/api/hot-search/all` | GET | 获取所有平台热搜 |
| `/api/hot-search/refresh` | GET | 手动刷新热搜数据 |

## 支持的平台

- **微博** - 微博热搜榜
- **知乎** - 知乎热榜
- **B站** - 哔哩哔哩热门
- **百度** - 百度热搜
- **虎扑** - 虎扑热帖

## 开发计划

- [x] 项目初始化与架构设计
- [x] 后端基础框架搭建
- [x] 前端基础框架搭建
- [x] 数据模型设计
- [x] 爬虫服务实现
- [ ] 前端页面开发
- [ ] 用户认证与授权
- [ ] 数据持久化优化
- [ ] 部署与运维

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

[MIT](LICENSE)
