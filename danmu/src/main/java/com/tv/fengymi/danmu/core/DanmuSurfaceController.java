package com.tv.fengymi.danmu.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.Danmu;
import com.tv.fengymi.danmu.model.PlayStateEnum;
import com.tv.fengymi.danmu.utils.DanmuUtils;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

public class DanmuSurfaceController extends DanmuController {
    private final int fps = danmuConfig.getFps();
    private final long timeGap = 1000L / fps;
    private final SurfaceHolder holder;
    private final Paint paint; // 绘制文本的画笔
    private final Paint stockPaint; // 绘制文本的画笔
    private Paint fpsPaint;

    protected int width;
    protected int height;

    private final Runnable painter;
    private Thread drawThread;

    private final Lock playStateLock;
    private final Condition playStateCondition;

    protected boolean stop;
    private int defaultColor = Color.WHITE;

    private int fpsCount = 0;
    private String showFpsText = "";
    private long fpsPreTime = System.currentTimeMillis();

    public DanmuSurfaceController(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSPARENT);
        this.holder = holder;

        int fontSize = 30;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 初始化画笔，开启抗锯齿
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE); // 文本颜色
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(fontSize);

        stockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stockPaint.setStyle(Paint.Style.STROKE);
        stockPaint.setColor(Color.BLACK);
        stockPaint.setTypeface(Typeface.DEFAULT_BOLD);
        stockPaint.setTextSize(fontSize);
        stockPaint.setStrokeWidth(2f);

        this.playStateLock = new ReentrantLock();
        this.playStateCondition = this.playStateLock.newCondition();

        painter = () -> {
            while (state != PlayStateEnum.STOP && !stop) {
                try {
                    if (state == PlayStateEnum.PAUSE || !danmuConfig.isOpen()) {
                        sateWait(!danmuConfig.isOpen());
                    }
                    if (state == PlayStateEnum.RUNNING) {
                        drawDanmu();
                    }
                    Thread.sleep(timeGap);
                } catch (Exception e) {
                    Timber.e(e, "执行弹幕渲染异常");
                }
            }

            Timber.d("弹幕执行结束， state=%s", state);
        };
    }

    @Override
    public void setWindows(int width, int height) {
        this.width = width;
        this.height = height;
        super.setWindows(width, height);
    }

    @Override
    public void skipPlayProcess(long currentPlayProcess) {
        super.skipPlayProcess(currentPlayProcess);
        clear();
    }

    @Override
    public void restore() {
        sateWaitRelease();
        super.restore();
    }

    @Override
    public void error() {
        super.error();
        clear();
    }

    @Override
    public void start() {
        super.start();
        this.state = PlayStateEnum.RUNNING;
        if (drawThread == null || !drawThread.isAlive()) {
            drawThread = new Thread(painter);
            drawThread.start();
        }
        sateWaitRelease();
    }

    @Override
    public void stop() {
        super.stop();
        this.stop = true;
        clear();
        sateWaitRelease();
        if (this.drawThread != null) {
            try {
                this.drawThread.join(5000L);
                this.drawThread = null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setDanmuGetter(DanmuConfigGetter danmuGetter) {
        int fontSize = danmuGetter.getFontSize();
        if (((int) paint.getTextSize())!= fontSize) {
            paint.setTextSize(fontSize);
            stockPaint.setTextSize(fontSize);
        }

        super.setDanmuGetter(danmuGetter);
    }

    @Override
    public void setFontSize(int fontSize) {
        super.setFontSize(fontSize);
        this.paint.setTextSize(fontSize);
        this.stockPaint.setTextSize(fontSize);
    }

    @Override
    public void setOpen(boolean open) {
        super.setOpen(open);
        if (open) {
            DanmuUtils.submit(this::sateWaitRelease);
        } else {
            clear();
        }
    }

    // 在这个方法中实现弹幕的绘制逻辑
    protected void drawDanmu() {
        Canvas canvas = null;
        try {
            // 在画布上锁定
            canvas = holder.lockCanvas();
            // 在这里进行弹幕的绘制
            if (canvas != null) {
                renderDanmaku(canvas);
            } else {
                Timber.i("不需要执行弹幕渲染 canvas=null");
            }
        } finally {
            if (canvas != null) {
                // 同步结束，并释放画布
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void renderDanmaku(Canvas canvas) {
        // 清空屏幕
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 绘制弹幕
        this.showDanMu(canvas);
        if (danmuConfig.isDebug()) {
            this.showFps(canvas);
        }
    }

    protected void showFps(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        long time = currentTime - fpsPreTime;

        int fontSize = 20;
        if (time > 1000L) {
            if (fpsPaint == null) {
                fpsPaint = new Paint();
                fpsPaint.setTextSize(20);
                fpsPaint.setColor(Color.GREEN);
            }

            showFpsText = "fps: " + fpsCount * 1000L / time + "/" + fps;
            fpsPreTime = currentTime;
            fpsCount = 0;
        } else {
            fpsCount ++;
        }

        canvas.drawText(showFpsText, width - showFpsText.length() * fontSize - 30, 10 + fontSize, fpsPaint);
    }

    protected void showDanMu(Canvas canvas) {
        List<Danmu> needShowDanmus = getNeedShowDanmus();
        if (needShowDanmus == null || needShowDanmus.isEmpty()) {
            return;
        }

        int total = needShowDanmus.size(), showNum = 0, ignoreNum = 0;
        // 绘制文本
        for (Danmu danma : needShowDanmus) {
            // 不展示，只进行数据更新
            if (danma.getX() > width) {
                ignoreNum ++;
                danma.update(danmuConfig.getSpeed());
                continue;
            }

            boolean needChangeColor = defaultColor != 2
                    && danma.getColor() != defaultColor;
            if (needChangeColor) {
                paint.setColor(danma.getColor());
            }
            canvas.drawText(danma.getValue(), danma.getX(), danma.getY(), stockPaint);
            canvas.drawText(danma.getValue(), danma.getX(), danma.getY(), paint);
            danma.update(danmuConfig.getSpeed());

            if (needChangeColor) {
                paint.setColor(defaultColor);
            }
            showNum ++;
        }

        if (danmuConfig.isDebug()) {
            Timber.d("弹幕显示数, total=%s, showNum=%s, ignoreNum=%s", total, showNum, ignoreNum);
        }
    }

    @Override
    public void resetAllDanmus(List<Danmu> danmus, long playPosition) {
        int count = 0;
        int color = Color.WHITE;
        for (Danmu danmu : danmus) {
            if (count == 0) {
                color = danmu.getColor();
            }
            count += (color == danmu.getColor()) ? 1 : -1;
        }

        this.defaultColor = count > 0 ? color : 2;
        super.resetAllDanmus(danmus);
    }

    @Override
    public void clearAll() {
        super.clearAll();
        clear();
    }

    protected void sateWait(boolean needClear) {
        Timber.d("暂停，停止弹幕渲染 start, needClear=%s", needClear);
        if (needClear) {
            clear();
        }
        this.playStateLock.lock();
        try {
            this.playStateCondition.await();
        } catch (InterruptedException e) {
            Timber.e(e, "状态等待异常");
        } finally {
            this.playStateLock.unlock();
        }
        Timber.d("暂停，停止弹幕渲染 end");
    }

    protected void clear() {
        // 关闭需要清除弹幕
        Canvas canvas = null;
        try {
            // 在画布上锁定
            canvas = holder.lockCanvas();
            // 在这里进行弹幕的绘制
            if (canvas != null) {
                // 清空屏幕
                getNeedShowDanmus().clear();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            } else {
                Timber.i("不需要执行弹幕渲染 canvas=null");
            }
        } finally {
            if (canvas != null) {
                // 同步结束，并释放画布
                holder.unlockCanvasAndPost(canvas);
            }
        }

        Timber.d("执行clear进行弹幕清除");
    }

    protected void sateWaitRelease() {
        Timber.d("恢复，通知弹幕渲染 start");
        this.playStateLock.lock();
        try {
            this.playStateCondition.signalAll();
        } finally {
            this.playStateLock.unlock();
        }
        Timber.d("恢复，通知弹幕渲染 end");
    }
}
