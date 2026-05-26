package com.aura.player.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ToolExecutor toolExecutor;
    private final BiliService biliService;
    private final TrackService trackService;

    @Value("${app.ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${app.ai.api-key:sk-placeholder}")
    private String apiKey;

    @Value("${app.ai.model:deepseek-chat}")
    private String model;

    @Value("${app.music-dir:${user.dir}/../music}")
    private String musicDir;

    private static final List<Map<String, Object>> TOOLS = List.of(
        Map.of(
            "type", "function",
            "function", Map.of(
                "name", "bash",
                "description", "Execute a bash command and return its output. Use this to call curl for API searches, run ls/dir for file listing, or execute other shell commands.",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "command", Map.of(
                            "type", "string",
                            "description", "The bash command to execute"
                        )
                    ),
                    "required", List.of("command")
                )
            )
        ),
        Map.of(
            "type", "function",
            "function", Map.of(
                "name", "search_bili",
                "description", "Search Bilibili videos by keyword. Returns video list with bvid, title, author, duration. Always use this tool for B站搜索 instead of bash/curl.",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "keyword", Map.of(
                            "type", "string",
                            "description", "Search keyword in Chinese"
                        ),
                        "page", Map.of(
                            "type", "integer",
                            "description", "Page number, default 1"
                        )
                    ),
                    "required", List.of("keyword")
                )
            )
        ),
        Map.of(
            "type", "function",
            "function", Map.of(
                "name", "convert_bili",
                "description", "Download Bilibili video as audio file. Use this to convert B站视频 to audio. Do NOT use bash/npx bv2mp3.",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "bvid", Map.of(
                            "type", "string",
                            "description", "Bilibili video BV id, e.g. BV1xxxxx"
                        )
                    ),
                    "required", List.of("bvid")
                )
            )
        )
    );

    private static final String BASE_PROMPT = """
        你是 AuraMusic 的 AI 音频助手。保持简洁的中文终端风格语气。

        ## 重要限制
        - 搜索B站视频必须使用 search_bili 工具（不要用 bash/curl）
        - 文件操作、转换等其他命令使用 bash 工具
        - 严禁安装任何外部工具或依赖（如 pip install、npm install -g 等）
        - 遇到工具缺失或命令失败时，如实告知用户并停止操作
        """;

    private static final String LOCAL_PROMPT = BASE_PROMPT + """

        ## 本地模式

        本地音频文件已自动加载到播放列表中，用户可以直接点击播放。
        如需搜索和下载新音乐，请使用 search_bili 工具搜索B站。

        ### B站搜索

        使用 search_bili 工具搜索B站视频。

        ### 输出格式

        ```tracks
        [{"bvid":"BV1xxxxx","title":"视频标题","author":"UP主","duration":"4:32","url":"https://www.bilibili.com/video/BV1xxxxx"}]
        ```

        ### 下载转换

        当用户要求下载/转换视频时：
        1. 使用 convert_bili 工具：
           convert_bili(bvid="BVxxxxx")
        2. 用 added 代码块输出结果：
           ```added
           [{"id":"...","title":"...","author":"...","url":"...","bvid":"BV..."}]
           ```
        """;

    private static final String CLOUD_PROMPT = BASE_PROMPT + """

        ## B站云端搜索

        通过 B站搜索视频内容。

        ### 搜索步骤
        1. 解析用户意图，提取搜索关键词
        2. 使用 search_bili 工具搜索，传入 keyword 参数（不要用 bash/curl）
        3. 筛选最相关的视频，以 tracks 格式输出

        ### 输出格式

        ```tracks
        [{"bvid":"BV1xxxxx","title":"视频标题","author":"UP主","duration":"4:32","url":"https://www.bilibili.com/video/BV1xxxxx"}]
        ```

        ### 下载转换

        当用户要求下载/转换视频时：
        1. 使用 convert_bili 工具：
           convert_bili(bvid="BVxxxxx")
        2. 用 added 代码块输出结果：
           ```added
           [{"id":"...","title":"...","author":"...","url":"...","bvid":"BV..."}]
           ```
        """;

    public ChatService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper, ToolExecutor toolExecutor,
                       BiliService biliService, TrackService trackService) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.toolExecutor = toolExecutor;
        this.biliService = biliService;
        this.trackService = trackService;
    }

    public Flux<String> chat(String message, String mode, String historyContext) {
        String systemPrompt = "cloud".equals(mode) ? CLOUD_PROMPT : LOCAL_PROMPT;
        // Inject actual music directory path (use forward slashes for bash)
        String musicPath = musicDir.replace("\\", "/");
        systemPrompt = systemPrompt.replace("${MUSIC_DIR}", musicPath);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (historyContext != null && !historyContext.isBlank()) {
            messages.add(Map.of("role", "user", "content", historyContext));
            messages.add(Map.of("role", "assistant", "content", "好的，我已了解对话历史。"));
        }

        messages.add(Map.of("role", "user", "content", message));

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // Run the agent loop in a separate thread
        new Thread(() -> runAgentLoop(messages, sink)).start();

        return sink.asFlux();
    }

    private void runAgentLoop(List<Map<String, Object>> messages, Sinks.Many<String> sink) {
        try {
            for (int i = 0; i < 10; i++) { // max 10 tool call rounds
                Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages,
                    "tools", TOOLS,
                    "stream", false
                );

                // Call AI (non-streaming for simplicity with tool calls)
                String responseBody = webClient.post()
                        .uri(baseUrl + "/v1/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (responseBody == null) {
                    sink.tryEmitNext(toJson(Map.of("type", "error", "error", "Empty response from AI")));
                    break;
                }

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choice = root.path("choices").path(0);
                JsonNode messageNode = choice.path("message");

                // Check for tool calls
                JsonNode toolCalls = messageNode.path("tool_calls");
                if (toolCalls.isArray() && toolCalls.size() > 0) {
                    // Add assistant message with tool calls to history
                    Map<String, Object> assistantMsg = new LinkedHashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", messageNode.path("content").isTextual()
                            ? messageNode.path("content").asText() : null);

                    List<Map<String, Object>> tcList = new ArrayList<>();
                    for (JsonNode tc : toolCalls) {
                        Map<String, Object> tcMap = new LinkedHashMap<>();
                        tcMap.put("id", tc.path("id").asText());
                        tcMap.put("type", "function");
                        tcMap.put("function", Map.of(
                                "name", tc.path("function").path("name").asText(),
                                "arguments", tc.path("function").path("arguments").asText()
                        ));
                        tcList.add(tcMap);
                    }
                    assistantMsg.put("tool_calls", tcList);
                    messages.add(assistantMsg);

                    // Send tool call event
                    for (JsonNode tc : toolCalls) {
                        String funcName = tc.path("function").path("name").asText();
                        String args = tc.path("function").path("arguments").asText();

                        sink.tryEmitNext(toJson(Map.of(
                                "type", "tool_call",
                                "name", funcName,
                                "arguments", args
                        )));

                        // Execute tool
                        JsonNode argsNode = objectMapper.readTree(args);
                        ToolResult result;

                        switch (funcName) {
                            case "search_bili" -> {
                                String keyword = argsNode.path("keyword").asText("");
                                int page = argsNode.path("page").asInt(1);
                                try {
                                    var videos = biliService.searchVideos(keyword, page);
                                    String json = objectMapper.writeValueAsString(videos);
                                    result = new ToolResult(true, json, "");
                                } catch (Exception e) {
                                    result = new ToolResult(false, "", "B站搜索失败: " + e.getMessage());
                                }
                            }
                            case "convert_bili" -> {
                                String bvid = argsNode.path("bvid").asText("");
                                try {
                                    String path = biliService.downloadAudio(bvid, musicDir);
                                    // Scan the directory to register the track
                                    String subDir = path.substring(0, path.indexOf('/'));
                                    trackService.scanSubDir(subDir);
                                    // Return structured result for frontend to parse
                                    var tracks = trackService.search(bvid);
                                    String json = objectMapper.writeValueAsString(Map.of(
                                        "status", "ok",
                                        "path", path,
                                        "tracks", tracks
                                    ));
                                    result = new ToolResult(true, json, "");
                                } catch (Exception e) {
                                    result = new ToolResult(false, "", "下载失败: " + e.getMessage());
                                }
                            }
                            default -> {
                                String command = argsNode.path("command").asText("");
                                if (command.isEmpty()) command = args;
                                result = toolExecutor.executeBash(command);
                            }
                        }

                        // Send tool result event
                        sink.tryEmitNext(toJson(Map.of(
                                "type", "tool_result",
                                "name", funcName,
                                "output", result.toDisplay().substring(0, Math.min(result.toDisplay().length(), 4000))
                        )));

                        // Add tool result to messages
                        messages.add(Map.of(
                                "role", "tool",
                                "tool_call_id", tc.path("id").asText(),
                                "content", result.toDisplay().substring(0, Math.min(result.toDisplay().length(), 4000))
                        ));
                    }

                    // Continue the loop for AI to process tool results
                    continue;

                } else {
                    // No tool calls - this is the final text response
                    String content = messageNode.path("content").asText("");
                    if (!content.isBlank()) {
                        sink.tryEmitNext(toJson(Map.of(
                                "type", "assistant",
                                "message", Map.of("content", List.of(Map.of("type", "text", "text", content)))
                        )));
                    }
                    break;
                }
            }

            sink.tryEmitNext(toJson(Map.of("type", "result", "subtype", "success", "result", "")));
            sink.tryEmitComplete();

        } catch (Exception e) {
            sink.tryEmitNext(toJson(Map.of("type", "error", "error", e.getMessage())));
            sink.tryEmitComplete();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"json serialization failed\"}";
        }
    }
}
