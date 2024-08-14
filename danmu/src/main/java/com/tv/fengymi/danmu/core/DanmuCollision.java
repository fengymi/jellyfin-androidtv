package com.tv.fengymi.danmu.core;

import android.graphics.Color;

import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.Danmu;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class DanmuCollision {
    private final DanmuConfigGetter danmuConfig;
    private final Random random = new Random();
    private final Danmu initDanmu = new Danmu("", 0L, Color.WHITE, 0, 0);
    /**
     * 行间隔
     */
    private static final int LINE_GAP = 10;

    private int width;
    private int height;

    /**
     * 记录最后弹幕的位置
     */
    private Danmu[] lastDanmu;

    public DanmuCollision(int height, int width, DanmuConfigGetter danmuConfig) {
        this.height = height;
        this.width = width;
        this.danmuConfig = danmuConfig;
        if (height == 0 || width == 0) {
            return;
        }

        reset(true, true);
    }

    /**
     * 重置计算器信息
     * @param resetLine 重置行数(涉及 位置、字体大小修改)
     * @param resetLastDanmu 重置最后的弹幕信息(涉及弹幕数据发生变化)
     */
    public void reset(boolean resetLine, boolean resetLastDanmu) {
        if (resetLastDanmu) {
            resetLastDanmus();
            return;
        }

        if (resetLine) {
            resetLines();
        }
    }

    /**
     * 计算弹幕坐标
     * @param resultDanmu 结果集
     * @param someDanmu 需要计算的弹幕集合
     * @param currentPlayTimestamp 当前播放时间
     */
    public void calculateDanmuXY(List<Danmu> resultDanmu, List<Danmu> someDanmu, long currentPlayTimestamp) {
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
     * 修改窗口大小
     * @param height 高度
     * @param width 宽度
     */
    public void changeWindows(int height, int width) {
        boolean needResetLines = this.height != height;
        this.height = height;
        this.width = width;
        if (needResetLines) {
            resetLines();
        }
    }

    /**
     * 重置弹幕行数和最后一行数据
     */
    private void resetLines() {
        // 上下各10 间隔10
        // x = (height - lineGap) / (fontSize + 10)
        int canUseHeight = height / danmuConfig.getPosition();
        int totalLines = (canUseHeight - LINE_GAP) / (danmuConfig.getFontSize() + LINE_GAP);
        int realLines = Math.max(totalLines, 0);
        // 填充最后一个弹幕信息
        if (this.lastDanmu == null) {
            Danmu[] lastAllDanmus = new Danmu[realLines];
            Arrays.fill(lastAllDanmus, initDanmu);
            this.lastDanmu = lastAllDanmus;
            return;
        }

        // 复制原始弹幕最后一个
        Danmu[] lastAllDanmus = new Danmu[realLines];
        int minSize = Math.min(realLines, lastDanmu.length);
        System.arraycopy(this.lastDanmu, 0, lastAllDanmus, 0, minSize);
        if (this.lastDanmu.length < realLines) {
            Arrays.fill(lastAllDanmus, this.lastDanmu.length, realLines, initDanmu);
        }
        this.lastDanmu = lastAllDanmus;
    }

    /**
     * 重新计算每行最后的弹幕
     */
    private void resetLastDanmus() {
        if (this.lastDanmu == null) {
            resetLines();
            return;
        }

        Arrays.fill(this.lastDanmu, initDanmu);
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

    /**
     * 根据当前弹幕信息计算最匹配的一行数据
     * @param lastLineDanmus 每行最后一个弹幕信息
     * @return 匹配的行
     */
    private int getMatchIndex(Danmu[] lastLineDanmus) {
        int startIndex = random.nextInt(lastLineDanmus.length);

        int minIndex = startIndex;
        for (int i = startIndex; i < lastLineDanmus.length + startIndex; i++) {
            int realIndex = i % lastLineDanmus.length;
            if (lastLineDanmus[realIndex].getLastX(danmuConfig.getFontSize()) < this.width) {
                minIndex = realIndex;
                break;
            }

            if (lastLineDanmus[realIndex].getLastX(danmuConfig.getFontSize()) < lastLineDanmus[minIndex].getLastX(danmuConfig.getFontSize())) {
                minIndex = realIndex;
            }
        }
        return minIndex;
    }

    public Danmu[] getLastDanmu() {
        return lastDanmu;
    }
}

