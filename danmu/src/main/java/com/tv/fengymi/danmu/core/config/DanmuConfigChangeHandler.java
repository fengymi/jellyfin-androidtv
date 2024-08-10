package com.tv.fengymi.danmu.core.config;

public interface DanmuConfigChangeHandler {

    /**
     * 设置字体大小
     *
     * @param fontSize 字体带下
     */
    default void setFontSize(int fontSize) {
    }

    /**
     * 设置速度
     *
     * @param speed 速度
     */
    default void setSpeed(int speed) {
    }

    /**
     * 设置位置
     * 上屏 - 3
     * 半屏 - 2
     * 全屏 - 1
     *
     * @param position 位置
     */
    default void setPosition(int position) {
    }

    /**
     * 是否开启
     *
     * @param open 是否开启
     */
    default void setOpen(boolean open) {
    }

    /**
     * 开启 debug
     *
     * @param debug 开启debug
     */
    default void setDebug(boolean debug) {
    }
}
