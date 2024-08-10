package com.tv.fengymi.danmu.core.config;

import androidx.core.util.Consumer;

import java.util.Collection;

public interface CompositeDanmuConfigChangeHandler extends DanmuConfigChangeHandler {

    void addDanmuConfigChangeHandler(DanmuConfigChangeHandler danmuConfigChangeHandler);

    default void removeDanmuConfigChangeHandler(DanmuConfigChangeHandler danmuConfigChangeHandler) {
    }

    Collection<DanmuConfigChangeHandler> getAllDanmuConfigChangeHandler();

    @Override
    default void setFontSize(int fontSize) {
        handlerSetting((danmuConfigChangeHandler)-> danmuConfigChangeHandler.setFontSize(fontSize));
    }

    @Override
    default void setSpeed(int speed) {
        handlerSetting((danmuConfigChangeHandler)-> danmuConfigChangeHandler.setSpeed(speed));
    }

    @Override
    default void setPosition(int position) {
        handlerSetting((danmuConfigChangeHandler)-> danmuConfigChangeHandler.setPosition(position));
    }

    @Override
    default void setOpen(boolean open) {
        handlerSetting((danmuConfigChangeHandler)-> danmuConfigChangeHandler.setOpen(open));
    }

    @Override
    default void setDebug(boolean debug) {
        handlerSetting((danmuConfigChangeHandler)-> danmuConfigChangeHandler.setDebug(debug));
    }

    default void handlerSetting(Consumer<DanmuConfigChangeHandler> handlerConsumer) {
        Collection<DanmuConfigChangeHandler> allDanmuConfigChangeHandler = getAllDanmuConfigChangeHandler();
        for (DanmuConfigChangeHandler danmuConfigChangeHandler : allDanmuConfigChangeHandler) {
            handlerConsumer.accept(danmuConfigChangeHandler);
        }
    }
}
