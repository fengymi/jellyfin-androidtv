package org.jellyfin.androidtv.danmu.ui.playback;

import com.tv.fengymi.danmu.core.config.CompositeDanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PlayCompositeDanmuConfigChangeHandler implements CompositeDanmuConfigChangeHandler {
    private final Set<DanmuConfigChangeHandler> danmuConfigChangeHandlers;

    public PlayCompositeDanmuConfigChangeHandler() {
        this.danmuConfigChangeHandlers = new HashSet<>();
    }

    @Override
    public void addDanmuConfigChangeHandler(DanmuConfigChangeHandler danmuConfigChangeHandler) {
        if (Objects.isNull(danmuConfigChangeHandler)) {
            return;
        }
        this.danmuConfigChangeHandlers.add(danmuConfigChangeHandler);
    }

    @Override
    public void removeDanmuConfigChangeHandler(DanmuConfigChangeHandler danmuConfigChangeHandler) {
        this.danmuConfigChangeHandlers.remove(danmuConfigChangeHandler);
    }

    @Override
    public Collection<DanmuConfigChangeHandler> getAllDanmuConfigChangeHandler() {
        return danmuConfigChangeHandlers;
    }
}
