package com.tv.fengymi.danmu.core;

import com.tv.fengymi.danmu.model.Danmu;

import java.util.List;

public class DanmuProvider {
    private int maxNums;
    private int maxTimeGaps;
    private long currentTimes;

    private List<Danmu> allDanmus;

    public DanmuProvider(int maxNums, int maxTimeGaps) {
        this.maxNums = maxNums;
        this.maxTimeGaps = maxTimeGaps;
    }

    public void setCurrentTimes(long currentTimes) {
        this.currentTimes = currentTimes;
    }

    public void getCurrentDanmus() {

    }
}
