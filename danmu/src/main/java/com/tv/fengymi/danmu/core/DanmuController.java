package com.tv.fengymi.danmu.core;


import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.core.container.CopyOnWriteDanmuContainer;
import com.tv.fengymi.danmu.model.Danmu;
import com.tv.fengymi.danmu.model.MockDanmuConfig;
import com.tv.fengymi.danmu.model.PlayStateEnum;

import java.util.List;

import timber.log.Timber;

public class DanmuController implements DanmuControllerHandler, DanmuConfigChangeHandler {
    protected DanmuContainer danmuContainer;
    protected PlayStateEnum state;
    protected DanmuConfigGetter danmuConfig;

    public DanmuController() {
        this(new CopyOnWriteDanmuContainer());
    }

    public DanmuController(DanmuContainer danmuContainer) {
        this.danmuContainer = danmuContainer;
        this.state = PlayStateEnum.INIT;
        this.danmuConfig = new MockDanmuConfig();
    }

    /**
     * 获取当前需要展示的弹幕
     * @return 当前可以展示的弹幕
     */
    public List<Danmu> getNeedShowDanmus() {
        return this.danmuContainer.getNeedShowDanmus();
    }

    public void setDanmuGetter(DanmuConfigGetter danmuGetter) {
        this.danmuConfig = danmuGetter;
        danmuContainer.setDanmuConfig(danmuGetter);
    }

    public DanmuConfigGetter getDanmuConfigGetter() {
        return this.danmuConfig;
    }

    /**
     * 执行规则
     */
    public void start() {
        state = PlayStateEnum.RUNNING;
        onStart();
    }

    protected void onStart() {
        danmuContainer.start();
    }

    /**
     * 修改窗口大小
     * @param width 宽度
     * @param height 高度
     */
    public void setWindows(int width, int height) {
        danmuContainer.setWindows(width, height);
    }

    @Override
    public void currentPosition(long playPosition) {
        danmuContainer.setCurrentPosition(playPosition, false);
    }

    @Override
    public void restore() {
        state = PlayStateEnum.RUNNING;
        danmuContainer.restore();
        Timber.d("恢复");
    }

    @Override
    public void pause() {
        state = PlayStateEnum.PAUSE;
        danmuContainer.pause();
        Timber.d("暂停");
    }

    @Override
    public void skipPlayProcess(long currentPlayProcess) {
        danmuContainer.setCurrentPosition(currentPlayProcess, true);
    }

    @Override
    public void error() {
        state = PlayStateEnum.PAUSE;
        danmuContainer.pause();
        Timber.d("播放器异常");
    }

    @Override
    public void stop() {
        state = PlayStateEnum.STOP;
        danmuContainer.stop();
        Timber.d("停止");
    }

    @Override
    public void clearAll() {
        danmuContainer.clearAll();
    }

    @Override
    public void resetAllDanmus(List<Danmu> danmus) {
        danmuContainer.resetDanmus(danmus);
    }

    @Override
    public void resetAllDanmus(List<Danmu> danmus, long playPosition) {
        danmuContainer.setCurrentPosition(playPosition, true);
        resetAllDanmus(danmus);
    }

    @Override
    public void changePlaySpeed(float speed) {}

    public void setFontSize(int fontSize) {
        danmuContainer.setFontSize(fontSize);
    }

    @Override
    public void setSpeed(int speed) {
        danmuContainer.setSpeed(speed);
    }

    @Override
    public void setPosition(int position) {
        danmuContainer.setPosition(position);
    }

    @Override
    public void setOpen(boolean open) {
        danmuContainer.setOpen(open);
    }

    @Override
    public void setDebug(boolean debug) {
        danmuContainer.setDebug(debug);
    }
}
