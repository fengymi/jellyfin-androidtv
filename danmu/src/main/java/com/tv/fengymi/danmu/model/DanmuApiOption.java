package com.tv.fengymi.danmu.model;

public class DanmuApiOption {
    /**
     * 弹幕来源id
     */
    private String source;

    /**
     * 弹幕来源名称
     */
    private String sourceName;

    /**
     * 是否开启
     */
    private boolean opened;

    public DanmuApiOption() {

    }

    public DanmuApiOption(String source, String sourceName, boolean opened) {
        this.source = source;
        this.sourceName = sourceName;
        this.opened = opened;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public String toString() {
        return "DanmuApiOption{" +
                "source='" + source + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", opened=" + opened +
                '}';
    }
}
