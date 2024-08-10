package com.tv.fengymi.danmu.core;

import android.graphics.Color;

import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.Danmu;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class DanmuCollision {
    private DanmuConfigGetter danmuConfig;
    private final Random random = new Random();
    private final Danmu initDanmu = new Danmu("", 0L, Color.WHITE, 0, 0);
    /**
     * 行间隔
     */
    private static final int LINE_GAP = 10;

    private int width;
    private int height;

    private Danmu[] lastDanmu;


    public DanmuCollision(int height, int width, DanmuConfigGetter danmuConfig) {
        this.height = height;
        this.width = width;
        this.danmuConfig = danmuConfig;
        if (height == 0 || width == 0) {
            return;
        }

        init(false);
    }

    public void reset(boolean resetLines, boolean resetLastDanmus) {
        if (resetLines || this.lastDanmu == null) {
            init(resetLastDanmus);
            return;
        }

        resetLastDanmus(this.lastDanmu.length, resetLastDanmus);
    }

    protected void resetLastDanmus(int size, boolean forceReset) {
//        DanmuSettingUtils.log("重新计算屏幕位置 size=" + size + ", forceReset=" + forceReset);
        Danmu[] lastAllDanmus = new Danmu[size];
        if (this.lastDanmu == null || forceReset) {
            Arrays.fill(lastAllDanmus, initDanmu);
            this.lastDanmu = lastAllDanmus;
            return;
        }

        int minSize = Math.min(size, lastDanmu.length);
        System.arraycopy(this.lastDanmu, 0, lastAllDanmus, 0, minSize);
        if (this.lastDanmu.length < size) {
            Arrays.fill(lastAllDanmus, this.lastDanmu.length, size, initDanmu);
        }
        this.lastDanmu = lastAllDanmus;
    }

    protected void init(boolean forceReset) {
        // 上下各10 间隔10
        // x = (height - lineGap) / (fontSize + 10)
        int canUseHeight = height / danmuConfig.getPosition();
        int totalLines = (canUseHeight - LINE_GAP) / (danmuConfig.getFontSize() + LINE_GAP);
        if (totalLines <= 0) {
            return;
        }

        resetLastDanmus(totalLines, forceReset);
    }

    public void resetDanmuXY(List<Danmu> danmus, int originWidth, long currentPlayTimestamp) {
        Danmu[] lineLastTime = this.lastDanmu;
        for (Danmu danmu : danmus) {
            initDanmuXY(lineLastTime, danmu, true, originWidth, currentPlayTimestamp);
        }
    }

    public void convertAndInit(List<Danmu> resultDanmu, List<Danmu> someDanmu, long currentPlayTimestamp) {
        if (someDanmu == null || someDanmu.isEmpty()) {
            return;
        }

        Danmu[] lastDanmu = this.lastDanmu;
        for (Danmu danmu : someDanmu) {
            initDanmuXY(lastDanmu, danmu, false, width, currentPlayTimestamp);
            resultDanmu.add(danmu);
        }
    }

    /**
     * 计算弹幕出现的位置
     * @param danmu 弹幕信息
     */
    public void initDanmuXY(Danmu[] lastDanmus, Danmu danmu, boolean reset, int originWidth, long currentPlayTimestamp) {
        int index = getMatchIndex(lastDanmus);
        danmu.setY((LINE_GAP + danmuConfig.getFontSize()) * (index + 1));

        int x;
        if (reset) {
            x = (int) (((double) danmu.getX()) / originWidth * width);
        } else {
            int random = (this.random.nextInt(5) + 5) * 10;
            Danmu originDanmu = lastDanmus[index];
            int lastX = originDanmu.getLastX(danmuConfig.getFontSize());
            int gapTime = (int) (danmu.getStartTimestamp() - currentPlayTimestamp);

            int theoryX = this.width + Math.max((gapTime / danmuConfig.getFps()) * danmuConfig.getSpeed(), 0);
            x = Math.max(lastX + random, theoryX);
            if (initDanmu != originDanmu) {
                originDanmu.setNext(danmu);
            }
        }
        danmu.setX(x);
        lastDanmu[index] = danmu;
        if (danmuConfig.isDebug()) {
            Timber.d("计算开始位置: index=" + index + ", x=" + x + ", lx=" + lastDanmus[index].getLastX(danmuConfig.getFontSize()) + ", current=" + lastDanmus[index].getStartTimestamp() + ", width=" + width + ", currentPlayTimestamp=" + currentPlayTimestamp + ", content=" + danmu.getValue());
        }
    }

    private int getMatchIndex(Danmu[] lineLastTime) {
        int startIndex = random.nextInt(lineLastTime.length);

        int minIndex = startIndex;
        for (int i = startIndex; i < lineLastTime.length + startIndex; i++) {
            int realIndex = i % lineLastTime.length;
            if (lineLastTime[realIndex].getLastX(danmuConfig.getFontSize()) < this.width) {
                minIndex = realIndex;
                break;
            }

            if (lineLastTime[realIndex].getLastX(danmuConfig.getFontSize()) < lineLastTime[minIndex].getLastX(danmuConfig.getFontSize())) {
                minIndex = realIndex;
            }
        }
        return minIndex;
    }

    public Danmu[] getLastDanmu() {
        return lastDanmu;
    }


    public boolean changeWindows(int height, int width, boolean needInit) {
        boolean needChange = this.height != height || this.width != width;
        this.height = height;
        this.width = width;
        init(false);
        return needChange;
    }
}

