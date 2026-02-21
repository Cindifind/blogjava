# 多功能博客系统后端服务

一个基于Spring Boot构建的多功能博客系统后端服务，集成了用户认证、API管理、评论系统、音乐播放、B站直播间WebSocket连接等功能。

## 项目概述

本项目是一个综合性的博客系统后端，采用Spring Boot 3.x框架开发，提供了完整的RESTful API接口，支持用户注册登录、文章评论、API接口管理、网易云音乐搜索播放、B站直播间实时消息监听等功能。

## 技术栈

- **核心框架**: Spring Boot 3.5.5
- **JDK版本**: Java 17
- **数据库**: MySQL 8.2.0
- **持久层框架**: MyBatis 3.0.3
- **缓存**: Redis
- **WebSocket**: Spring WebSocket + Java-WebSocket
- **邮件服务**: Spring Mail
- **工具库**: Hutool 5.8.35, org.json
- **HTTP客户端**: Kong Unirest

## 项目结构

```
src/main/java/com/example/demo/
├── auth/                          # 用户认证模块
│   ├── mapper/UserInfoMapper.java # 用户信息数据访问
│   ├── model/UserInfo.java        # 用户实体类
│   ├── server/                    # 认证服务
│   └── util/GetSh256.java         # SHA256加密工具
├── bilibliWebsocket/              # B站WebSocket模块
│   ├── map/                       # 数据映射类
│   ├── sendmessage/               # 消息发送
│   ├── tool/                      # B站签名工具
│   ├── BilibiliClient.java        # B站客户端
│   ├── BilibiliWebSocketService.java # WebSocket服务
│   └── ...
├── config/                        # 配置类
│   ├── WebConfig.java             # Web配置
│   ├── WebSocketConfig.java       # WebSocket配置
│   └── *Interceptor.java          # 拦截器
├── controller/                    # 控制器层
│   ├── LoginController.java       # 登录接口
│   ├── RegisterController.java    # 注册接口
│   ├── ApiController.java         # API管理接口
│   ├── CommentController.java     # 评论接口
│   ├── MusicController.java       # 音乐接口
│   ├── BilibiliWsUrlController.java # B站WebSocket接口
│   └── ...
├── mapper/                        # MyBatis Mapper接口
├── model/                         # 实体类
├── music/                         # 音乐模块
│   ├── Search.java                # 音乐搜索
│   └── NeteaseMusicApp.java       # 网易云音乐API
├── server/                        # 服务层
└── util/                          # 工具类
```

## 核心功能模块

### 1. 用户认证模块

- **用户注册**: 邮箱验证码注册，支持QQ邮箱头像自动获取
- **用户登录**: SHA256加密Token验证
- **密码修改**: 安全的密码重置功能
- **权限控制**: 基于Token的权限验证，支持普通用户和管理员角色

**主要接口**:
- `POST /login` - 用户登录
- `POST /register` - 用户注册
- `POST /user/changePassword` - 修改密码
- `POST /sendCode` - 发送验证码

### 2. API管理模块

- **API统计**: 记录API调用次数和状态
- **API文档**: 管理API接口文档信息
- **批量查询**: 支持批量获取API信息

**主要接口**:
- `GET /apiStats` - 获取API统计
- `POST /admin/insertApiUrl` - 添加API
- `POST /admin/updateApiUrl` - 更新API信息
- `POST /apiList` - 批量获取API列表
- `GET /api/proxy-video` - 视频代理(支持B站视频)

### 3. 评论系统

- **多级评论**: 支持文章评论和回复评论
- **权限验证**: 仅允许评论作者删除自己的评论
- **实时统计**: 自动更新文章评论数

**主要接口**:
- `POST /user/addComment` - 添加评论
- `POST /user/addReply` - 添加回复
- `GET /article` - 获取文章评论
- `GET /articleReply` - 获取回复列表
- `GET /user/deleteComment` - 删除评论

### 4. 音乐播放模块

- **音乐搜索**: 集成网易云音乐API
- **音乐播放**: 获取音乐播放URL
- **歌词获取**: 支持歌词实时获取
- **API统计**: 自动统计音乐接口调用

**主要接口**:
- `GET /api/music` - 获取音乐列表
- `GET /api/musicSearch` - 搜索音乐
- `GET /api/getMusicUrl` - 获取音乐播放URL
- `GET /api/getLyric` - 获取歌词

### 5. B站直播间WebSocket

- **实时连接**: 通过WebSocket连接B站直播间
- **弹幕监听**: 实时接收直播间弹幕、礼物、进入房间等消息
- **多房间支持**: 支持同时监听多个直播间
- **自动重连**: 断线自动重连机制
- **心跳保活**: 定时发送心跳包维持连接

**连接方式**:
```
WebSocket连接: ws://your-server/bilibili
发送消息格式: connect:房间号:cookie
cookie可为空，系统会使用默认cookie
```

### 6. 其他功能

- **H5统计**: 记录H5页面访问数据
- **天气查询**: 集成天气API
- **文章资源管理**: 管理博客文章和资源路径
- **静态资源同步**: 支持多台服务器静态资源同步

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 数据库配置

在 `application.properties` 中配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://your-host:3306/blog
spring.datasource.username=root
spring.datasource.password=your-password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### Redis配置

```properties
spring.data.redis.host=your-redis-host
spring.data.redis.port=6379
spring.data.redis.password=your-password
```

### 邮件服务配置

```properties
spring.mail.host=smtp.qq.com
spring.mail.username=your-email@qq.com
spring.mail.password=your-auth-code
spring.mail.port=465
```

### 运行项目

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd demo

# 编译运行
./mvnw spring-boot:run

# 或者打包后运行
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## API文档

启动项目后，可通过以下方式查看API文档：

- 项目集成了自定义的 `@Client` 注解，用于API接口注册和管理
- 可通过 `/apiStats` 接口获取所有API列表

## 配置文件说明

### application.properties 主要配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `spring.datasource.url` | 数据库连接URL | - |
| `spring.data.redis.host` | Redis主机地址 | - |
| `spring.mail.host` | 邮件服务器地址 | smtp.qq.com |
| `mybatis.mapper-locations` | Mapper XML文件位置 | classpath:mapper/*.xml |

## 技术亮点

1. **模块化设计**: 各功能模块独立，便于维护和扩展
2. **WebSocket实时通信**: 支持B站直播间实时消息推送
3. **安全认证**: SHA256加密 + Token验证机制
4. **性能优化**: Redis缓存 + 数据库连接池
5. **代码规范**: 统一的RESTful API设计规范

## 相关文档

- [博客系统主要功能与代码实现.md](./博客系统主要功能与代码实现.md)
- [博客评论系统设计与实现.md](./博客评论系统设计与实现.md)
- [连接b站直播间实现.md](./连接b站直播间实现.md)
- [静态资源同步到多台服务器.md](./静态资源同步到多台服务器.md)

## 贡献指南

欢迎提交Issue和Pull Request来改进项目。

## 许可证

本项目仅供学习和参考使用。

---

**作者**: [您的姓名]  
**联系方式**: 1269838578@qq.com  
**项目地址**: [您的项目仓库地址]
