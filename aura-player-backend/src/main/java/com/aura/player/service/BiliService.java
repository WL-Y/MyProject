package com.aura.player.service;

import com.aura.player.model.BiliVideo;
import com.aura.player.model.DanmakuItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;
import java.util.zip.GZIPInputStream;

@Service
public class BiliService {

    private static final int[] MIXIN_KEY_ENC_TAB = {
        46,47,18,2,53,8,23,32,15,50,10,31,58,3,45,35,27,43,5,49,33,9,42,19,29,28,14,39,12,38,41,13,37,48,7,16,24,55,40,61,26,17,0,1,60,51,30,4,22,25,54,21,56,59,6,63,57,62,11,36,20,34,44,52
    };

    private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    private String cachedImgKey;
    private String cachedSubKey;
    private long cachedKeysTs;
    private String cachedBuvid3;

    private static final long KEY_TTL = 12 * 60 * 60 * 1000L;

    public BiliService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<BiliVideo> searchVideos(String keyword, int page) throws Exception {
        var keys = getWbiKeys();
        String mixinKey = getMixinKey(keys[0], keys[1]);
        String buvid3 = getBuvid3();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("search_type", "video");
        params.put("keyword", keyword);
        params.put("page", String.valueOf(page));
        params.put("order", "totalrank");

        Map<String, String> signed = signParams(params, mixinKey);
        String qs = buildQueryString(signed);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.bilibili.com/x/web-interface/search/type?" + qs))
                .header("User-Agent", UA)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Origin", "https://www.bilibili.com")
                .header("Referer", "https://www.bilibili.com/")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-site")
                .header("Cookie", "buvid3=" + buvid3)
                .GET()
                .build();

        HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
        String body = new String(resp.body(), StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(body);

        if (root.path("code").asInt() != 0) {
            return List.of();
        }

        JsonNode results = root.path("data").path("result");
        List<BiliVideo> videos = new ArrayList<>();
        if (results.isArray()) {
            for (JsonNode v : results) {
                if (v.path("bvid").isMissingNode()) continue;
                videos.add(new BiliVideo(
                    v.path("bvid").asText(),
                    "", // title will be fetched from video info API
                    v.path("author").asText(""),
                    v.path("duration").asText(""),
                    v.path("play").asLong(0),
                    fixPicUrl(v.path("pic").asText(""))
                ));
            }
        }

        // Fetch clean titles from video info API in parallel
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(videos.size(), 5));
        List<Future<?>> futures = new ArrayList<>();
        for (BiliVideo video : videos) {
            futures.add(executor.submit(() -> {
                try {
                    String[] info = getVideoInfo(video.getBvid());
                    video.setTitle(stripHtml(info[1]));
                } catch (Exception e) {
                    // Keep empty title on failure
                }
            }));
        }
        for (Future<?> f : futures) {
            try { f.get(5, TimeUnit.SECONDS); } catch (Exception ignored) {}
        }
        executor.shutdown();

        return videos;
    }

    public String[] getVideoInfo(String bvid) throws Exception {
        var keys = getWbiKeys();
        String mixinKey = getMixinKey(keys[0], keys[1]);
        String buvid3 = getBuvid3();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("bvid", bvid);

        Map<String, String> signed = signParams(params, mixinKey);
        String qs = buildQueryString(signed);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.bilibili.com/x/web-interface/view?" + qs))
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .header("Origin", "https://www.bilibili.com")
                .header("Referer", "https://www.bilibili.com/")
                .header("Cookie", "buvid3=" + buvid3)
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());

        if (root.path("code").asInt() != 0 || root.path("data").path("cid").isMissingNode()) {
            throw new RuntimeException("Failed to get video info for " + bvid);
        }

        return new String[]{
            String.valueOf(root.path("data").path("cid").asLong()),
            stripHtml(root.path("data").path("title").asText(""))
        };
    }

    public String getAudioUrl(String bvid, String cid) throws Exception {
        String buvid3 = getBuvid3();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("bvid", bvid);
        params.put("cid", cid);
        params.put("fnval", "16"); // audio only

        var keys = getWbiKeys();
        String mixinKey = getMixinKey(keys[0], keys[1]);
        Map<String, String> signed = signParams(params, mixinKey);
        String qs = buildQueryString(signed);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.bilibili.com/x/player/playurl?" + qs))
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .header("Origin", "https://www.bilibili.com")
                .header("Referer", "https://www.bilibili.com/video/" + bvid)
                .header("Cookie", "buvid3=" + buvid3)
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());

        if (root.path("code").asInt() != 0) {
            throw new RuntimeException("Failed to get audio URL: " + root.path("message").asText());
        }

        JsonNode dash = root.path("data").path("dash");
        if (dash.isMissingNode() || dash.path("audio").isMissingNode() || !dash.path("audio").isArray() || dash.path("audio").isEmpty()) {
            throw new RuntimeException("No audio stream available for " + bvid);
        }

        // Get the first (best quality) audio stream
        return dash.path("audio").path(0).path("baseUrl").asText();
    }

    public String downloadAudio(String bvid, String musicDir) throws Exception {
        String[] info = getVideoInfo(bvid);
        String cid = info[0];
        String title = info[1]; // Always use the real title from API

        String audioUrl = getAudioUrl(bvid, cid);
        String buvid3 = getBuvid3();

        // Create output directory
        String dateDir = java.time.LocalDate.now().toString().replace("-", "");
        Path dir = Path.of(musicDir, dateDir);
        Files.createDirectories(dir);

        // Clean filename - only remove chars problematic for filesystems
        String cleanTitle = title.replaceAll("[\\\\/:*?\"<>|]", "_")
                                 .replaceAll("\\s+", " ").trim();
        String filename = cleanTitle + "_" + bvid + ".m4a";
        Path outputPath = dir.resolve(filename);

        // Download audio
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(audioUrl))
                .header("User-Agent", UA)
                .header("Accept", "*/*")
                .header("Origin", "https://www.bilibili.com")
                .header("Referer", "https://www.bilibili.com/video/" + bvid)
                .header("Cookie", "buvid3=" + buvid3)
                .GET()
                .build();

        HttpResponse<InputStream> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Download failed with HTTP " + resp.statusCode());
        }

        try (InputStream in = resp.body();
             OutputStream out = Files.newOutputStream(outputPath)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }

        return dateDir + "/" + filename;
    }

    public List<DanmakuItem> getDanmaku(String bvid) throws Exception {
        String[] info = getVideoInfo(bvid);
        String cid = info[0];
        String buvid3 = getBuvid3();

        // Use WebClient for auto-decompression
        String xml = webClient.get()
                .uri("https://api.bilibili.com/x/v1/dm/list.so?oid=" + cid)
                .header("User-Agent", UA)
                .header("Accept-Encoding", "gzip, deflate")
                .header("Origin", "https://www.bilibili.com")
                .header("Referer", "https://www.bilibili.com/")
                .header("Cookie", "buvid3=" + buvid3)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (xml == null || xml.isBlank()) return List.of();

        List<DanmakuItem> items = new ArrayList<>();
        Pattern pattern = Pattern.compile("<d p=\"([^\"]*)\"[^>]*>([^<]*)</d>");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            String[] attrs = matcher.group(1).split(",");
            double time = Double.parseDouble(attrs[0]);
            int type = Integer.parseInt(attrs[1]);
            String color = attrs.length > 3
                ? "#" + String.format("%06x", Integer.parseInt(attrs[3]))
                : "#ffffff";
            String content = matcher.group(2).trim();
            if (!content.isEmpty()) {
                items.add(new DanmakuItem(time, content, type, color));
            }
        }
        items.sort(Comparator.comparingDouble(DanmakuItem::getTime));
        return items;
    }

    private String[] getWbiKeys() throws Exception {
        if (cachedImgKey != null && System.currentTimeMillis() - cachedKeysTs < KEY_TTL) {
            return new String[]{cachedImgKey, cachedSubKey};
        }

        String buvid3 = getBuvid3();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.bilibili.com/x/web-interface/nav"))
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .header("Cookie", "buvid3=" + buvid3)
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(resp.body());
        JsonNode wbi = root.path("data").path("wbi_img");

        String imgUrl = wbi.path("img_url").asText("");
        String subUrl = wbi.path("sub_url").asText("");
        cachedImgKey = extractKey(imgUrl);
        cachedSubKey = extractKey(subUrl);
        cachedKeysTs = System.currentTimeMillis();

        return new String[]{cachedImgKey, cachedSubKey};
    }

    public synchronized String getBuvid3() throws Exception {
        if (cachedBuvid3 != null) return cachedBuvid3;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.bilibili.com"))
                .header("User-Agent", UA)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
        List<String> cookies = resp.headers().allValues("set-cookie");
        for (String c : cookies) {
            Matcher m = Pattern.compile("buvid3=([^;]+)").matcher(c);
            if (m.find()) {
                cachedBuvid3 = m.group(1);
                return cachedBuvid3;
            }
        }
        cachedBuvid3 = UUID.randomUUID() + "infoc";
        return cachedBuvid3;
    }

    private String getMixinKey(String imgKey, String subKey) {
        String raw = imgKey + subKey;
        StringBuilder sb = new StringBuilder();
        for (int i : MIXIN_KEY_ENC_TAB) {
            if (i < raw.length()) sb.append(raw.charAt(i));
        }
        return sb.substring(0, Math.min(32, sb.length()));
    }

    private Map<String, String> signParams(Map<String, String> params, String mixinKey) throws Exception {
        long wts = System.currentTimeMillis() / 1000;
        Map<String, String> signed = new TreeMap<>(params);
        signed.put("wts", String.valueOf(wts));

        StringBuilder query = new StringBuilder();
        for (var entry : signed.entrySet()) {
            if (query.length() > 0) query.append("&");
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                 .append("=")
                 .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        MessageDigest md = MessageDigest.getInstance("MD5");
        String wRid = bytesToHex(md.digest((query.toString() + mixinKey).getBytes(StandardCharsets.UTF_8)));
        signed.put("w_rid", wRid);
        return signed;
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
              .append("=")
              .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String extractKey(String url) {
        String file = url.substring(url.lastIndexOf('/') + 1);
        return file.replace(".png", "");
    }

    private String stripHtml(String s) {
        // Remove HTML tags
        s = s.replaceAll("<[^>]*>", "");
        // Remove replacement characters and non-printable chars (keep CJK, ASCII, common punctuation)
        s = s.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\xFFFE\\xFFFF]", "");
        // Remove trailing garbled chars (U+FFFD replacement char and symbols)
        s = s.replaceAll("[\\uFFFD\\u2588\\u2606\\u2610\\u2611\\u2612]+$", "");
        // Clean up trailing dashes, underscores, pipes
        s = s.replaceAll("[\\-_\\|]+$", "");
        return s.trim();
    }

    private String fixPicUrl(String pic) {
        if (pic.startsWith("//")) return "https:" + pic;
        return pic;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] decompressGzip(byte[] data) {
        if (data == null || data.length < 2) return data;
        // Check for gzip magic bytes
        if (data[0] == (byte)0x1f && data[1] == (byte)0x8b) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                 GZIPInputStream gis = new GZIPInputStream(bis);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = gis.read(buf)) != -1) bos.write(buf, 0, n);
                return bos.toByteArray();
            } catch (IOException e) {
                return data;
            }
        }
        // Try deflate (raw)
        try {
            Inflater inflater = new Inflater(true);
            inflater.setInput(data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            while (!inflater.finished()) {
                int n = inflater.inflate(buf);
                bos.write(buf, 0, n);
            }
            inflater.end();
            return bos.toByteArray();
        } catch (Exception e) {
            return data;
        }
    }
}
