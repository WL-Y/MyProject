package com.aura.player.model;

public class BiliVideo {
    private String bvid;
    private String title;
    private String author;
    private String duration;
    private long play;
    private String pic;

    public BiliVideo() {}

    public BiliVideo(String bvid, String title, String author, String duration, long play, String pic) {
        this.bvid = bvid;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.play = play;
        this.pic = pic;
    }

    public String getBvid() { return bvid; }
    public void setBvid(String bvid) { this.bvid = bvid; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public long getPlay() { return play; }
    public void setPlay(long play) { this.play = play; }
    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }
}
