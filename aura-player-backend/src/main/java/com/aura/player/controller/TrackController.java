package com.aura.player.controller;

import com.aura.player.model.Track;
import com.aura.player.service.TrackService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping("/scan")
    public Map<String, Object> scan(@RequestParam(required = false) String subDir) {
        List<Track> tracks;
        if (subDir != null && !subDir.isBlank()) {
            tracks = trackService.scanSubDir(subDir);
        } else {
            tracks = trackService.scanAll();
        }
        trackService.syncToDb(tracks);
        return Map.of("tracks", tracks);
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> stream(@RequestParam String path) {
        String fullPath = trackService.resolveFilePath(path);
        if (fullPath == null) return ResponseEntity.notFound().build();

        File file = new File(fullPath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        String filename = file.getName();
        MediaType contentType = filename.endsWith(".m4a")
            ? MediaType.parseMediaType("audio/mp4")
            : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(new FileSystemResource(file));
    }
}
