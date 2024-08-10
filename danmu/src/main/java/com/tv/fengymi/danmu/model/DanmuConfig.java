package com.tv.fengymi.danmu.model;

import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DanmuConfig implements DanmuConfigGetter, DanmuConfigChangeHandler {
    private List<DanmuApiOption> danmuApiList;

    /**
     * 是否打开
     */
    private boolean open = true;

    /**
     * 字体大小
     */
    private int fontSize = 30;

    /**
     * 弹幕位置
     * 3-上屏, 2-半屏, 1-全屏
     */
    private int position = 3;

    private int speed = 8;

    private int fps = 30;

    /**
     * debug模式
     */
    private boolean debug;

    public DanmuConfig() {
    }

    public List<DanmuApiOption> getDanmuApiList() {
        return danmuApiList;
    }

    public void setDanmuApiList(List<DanmuApiOption> danmuApiList) {
        this.danmuApiList = danmuApiList;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public Set<String> getAllOpenSites() {
//            if (this.getDanmuApiList() == null || this.getDanmuApiList().isEmpty()) {
//                return Collections.emptySet();
//            }
//            Set<String> openSites = new HashSet<>(this.getDanmuApiList().size() << 1);
//            for (DanmuApiOption danmuApiOption : this.getDanmuApiList()) {
//                if (danmuApiOption.isOpened()) {
//                    openSites.add(danmuApiOption.getSource());
//                }
//            }
//            return openSites;
        return new HashSet<>(Arrays.asList("优酷", "爱奇艺"));
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isOpen() {
        return open;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getPositionIntValue() {
        return position;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
