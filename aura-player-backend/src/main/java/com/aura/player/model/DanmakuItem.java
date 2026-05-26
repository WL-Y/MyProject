package com.aura.player.model;

public class DanmakuItem {
    private double time;
    private String content;
    private int type;
    private String color;

    public DanmakuItem() {}

    public DanmakuItem(double time, String content, int type, String color) {
        this.time = time;
        this.content = content;
        this.type = type;
        this.color = color;
    }

    public double getTime() { return time; }
    public void setTime(double time) { this.time = time; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
