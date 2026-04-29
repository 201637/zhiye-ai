# zhiye-ai-agent

## 项目简介
`zhiye-ai-agent` 是一个基于 **Spring Boot + Spring AI Alibaba + Vue 3** 的 AI 求职辅助平台，包含两个主要应用场景：

1. **AI 职业大师**：面向求职咨询场景，支持多轮对话、职业问题分析、面试建议等。
2. **AI 超级智能体**：具备工具调用能力，可结合网页搜索、网页抓取、文件操作、资源下载等工具完成更复杂的任务。

该项目由 **Java 后端** 和 **Vue 前端** 两部分组成，前后端分离，前端默认运行在 `3000` 端口，后端默认运行在 `8123` 端口，并以 `/api` 作为接口前缀。

## 功能概览
### 后端能力
- 基于 DashScope / 通义模型的 AI 对话
- 支持 SSE 流式输出
- 支持多轮对话记忆（按 `chatId` 区分会话）
- 提供职业顾问型 AI 应用
- 提供具备工具调用能力的超级智能体
- 集成 Swagger / OpenAPI 文档
- 支持本地文件式聊天记忆持久化

### 智能体工具能力
项目中已实现以下工具类：
- 文件读写工具 `FileOperationTool`
- 网页搜索工具 `WebSearchTool`
- 网页抓取工具 `WebScrapingTool`
- 资源下载工具 `ResourceDownloadTool`
- 终止工具 `TerminateTool`
- PDF 生成工具 `PDFGenerationTool`（代码已实现，当前默认未加入工具注册列表）

### 前端能力
- 平台首页
- AI 职业大师聊天页
- AI 超级智能体聊天页
- SSE 实时消息展示
- 基于 Vue Router 的单页应用路由
- 适配开发与生产环境 API 地址

## 技术栈
### 后端
- Java 21
- Spring Boot 3.4.4
- Spring AI Alibaba
- DashScope SDK
- Reactor / SSE
- Lombok
- Jsoup
- Hutool
- Knife4j / OpenAPI

### 前端
- Vue 3
- Vite
- Vue Router
- Axios

## 项目结构
```text
zhiye-ai-agent/
├─ pom.xml
├─ src/main/java/com/example/zhiyeaiagent/
│  ├─ advisor/                  # 自定义 Advisor
│  ├─ agent/                    # 智能体基类与超级智能体实现
│  ├─ app/                      # AI 应用封装
│  ├─ chatmemory/               # 文件式聊天记忆
│  ├─ config/                   # 例如跨域配置
│  ├─ constant/                 # 常量定义
│  ├─ controller/               # 接口控制器
│  └─ tools/                    # 工具调用实现
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-local.yml
│  └─ document/
├─ tmp/                         # 运行过程中生成的缓存、会话与文件
└─ zhiye-ai-agent-frontend/     # Vue 前端项目
   ├─ src/
   │  ├─ api/
   │  ├─ components/
   │  ├─ router/
   │  └─ views/
   ├─ Dockerfile
   ├─ nginx.conf
   └─ package.json
```

## 核心后端接口
控制器位于 `src/main/java/com/example/zhiyeaiagent/controller/AiController.java`。

### 1. 职业大师同步接口
```http
GET /api/ai/loveapp/chat/sync
```
参数：
- `message`：用户消息
- `chatId`：会话 ID

### 2. 职业大师 SSE 流式接口
```http
GET /api/ai/love_app/chat/sse
```
参数：
- `message`
- `chatId`

### 3. 职业大师 ServerSentEvent 接口
```http
GET /api/ai/love_app/chat/server_sent_event
```

### 4. 职业大师 SseEmitter 接口
```http
GET /api/ai/love_app/chat/sse_emitter
```

### 5. 超级智能体流式接口
```http
GET /api/ai/manus/chat
```
参数：
- `message`：用户输入

## 运行环境
建议环境：
- JDK 21
- Maven 3.9+
- Node.js 18+（建议 20）
- npm

## 配置说明
后端配置文件：
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`

默认配置要点：
- 服务端口：`8123`
- 接口前缀：`/api`
- Spring Profile：`local`

你至少需要根据自己的环境补充或调整以下配置：
- `spring.ai.dashscope.api-key`
- `spring.ai.dashscope.chat.options.model`
- `search-api.api-key`

> 建议将密钥改为本地环境变量或单独的私有配置文件管理，不要在公开仓库中直接暴露。

## 后端启动方式
在后端根目录执行：

```bash
./mvnw spring-boot:run
```

Windows：

```bash
mvnw.cmd spring-boot:run
```

启动后默认访问：
- 接口根地址：`http://localhost:8123/api`
- Swagger 文档：`http://localhost:8123/api/swagger-ui.html`

## 前端启动方式
进入前端目录：

```bash
cd zhiye-ai-agent-frontend
npm install
npm run dev
```

默认访问：
- 前端页面：`http://localhost:3000/`

前端开发环境下会请求：
- `http://localhost:8123/api`

## 前后端联调说明
- 前端开发端口在 `vite.config.js` 中配置为 `3000`
- 后端接口端口在 `application.yml` 中配置为 `8123`
- 前端 `src/api/index.js` 会根据环境自动切换 API 基地址
- 该端口设计与另一个 Django 项目的 `/myApp/ai/` 跳转地址保持一致，因此两个项目可以联动使用

## 会话记忆与临时文件
项目会在运行目录下生成：
- `tmp/chat-memory/`：多轮会话记忆
- `tmp/file/`：文件工具写入内容
- `tmp/download/`：下载工具保存资源
- `tmp/pdf/`：PDF 生成结果（如启用）

## 生产部署说明
前端目录提供了：
- `Dockerfile`
- `nginx.conf`

可用于构建前端静态资源镜像并通过 Nginx 托管。生产环境下：
- 前端静态文件由 Nginx 提供
- `/api/` 请求通过反向代理转发到后端服务

部署前建议检查并修改：
- `zhiye-ai-agent-frontend/nginx.conf` 中的 `proxy_pass`
- 生产域名
- 后端真实部署地址

## 项目亮点
- AI 求职问答与通用智能体双场景结合
- 支持工具调用，具备一定自主规划能力
- 支持 SSE 实时输出，聊天体验更自然
- 前后端分离，便于独立开发与部署
- 可以和招聘数据可视化项目组合，形成“数据分析 + AI 咨询”完整展示链路

## 已知注意事项
- 当前使用的模型与搜索服务依赖外部 API Key
- 超级智能体使用的工具列表在 `ToolRegistration.java` 中集中注册
- `PDFGenerationTool` 虽已实现，但默认未注册到 `allTools`
- 生产代理配置需要按实际部署环境修改，不建议直接使用默认值

## 适用场景
- 求职咨询类 AI 应用原型
- 大模型工具调用实践
- SSE 流式聊天项目示例
- 前后端分离 AI Web 应用开发
