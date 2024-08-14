package com.tv.fengymi.danmu.core;

import com.tv.fengymi.danmu.model.Danmu;

import java.util.List;

public interface DanmuControllerHandler {
    /**
     * 修改当前播放位置
     * @param playPosition 位置
     */
    void currentPosition(long playPosition);

    /**
     * 恢复
     */
    void restore();

    /**
     * 暂停
     */
    void pause();

    /**
     * 跳跃播放进度
     * @param currentPlayProcess 当前播放进度
     */
    void skipPlayProcess(long currentPlayProcess);

    void error();

    /**
     * 终止
     */
    void stop();

    /**
     * 清除当前全部弹幕
     */
    void clearAll();

    /**
     * 添加弹幕
     * @param danmus 弹幕信息
     */
    default void addDanmus(List<Danmu> danmus) {}

    /**
     * 添加弹幕
     * @param danmu 弹幕信息
     */
    default void addDanmu(Danmu danmu) {}

    /**
     * 重新设置弹幕信息
     * @param danmus 弹幕信息
     */
    void resetAllDanmus(List<Danmu> danmus);

    /**
     * 更新全部弹幕信息
     * @param danmus 弹幕信息
     * @param playPosition 当前播放位置
     */
    void resetAllDanmus(List<Danmu> danmus, long playPosition);

    /**
     * 修改播放速度
     * @param speed 速度
     */
    void changePlaySpeed(float speed);
}
