package com.aura.player.controller;

import com.aura.player.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody JsonNode body) {
        String message = body.path("message").asText("");
        String mode = body.path("mode").asText("local");

        String historyContext = "";
        JsonNode history = body.path("history");
        if (history.isArray() && history.size() > 0) {
            StringBuilder sb = new StringBuilder("\n\n## 对话历史\n");
            int count = Math.min(history.size(), 16);
            for (int i = history.size() - count; i < history.size(); i++) {
                JsonNode msg = history.get(i);
                String role = msg.path("role").asText();
                String content = msg.path("content").asText();
                String label = "operator".equals(role) ? "用户" : "助手";
                sb.append(label).append(": ").append(content).append("\n");
            }
            sb.append("---\n");
            historyContext = sb.toString();
        }

        return chatService.chat(message, mode, historyContext)
                .map(chunk -> "data: " + chunk + "\n\n")
                .concatWith(Flux.just("data: [DONE]\n\n"));
    }
}
