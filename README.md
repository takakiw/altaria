# altaria-space

项目使用了 JDK 17 和主流的微服务技术栈，包括 Redis、MySQL、MinIO、Nacos、Seata 和 RabbitMQ。

## 环境要求

在启动项目之前，请确保您的开发环境满足以下条件：

- 安装了 JDK 17
- 安装了 Maven
- 安装了 Docker 和 Docker Compose

## 快速启动

### 1. 拉取代码

将项目克隆到本地计算机， 并进入项目目录


### 2. 使用 Maven 构建项目

在项目根目录下运行以下命令，完成 Maven 构建：

```bash
mvn clean install -Dmaven.test.skip=true
```

### 3. 使用 Docker Compose 启动项目

在构建完成后，运行以下命令启动服务：

#### 3.1 初次使用:
```bash
docker-compose up -d --build
```
此命令将拉取必要的镜像、构建服务并以后台模式启动。

#### 3.2 本地已经构建好镜像 
```bash
docker-compose -f docker-compose-porduction.yml up -d
```
此命令将直接使用本地镜像在后台启动服务。

### 4. 检查服务状态

您可以通过以下命令检查容器运行状态：

```bash
docker-compose ps
```

确保所有容器状态为 `Up`。

### 5. 访问服务

根据项目中的配置，您可以通过以下入口访问主要服务：

- **网关地址**: `http://localhost:10086`
- **Nacos 控制台**: `http://localhost:28848/nacos`
- **minio 控制台**: `http://localhost:29090`
- **minio 控制台**: `http://localhost:7091`
- **rabbitmq 控制台**: `http://localhost:35672`
- **其他服务**: 请参考项目文档。

## 技术栈
- **Spring Boot**: 微服务应用的核心框架
- **Spring Cloud**: 微服务架构解决方案
- **Spring Cloud Alibaba**: 与 Spring Cloud 集成的 Alibaba 微服务解决方案（包括 Nacos、Seata 等）
- **JDK**: 17
- **Maven**: 项目管理和构建工具
- **Redis**: 缓存
- **MySQL**: 数据库
- **Nacos**: 服务注册与配置中心
- **MinIO**: 对象存储
- **RabbitMQ**: 消息队列
- **Seata**: 分布式事务管理

## 注意事项

- 请根据需要修改 `.env` 文件中的环境变量。
- 若为生产环境，请使用 `docker-compose-production.yml` 并替换命令中的配置文件：

```bash
docker-compose -f docker-compose-production.yml up -d --build
```
