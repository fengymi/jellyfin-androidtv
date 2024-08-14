package com.tv.fengymi.danmu.core;

import androidx.core.util.Function;

import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.Danmu;
import com.tv.fengymi.danmu.model.MockDanmuConfig;
import com.tv.fengymi.danmu.model.PlayStateEnum;
import com.tv.fengymi.danmu.utils.DanmuUtils;
import com.tv.fengymi.danmu.utils.SortUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

/**
 * 弹幕容器类，使用com.tv.fengymi.danmu.core.DanmuContainer#iteratorDanmu(androidx.core.util.Function)进行弹幕变了
 * @see DanmuContainer#iteratorDanmu(Function)
 */
public abstract class DanmuContainer implements DanmuConfigChangeHandler {
    protected int maxCacheDanmuNums = 200;
    protected long maxGapTimes = 3000;

    protected DanmuConfigGetter danmuConfig;
    protected DanmuCollision danmuCollision;

    protected int width;
    protected int height;

    /**
     * 弹幕计算锁
     */
    protected Lock calculteShowLock;
    protected Condition calculteCondition;
    protected AtomicBoolean danmuPrepared;

    /**
     * 弹幕二级缓存容器
     */
    protected List<Danmu> needShowDanmus;
    private List<Danmu> cacheNeedShowDanmus;
    private List<Danmu> secondCacheNeedShowDanmus;

    /**
     * 当前弹幕位置
     */
    protected long currentPlayPosition;
    private long lastDanmuPositionTime;
    private int preIndex;

    private final Danmu baseCalculateDummu = new Danmu("", 0L);
    private Runnable calculator;
    private Thread calculatorThread;
    protected final AtomicBoolean clearAll = new AtomicBoolean(false);

    /**
     * 0 - 停止
     * 1 - 运行
     * 2 - 暂停
     */
    private PlayStateEnum state;

    private boolean stop;

    public DanmuContainer() {
        this(200);
    }

    public DanmuContainer(int maxCacheDanmuNums) {
        this.danmuConfig = createDanmuConfigGetter();
        this.state = PlayStateEnum.INIT;
        initDanmuCalculator(maxCacheDanmuNums);
    }

    public DanmuConfigGetter getDanmuConfig() {
        return danmuConfig;
    }

    public void setDanmuConfig(DanmuConfigGetter danmuConfig) {
        this.danmuConfig = danmuConfig;
    }

    /**
     * 创建弹幕配置
     * @return 弹幕配置信息
     */
    protected DanmuConfigGetter createDanmuConfigGetter() {
        return new MockDanmuConfig();
    }

    /**
     * 初始化
     */
    protected void initDanmuCalculator(int maxCacheDanmuNums) {
        this.calculteShowLock = new ReentrantLock();
        this.calculteCondition = this.calculteShowLock.newCondition();
        this.danmuPrepared = new AtomicBoolean(false);

        this.needShowDanmus = new ArrayList<>(maxCacheDanmuNums);
        this.cacheNeedShowDanmus = new ArrayList<>(maxCacheDanmuNums);
        this.secondCacheNeedShowDanmus = new ArrayList<>(maxCacheDanmuNums);

        calculator = () -> {
            while (state != PlayStateEnum.STOP && !stop) {
                try {
                    // 运行时才需要不断执行
                    if (state == PlayStateEnum.RUNNING) {
                        this.calculateNeedShowDanmus();
                    }
                    Thread.sleep(danmuConfig.getTimeGap());
                } catch (Exception e) {
                    Timber.e(e, "计算需要显示弹幕异常");
                }
            }
        };
    }

    /**
     * 启动容器进行循环执行
     */
    public void start() {
        this.state = PlayStateEnum.RUNNING;

        // 初始化计算器
        if (danmuCollision == null) {
            this.danmuCollision = new DanmuCollision(height, width, danmuConfig);
        }

        // 启动循环计算
        if (calculatorThread == null || !calculatorThread.isAlive()) {
            calculatorThread = new Thread(calculator);
            calculatorThread.start();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        this.state = PlayStateEnum.PAUSE;
    }

    /**
     * 清除全部数据
     */
    public void clearAll() {
        DanmuUtils.submit(() -> {
            clearAll.compareAndSet(false, true);
            resetDanmus(Collections.emptyList());
        });
    }

    /**
     * 恢复
     */
    public void restore() {
        this.state = PlayStateEnum.RUNNING;
    }

    /**
     * 设置当前播放时间
     * @param playPosition 当前播放时间
     * @param skip 是否快进/后台
     */
    public void setCurrentPosition(long playPosition, boolean skip) {
        if (skip || currentPlayPosition > playPosition) {
            reset(true);
        }
        this.currentPlayPosition = playPosition;
    }

    /**
     * 停止
     */
    public void stop() {
        this.state = PlayStateEnum.STOP;
        this.stop = true;
        if (this.calculatorThread != null) {
            Timber.d("窗口关闭，lock");
            asyncNotifyAllDanmuLock();
            Timber.d("窗口关闭，locked");
            try {
                this.calculatorThread.join(5000L);
                this.calculatorThread = null;
                Timber.d("窗口关闭，joined");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected List<Danmu> calculateNeedShowDanmus() {
        if (width == 0 || height == 0) {
            return Collections.emptyList();
        }

        return iteratorDanmu(this::doCalculateNeedShowDanmus);
    }

    private List<Danmu> doCalculateNeedShowDanmus(List<Danmu> danmus) {
        calculteShowLock.lock();
        try {
            if (danmus == null || danmus.isEmpty()) {
                calculteCondition.signalAll();
                calculteCondition.await();
                return Collections.emptyList();
            }
            this.cacheNeedShowDanmus.clear();
            this.secondCacheNeedShowDanmus.clear();

            // 如果标记为全部删除，则不保留当前数据
            int currentSize = 0;
            if (!clearAll.get()) {
                // 获取当前显示的弹幕
                List<Danmu> currentShowDanmus = this.needShowDanmus;
                currentSize = currentShowDanmus.size();
                // 保留不删除的数据
                for (Danmu next : currentShowDanmus) {
                    if (!needRemove(next, false)) {
                        this.cacheNeedShowDanmus.add(next);
                    }
                }
            } else {
                clearAll.compareAndSet(true, false);
            }

            // 获取新增的弹幕
            List<Danmu> someDanmu = getSomeDanmu(this.cacheNeedShowDanmus);
            if (someDanmu.isEmpty()) {
                updateNeedShowDanmus(this.cacheNeedShowDanmus);
                return Collections.emptyList();
            }

            danmuCollision.calculateDanmuXY(this.cacheNeedShowDanmus, someDanmu, this.currentPlayPosition);
            if (danmuConfig.isDebug()) {
                Timber.d("本次弹幕处理完成 当前=" + cacheNeedShowDanmus.size() + ", 原始=" + currentSize + ", 新增=" + someDanmu.size() + ", needShowDanmus=" + needShowDanmus.size() + ", currentShowDanmus=" + currentSize + ", 删除=" + (currentSize + someDanmu.size() - cacheNeedShowDanmus.size()));
            }
            updateNeedShowDanmus(this.cacheNeedShowDanmus);
        } catch (Exception e) {
            Timber.e(e, "计算需要展示的弹幕失败 e=");
        } finally {
            calculteShowLock.unlock();
        }

        return Collections.emptyList();
    }

    private void updateNeedShowDanmus(List<Danmu> needShowDanmus) throws InterruptedException {
        if (!needShowDanmus.isEmpty()) {
            this.secondCacheNeedShowDanmus.clear();
            this.secondCacheNeedShowDanmus.addAll(needShowDanmus);
        }

        danmuPrepared.set(true);
        calculteCondition.signalAll();
        calculteCondition.await();
    }

    protected boolean needRemove(Danmu danmu, boolean force) {
        // 展示结束
        boolean showOver = danmu.getLastX(danmuConfig.getFontSize()) <= -10;
        if (showOver) {
            return true;
        }

        // 后面没有弹幕了，不需要根据时间进行清除
        if (danmu.getNext() == null) {
            return false;
        }

        // 超出最大允许展示的时间
        long timeGap = currentPlayPosition - danmu.getStartTimestamp();
        boolean timeGap5 = timeGap > 5000;
        boolean notShow = danmu.getX() > width;
        if (timeGap5 && notShow) {
            resetNextPosition(danmu, 5000);
            return true;
        }

        boolean timeGap1 = timeGap > 1000;
        if (force && timeGap1 && notShow) {
            resetNextPosition(danmu, 1000);
            return true;
        }
        return false;
    }

    protected void resetNextPosition(Danmu danmu, long needDeathTime) {
        if (danmuConfig.isDebug()) {
            Timber.e("丢弃弹幕信息: 当前时间: " + currentPlayPosition +", 弹幕开始时间: "+ danmu.getStartTimestamp() + ", x: " + danmu.getX() + ", v: " + danmu.getValue());
        }
        Danmu next = danmu.getNext();
        if (next == null) {
            danmu.setDeath();
            return;
        }

        int gapX = next.getX() - danmu.getX();
        while (next != null) {
            next.setX(next.getX() - gapX);
            if (next.getStartTimestamp() > currentPlayPosition - needDeathTime && next.getX() > width) {
                next.setDeath();
            }
            next = next.getNext();
        }
        danmu.setDeath();
    }

    /**
     * 从全部弹幕列表中获取部分需要展示的弹幕
     * @param cacheNeedShowDanmus 当前缓存的弹幕信息
     * @return 可能需要展示的弹幕
     */
    protected List<Danmu> getSomeDanmu(List<Danmu> cacheNeedShowDanmus) {
        int needDanmuNum = maxCacheDanmuNums - cacheNeedShowDanmus.size();
        if (needDanmuNum > 5) {
            return getTopScores(needDanmuNum);
        }

        // 没有空间了，删除超过1秒而且没展示的
        Timber.i("空间不足，进行强制删除 first=" + (currentPlayPosition - cacheNeedShowDanmus.get(0).getStartTimestamp()) + ", last=" + (currentPlayPosition - cacheNeedShowDanmus.get(cacheNeedShowDanmus.size() - 1).getStartTimestamp()));
        Iterator<Danmu> iterator = cacheNeedShowDanmus.iterator();
        while (iterator.hasNext()) {
            Danmu next = iterator.next();
            if (needRemove(next, true)) {
                iterator.remove();
            }
        }

        needDanmuNum = maxCacheDanmuNums - cacheNeedShowDanmus.size();
        if (needDanmuNum <= 0) {
            return Collections.emptyList();
        }
        // 获取下个该展示的时间
        return getTopScores(needDanmuNum);
    }

    protected List<Danmu> getTopScores(int num) {
        List<Danmu> danmus = getDanmus();
        // max(MaxIndex(当前播放时间, 上次最后一个播放时间), preIndex)
        // 返回此集合中大于等于startCurrentTimestamp的下标
        baseCalculateDummu.setStartTimestamp(Math.max(currentPlayPosition, lastDanmuPositionTime));
        int currentIndex = SortUtil.findFirstGreaterThanOrEqual(danmus, baseCalculateDummu, preIndex);
        if (currentIndex < 0 || currentIndex >= danmus.size()) {
            return Collections.emptyList();
        }

        // 计算允许的最后数据
        long maxTimestamp = currentPlayPosition + maxGapTimes;
        List<Danmu> topDanmuEvents = new ArrayList<>();

        int newLastIndex = preIndex;
        for (int i = currentIndex, j = 0; i < danmus.size() && j < num; i ++) {
            Danmu danmuEvent = danmus.get(i);
            // 不能超过最大的时间数据
            if (danmuEvent.getStartTimestamp() > maxTimestamp) {
                break;
            }

            newLastIndex = i;
            topDanmuEvents.add(danmuEvent);
            j++;
        }

        if (newLastIndex == preIndex) {
            return Collections.emptyList();
        }

        if (danmuConfig.isDebug()) {
            Timber.d("本次加载弹幕成功, index 范围 [" + currentIndex + ", " + newLastIndex + "], 加载数量=" + num + ", currentPlayingTimestamp=" + currentPlayPosition + ", preLastDanmuTimestamp=" + lastDanmuPositionTime);
        }
        setPreLastRecord(newLastIndex, danmus.get(newLastIndex).getStartTimestamp());
        return topDanmuEvents;
    }

    private void setPreLastRecord(int lastIndex, long lastDanmuPositionTime) {
        this.preIndex = lastIndex;
        this.lastDanmuPositionTime = lastDanmuPositionTime;
    }

    /**
     * 获取当前需要展示的弹幕
     * @return 当前可以展示的弹幕
     */
    public List<Danmu> getNeedShowDanmus() {
        if (!calculteShowLock.tryLock()) {
            return this.needShowDanmus;
        }

        try {
            // 没有新准备的弹幕数据直接使用当前数据
            if (!danmuPrepared.compareAndSet(true, false)) {
                return needShowDanmus;
            }
            List<Danmu> temp = this.needShowDanmus;
            this.needShowDanmus = this.secondCacheNeedShowDanmus;
            this.secondCacheNeedShowDanmus = temp;
            calculteCondition.signalAll();
        } finally {
            calculteShowLock.unlock();
        }
        return needShowDanmus;
    }

    public List<Danmu> iteratorDanmu(Function<List<Danmu>, List<Danmu>> danmuIterator) {
        lockDanmus();
        try {
            return danmuIterator.apply(getDanmus());
        } finally {
            releaseDanmus();
        }
    }

    /**
     * 重新计算规则
     */
    public void reset(boolean clearAllDanmu) {
        this.preIndex = -1;
        this.lastDanmuPositionTime = 0;
        this.clearAll.set(true);

        if (this.danmuCollision == null) {
            return;
        }
        this.danmuCollision.reset(true, clearAllDanmu);
    }

    /**
     * 修改窗口大小
     * @param width 宽度
     * @param height 高度
     */
    public void setWindows(int width, int height) {
        boolean hasChange = this.width != width || this.height != height;
        this.width = width;
        this.height = height;

        if (this.danmuCollision != null && hasChange) {
            this.danmuCollision.changeWindows(height, width);
        }
    }

    @Override
    public void setFontSize(int fontSize) {
        if (this.danmuCollision != null) {
            this.danmuCollision.reset(true, false);
        }
    }

    @Override
    public void setPosition(int position) {
        if (this.danmuCollision != null) {
            this.danmuCollision.reset(true, false);
        }
    }

    @Override
    public void setOpen(boolean open) {
        if (open) {
            asyncNotifyAllDanmuLock();
        }
    }

    /**
     * 获取全部需要处理的弹幕
     * @return 全部弹幕
     */
    public abstract List<Danmu> getDanmus();

    protected void lockDanmus() {};

    protected void releaseDanmus() {};

    public final void resetDanmus(List<Danmu> danmus) {
        calculteShowLock.lock();
        try {
            doResetDanmus(danmus);
        } finally {
            calculteCondition.signalAll();
            calculteShowLock.unlock();
        }
    }

    protected abstract void doResetDanmus(List<Danmu> danmus);

    public final void addDanmu(Danmu danmu) {
        calculteShowLock.lock();
        try {
            doAddDanmu(danmu);
        } finally {
            calculteCondition.signalAll();
            calculteShowLock.unlock();
        }
    }

    /**
     * 添加单个弹幕
     * @param danmu 弹幕
     */
    protected abstract void doAddDanmu(Danmu danmu);

    /**
     * 通知弹幕任务
     */
    protected void asyncNotifyAllDanmuLock() {
        DanmuUtils.submit(() -> {
            calculteShowLock.lock();
            try {
                calculteCondition.signalAll();
            } finally {
                calculteShowLock.unlock();
            }
        });
    }
}
