package com.aura.player.controller;

import com.aura.player.model.Track;
import com.aura.player.service.TrackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TrackService trackService;

    public SearchController(TrackService trackService) {
        this.trackService = trackService;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int limit) {
        List<Track> results = trackService.search(q);
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        return Map.of("total", results.size(), "tracks", results);
    }
}
