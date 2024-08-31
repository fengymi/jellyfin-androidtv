package org.jellyfin.androidtv.customer.gsy.shade;

import android.view.View;

/**
 * 视频遮罩层
 */
public interface VideoPlayerShade extends View.OnKeyListener {

    /**
     * 当前是否显示中
     * @return 是否显示
     */
    boolean isVisible();

    /**
     * 执行显示
     */
    void show();

    /**
     * 执行隐藏
     */
    void hide();
}
