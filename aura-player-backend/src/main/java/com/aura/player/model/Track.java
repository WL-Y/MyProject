package com.aura.player.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tracks")
public class Track {
    @Id
    private String id;
    private String title;
    private String author;
    private String date;
    private String filename;
    private String subDir;
    private long size;
    private String url;
    private String bvid;

    public Track() {}

    public Track(String id, String title, String author, String date,
                 String filename, String subDir, long size, String url, String bvid) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.date = date;
        this.filename = filename;
        this.subDir = subDir;
        this.size = size;
        this.url = url;
        this.bvid = bvid;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getSubDir() { return subDir; }
    public void setSubDir(String subDir) { this.subDir = subDir; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getBvid() { return bvid; }
    public void setBvid(String bvid) { this.bvid = bvid; }
}
