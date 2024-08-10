package com.tv.fengymi.danmu.model;

public enum PlayStateEnum {
    /**
     * 停止
     */
    STOP(0, "停止"),
    RUNNING(1, "运行"),
    PAUSE(2, "暂停"),
    INIT(-1, "初始化"),
    ;
    private final int state;
    private final String desc;

    PlayStateEnum(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    /**
     * 是否已停止
     * @return true/false
     */
    public boolean isStop() {
        return this == STOP;
    }

    /**
     * 是否暂停
     * @return true/false
     */
    public boolean isPause() {
        return this == PAUSE;
    }

    /**
     * 是否已停止
     * @return true/false
     */
    public boolean isStarted() {
        return this != STOP && this != INIT;
    }
}
