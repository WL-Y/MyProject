package com.aura.player.controller;

import com.aura.player.model.BiliVideo;
import com.aura.player.model.DanmakuItem;
import com.aura.player.service.BiliService;
import com.aura.player.service.TrackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bili")
public class BiliController {

    private final BiliService biliService;
    private final TrackService trackService;
    private final String musicDir;
    private final WebClient webClient;

    public BiliController(BiliService biliService, TrackService trackService,
                          @Value("${app.music-dir}") String musicDir,
                          WebClient webClient) {
        this.biliService = biliService;
        this.trackService = trackService;
        this.musicDir = musicDir;
        this.webClient = webClient;
    }


    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page) {
        try {
            List<BiliVideo> videos = biliService.searchVideos(keyword, page);
            return Map.of("total", videos.size(), "videos", videos);
        } catch (Exception e) {
            return Map.of("total", 0, "videos", List.of(), "error", e.getMessage());
        }
    }

    @PostMapping("/search")
    public Map<String, Object> searchPost(@RequestBody Map<String, Object> body) {
        try {
            String keyword = (String) body.getOrDefault("keyword", "");
            int page = (int) body.getOrDefault("page", 1);
            List<BiliVideo> videos = biliService.searchVideos(keyword, page);
            return Map.of("total", videos.size(), "videos", videos);
        } catch (Exception e) {
            return Map.of("total", 0, "videos", List.of(), "error", e.getMessage());
        }
    }

    @GetMapping("/video")
    public Map<String, Object> video(@RequestParam String bvid) {
        try {
            String[] info = biliService.getVideoInfo(bvid);
            return Map.of("cid", info[0], "title", info[1]);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/danmaku")
    public Map<String, Object> danmaku(@RequestParam String bvid) {
        try {
            List<DanmakuItem> items = biliService.getDanmaku(bvid);
            return Map.of("danmaku", items, "count", items.size());
        } catch (Exception e) {
            return Map.of("danmaku", List.of(), "error", e.getMessage(), "count", 0);
        }
    }

    @PostMapping("/download")
    public Map<String, Object> download(@RequestBody Map<String, Object> body) {
        try {
            String bvid = (String) body.getOrDefault("bvid", "");
            String relativePath = biliService.downloadAudio(bvid, musicDir);

            // Scan and return the track info
            String subDir = relativePath.substring(0, relativePath.indexOf('/'));
            trackService.scanSubDir(subDir);

            return Map.of("status", "ok", "path", relativePath);
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }
}
