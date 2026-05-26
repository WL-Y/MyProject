# AuraPlayer - B站 AI 音频播放器

基于 Java Spring Boot + Vue 3 的 AI 驱动 B站音频播放器，支持自然语言搜索、下载B站视频为音频、弹幕同步播放。

## 功能

- **AI 对话操控** — 自然语言让 AI 搜索B站视频、下载音频、管理播放列表
- **B站音频下载** — 直接调用 B站 API 下载视频音频轨（m4a 格式），无需外部工具
- **终端赛博朋克 UI** — CRT 扫描线、霓虹辉光、数据雨、弹幕滚动等复古未来主义视觉
- **弹幕同步** — 播放 B站下载的音频时，同步显示原视频弹幕
- **本地曲库** — 启动时自动加载已下载音频，点击即播

## 技术栈

| 端 | 技术 |
|---|------|
| **前端** | Vue 3 + TypeScript + Vite + Tailwind CSS + Pinia |
| **后端** | Spring Boot 3.4 + Java 21 + Maven |
| **数据库** | SQLite (JPA/Hibernate) |
| **AI** | DeepSeek API (兼容 OpenAI function calling) |
| **API** | SSE 流式聊天、B站 WBI 签名、弹幕 XML 解析 |

## 项目结构

```
aura-player-backend/          # Spring Boot 后端
├── src/main/java/com/aura/player/
│   ├── config/CorsConfig.java
│   ├── controller/
│   │   ├── BiliController.java      # B站搜索/下载/弹幕 API
│   │   ├── ChatController.java      # SSE 流式聊天
│   │   ├── SearchController.java    # 本地曲库搜索
│   │   └── TrackController.java     # 音频扫描与流媒体
│   ├── model/
│   │   ├── BiliVideo.java
│   │   ├── DanmakuItem.java
│   │   └── Track.java
│   ├── repository/TrackRepository.java
│   └── service/
│       ├── BiliService.java         # B站 API (WBI签名/下载/弹幕)
│       ├── ChatService.java         # AI 对话与工具调用
│       ├── ToolExecutor.java        # Bash 工具执行器
│       └── TrackService.java        # 音频文件扫描/流媒体
│
aura-player-ui/               # Vue 3 前端
├── src/
│   ├── api/                   # 后端 API 封装
│   ├── components/
│   │   ├── atoms/             # 基础组件 (Logo/Badge/GlowDot/ProgressBar)
│   │   ├── molecules/         # 组合组件 (ChatMessage/ControlBar/SeekBar)
│   │   └── organisms/         # 页面区块 (Player/Playlist/AgentChat/ClockPanel)
│   ├── composables/           # Vue composables (useClock)
│   ├── stores/                # Pinia 状态管理 (player/agent/danmaku)
│   ├── styles/globals.css     # 赛博朋克主题 + CRT 特效
│   └── types/index.ts         # TypeScript 类型定义
```

## 快速开始

### 环境要求

- Java 21+
- Node.js 20+
- Maven 3.8+

### 1. 启动后端

```bash
cd aura-player-backend

# 设置 DeepSeek API Key (环境变量)
# Windows: set AI_API_KEY=sk-xxxxx
# Linux/macOS: export AI_API_KEY=sk-xxxxx

./mvnw spring-boot:run
# 后端启动在 http://localhost:8080
```

### 2. 启动前端

```bash
cd aura-player-ui

npm install
npm run dev
# 前端启动在 http://localhost:5173
```

### 3. 使用

1. 浏览器打开 `http://localhost:5173`
2. 在聊天框输入搜索指令，例如"搜索周杰伦 晴天"
3. 点击曲目卡片上的 **+ ADD** 按钮下载音频
4. 下载完成后自动加入播放列表，点击播放

## AI 工具

AI 助手可使用以下工具：

| 工具 | 功能 |
|------|------|
| `search_bili` | 搜索 B站视频，返回 bvid/标题/UP主/时长 |
| `convert_bili` | 下载 B站视频音频轨为 m4a 文件 |
| `bash` | 执行系统命令（文件操作等） |

## 配置

`application.yml` 中的关键配置：

```yaml
app:
  music-dir: ${MUSIC_DIR:${user.dir}/../music}    # 音频下载目录
  ai:
    base-url: ${AI_BASE_URL:https://api.deepseek.com}
    api-key: ${AI_API_KEY:}                        # 通过环境变量设置
    model: deepseek-chat
```

## 弹幕

播放已下载的 B站音频时，点击状态栏的 **DANMAKU_OFF** 开启弹幕。弹幕会随音频播放进度同步滚动显示，颜色保持B站原始弹幕颜色。

## License

MIT