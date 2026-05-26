package com.aura.player.service;

import com.aura.player.model.Track;
import com.aura.player.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TrackService {

    private final TrackRepository trackRepository;
    private final String musicDir;

    private static final Pattern BV_PATTERN = Pattern.compile("[_ ]BV([A-Za-z0-9]+)$");
    private static final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");

    public TrackService(TrackRepository trackRepository,
                        @Value("${app.music-dir}") String musicDir) {
        this.trackRepository = trackRepository;
        this.musicDir = musicDir;
    }

    public List<Track> scanAll() {
        List<Track> tracks = new ArrayList<>();
        Path root = Path.of(musicDir);
        if (!Files.exists(root)) return tracks;

        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(root, Files::isDirectory)) {
            for (Path dir : dirs) {
                String subDir = dir.getFileName().toString();
                tracks.addAll(scanDirectory(dir, subDir));
            }
        } catch (IOException e) {
            // ignore
        }
        return tracks;
    }

    public List<Track> scanSubDir(String subDir) {
        Path dir = Path.of(musicDir, subDir);
        if (!Files.exists(dir)) return List.of();
        return scanDirectory(dir, subDir);
    }

    private List<Track> scanDirectory(Path dir, String subDir) {
        List<Track> tracks = new ArrayList<>();
        String[] patterns = {"*.mp3", "*.m4a", "*.flac"};
        for (String pattern : patterns) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, pattern)) {
            for (Path file : files) {
                String filename = file.getFileName().toString();
                String baseName = filename.replaceAll("(?i)\\.(mp3|m4a|flac)$", "");
                ParsedName parsed = parseName(baseName);

                long size = 0;
                try { size = Files.size(file); } catch (IOException ignored) {}

                String id = subDir + "/" + filename;
                String url = "/api/tracks/stream?path=" + encodePath(subDir) + "/" + encodePath(filename);

                Track track = new Track(id, parsed.title, parsed.author, parsed.date,
                                        filename, subDir, size, url, parsed.bvid);
                tracks.add(track);
            }
        } catch (IOException ignored) {}
        }
        return tracks;
    }

    public List<Track> search(String query) {
        if (query == null || query.isBlank()) {
            return trackRepository.findAll();
        }
        return trackRepository.search(query);
    }

    public void syncToDb(List<Track> tracks) {
        for (Track t : tracks) {
            trackRepository.save(t);
        }
    }

    public String getMusicDir() {
        return musicDir;
    }

    public String resolveFilePath(String relativePath) {
        Path musicPath = Path.of(musicDir).toAbsolutePath().normalize();
        Path full = musicPath.resolve(relativePath).normalize();
        if (!full.startsWith(musicPath)) return null;
        return full.toString();
    }

    public ParsedName parseName(String name) {
        Matcher bvMatcher = BV_PATTERN.matcher(name);
        String bvid = "";
        if (bvMatcher.find()) {
            bvid = "BV" + bvMatcher.group(1);
            name = name.substring(0, bvMatcher.start());
        }

        String[] parts = name.split("-");
        int n = parts.length;

        if (n >= 4) {
            String y = parts[n - 3];
            String m = parts[n - 2];
            String d = parts[n - 1];
            if (isYear(y) && isNum(m) && isNum(d)) {
                String date = y + "-" + m + "-" + d;
                if (n >= 5) {
                    String title = String.join("-", Arrays.copyOfRange(parts, 0, n - 4)).trim();
                    String author = parts[n - 4].trim();
                    return new ParsedName(title, author, date, bvid);
                }
                String title = String.join("-", Arrays.copyOfRange(parts, 0, n - 3)).trim();
                return new ParsedName(title, "", date, bvid);
            }
        }

        return new ParsedName(name, "", "", bvid);
    }

    private boolean isYear(String s) {
        return YEAR_PATTERN.matcher(s).matches() && s.compareTo("1990") >= 0 && s.compareTo("2030") <= 0;
    }

    private boolean isNum(String s) {
        return s.matches("^\\d{1,2}$");
    }

    private String encodePath(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static class ParsedName {
        public final String title;
        public final String author;
        public final String date;
        public final String bvid;

        public ParsedName(String title, String author, String date, String bvid) {
            this.title = title;
            this.author = author;
            this.date = date;
            this.bvid = bvid;
        }
    }
}
