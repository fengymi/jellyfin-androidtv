package com.tv.fengymi.danmu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.tv.fengymi.danmu.core.DanmuController;
import com.tv.fengymi.danmu.core.DanmuControllerHandler;
import com.tv.fengymi.danmu.core.DanmuSurfaceController;
import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;

import timber.log.Timber;

public class DanmuSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder holder;
    private final DanmuSurfaceController danmuController;

    public DanmuSurfaceView(Context context) {
        this(context, null);
    }

    public DanmuSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DanmuSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        holder = getHolder();
        holder.addCallback(this);
        danmuController = createDanmuController();
    }

    protected DanmuSurfaceController createDanmuController() {
        return new DanmuSurfaceController(getHolder());
    }

    public DanmuController getDanmuController() {
        return danmuController;
    }

    public DanmuConfigChangeHandler getDanmuConfigChangeHandler() {
        return danmuController;
    }

    public DanmuControllerHandler getDanmuHandler() {
        return getDanmuController();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        int width = getWidth();
        int height = getHeight();
        danmuController.setWindows(width, height);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        danmuController.setWindows(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Timber.d("窗口关闭，执行停止动作");
        danmuController.stop();
        Timber.d("窗口关闭，动作停止成功");
    }
}
