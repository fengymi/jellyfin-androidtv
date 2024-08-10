package com.tv.fengymi.danmu.model;

public class Danmu implements Comparable<Danmu> {
    private final String value;
    private long startTimestamp;
    private int color;
    private int x;
    private int y;

    private Danmu next;

    private int speedRate = 1;

    public Danmu(String value, long startTimestamp) {
        this(value, startTimestamp, 0xFFFFFFFF);
    }

    public Danmu(String value, long startTimestamp, int color) {
        this.value = value;
        this.startTimestamp = startTimestamp;
        this.color = color;
    }

    public Danmu(String value, long startTimestamp, int color, int x, int y) {
        this.value = value;
        this.startTimestamp = startTimestamp;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void setSpeedRate(int speedRate) {
        this.speedRate = speedRate;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDeath() {
        this.x = -10000;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setNext(Danmu next) {
        this.next = next;
    }

    public Danmu getNext() {
        return next;
    }

    public int getLastX(int fontSize) {
        return this.x + this.value.length() * fontSize;
    }
    public void destroy() {
        this.x = -10000;
    }

    public void update(int position) {
        this.x -= position;
    }

    @Override
    public int compareTo(Danmu o) {
        // 根据时间升序
        return Long.compare(this.startTimestamp, o.startTimestamp);
    }

    @Override
    public String toString() {
        return "Danmu{" +
                "value='" + value + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", color=" + color +
                ", x=" + x +
                ", y=" + y +
                ", next=" + next +
                ", speedRate=" + speedRate +
                '}';
    }
}
