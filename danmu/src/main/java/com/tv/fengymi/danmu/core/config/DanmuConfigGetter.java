package com.tv.fengymi.danmu.core.config;

import com.tv.fengymi.danmu.model.DanmuApiOption;

import java.util.List;
import java.util.Set;

public interface DanmuConfigGetter {

    /**
     * 允许的弹幕类型
     * @return 允许的弹幕列表
     */
    List<DanmuApiOption> getDanmuApiList();

    /**
     * fps值
     * @return fps值
     */
    default int getFps() {
        return 40;
    }

    default long getTimeGap() {
        return 1000L / getFps();
    }


    Set<String> getAllOpenSites();

    int getSpeed();

    boolean isOpen();

    int getPosition();


    int getFontSize();

    boolean isDebug();

    default boolean showFps() {
        return false;
    }
}
