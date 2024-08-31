package org.jellyfin.androidtv.customer.gsy.shade.jellyfin;

import android.view.KeyEvent;
import android.view.View;

import org.jellyfin.androidtv.customer.gsy.shade.VideoPlayerShade;
import org.jellyfin.sdk.model.api.BaseItemDto;

public interface JellyfinVideoPlayerShade extends VideoPlayerShade {

    /**
     * 更新需要显示的信息
     * @param item 当前视频信息
     * @param needShow 是否需要里面显示
     */
    void updateDisplay(BaseItemDto item, boolean needShow);

    @Override
    default boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }
}
