package org.jellyfin.androidtv.danmu.ui.playback;

import android.view.View;

import androidx.annotation.NonNull;

import com.tv.fengymi.danmu.core.DanmuController;
import com.tv.fengymi.danmu.core.DanmuControllerHandler;
import com.tv.fengymi.danmu.core.config.CompositeDanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.model.Danmu;
import com.tv.fengymi.danmu.utils.DanmuUtils;
import com.tv.fengymi.danmu.view.DanmuSurfaceView;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.danmu.api.DanmuApi;
import org.jellyfin.androidtv.danmu.model.DanmuEvent;
import org.jellyfin.androidtv.danmu.model.DanmuResult;
import org.jellyfin.androidtv.danmu.model.DanmuSource;
import org.jellyfin.androidtv.danmu.utils.SharedPreferencesDanmuConfig;
import org.jellyfin.androidtv.danmu.utils.SimpleDanmuUtil;
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment;
import org.jellyfin.androidtv.ui.playback.PlaybackController;
import org.jellyfin.androidtv.ui.playback.VideoManager;
import org.jellyfin.sdk.api.client.Response;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

public class DanmuPlaybackController extends PlaybackController implements DanmuConfigChangeHandler {
    private DanmuSurfaceView danmuView;
    private DanmuControllerHandler danmuControllerHandler;
    private final CompositeDanmuConfigChangeHandler danmuConfigChangeHandler;

    private Runnable fetchDanmuRunnable;
    private final SharedPreferencesDanmuConfig config;
    private final DanmuApi danmuApi;
    private long currentPosition;

    private UUID danmuLoadedId;

    static int i;

    public DanmuPlaybackController(List<BaseItemDto> items, CustomPlaybackOverlayFragment fragment) {
        this(items, fragment, 0);
    }

    public DanmuPlaybackController(List<BaseItemDto> items, CustomPlaybackOverlayFragment fragment, int startIndex) {
        super(items, fragment, startIndex);
        config = KoinJavaComponent.get(SharedPreferencesDanmuConfig.class);
        danmuApi = KoinJavaComponent.get(DanmuApi.class);
        this.danmuConfigChangeHandler = new PlayCompositeDanmuConfigChangeHandler();
        this.danmuConfigChangeHandler.addDanmuConfigChangeHandler(config);

        this.danmuLoadedId = null;
        Timber.d("DanmuPlaybackController 被初始化 i=%s ", (++i));
    }

    public void initWithDanmu(@NonNull View view, @NonNull VideoManager mgr, @NonNull CustomPlaybackOverlayFragment fragment) {
        this.danmuView = view.findViewById(R.id.danmuView);
        DanmuController danmuController = danmuView.getDanmuController();
        this.danmuControllerHandler = danmuView.getDanmuHandler();
        danmuController.setDanmuGetter(config);
        this.fetchDanmuRunnable = () -> {
            try {
                if (danmuControllerHandler == null) {
                    Timber.i("danmuControllerHandler is null");
                    return;
                }

                if (!config.isOpen()) {
                    return;
                }

                BaseItemDto currentlyPlayingItem = getCurrentlyPlayingItem();
                UUID id = currentlyPlayingItem.getId();
                if (danmuLoadedId != null && Objects.equals(danmuLoadedId, id)) {
                    Timber.d("当前视频已经加载，无需重新加载");
                    return;
                }

                List<Danmu> danmus = getDanmuByItem(currentlyPlayingItem);
                if (danmus == null || danmus.isEmpty()) {
                    return;
                }

                SimpleDanmuUtil.show(view.getContext(), "一共加载" + danmus.size() + "条弹幕");
                danmuControllerHandler.resetAllDanmus(danmus, currentPosition);

                danmuLoadedId = id;
                danmuController.start();
            } catch (Exception e) {
                Timber.e(e, "加载弹幕异常");
                SimpleDanmuUtil.show(view.getContext(), "弹幕加载异常");
            }
        };

        this.danmuConfigChangeHandler.addDanmuConfigChangeHandler(danmuView.getDanmuConfigChangeHandler());
        super.init(mgr, fragment);

        loadDanmu();
    }

    @Override
    public void onCompletion() {
        super.onCompletion();

        Timber.d("onCompletion");
    }

    @Override
    public void onError() {
        super.onError();
        if (danmuControllerHandler != null) {
            danmuControllerHandler.stop();
        }
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        Timber.d("onPrepared");
    }

    @Override
    public void onProgress() {
        super.onProgress();
        this.currentPosition = getCurrentPosition();
        if (danmuControllerHandler != null) {
            danmuControllerHandler.currentPosition(currentPosition);
        }
    }

    @Override
    public void onPlaybackSpeedChange(float newSpeed) {
        super.onPlaybackSpeedChange(newSpeed);
        if (danmuControllerHandler != null) {
            danmuControllerHandler.changePlaySpeed(newSpeed);
        }
        if (config != null) {
            config.setVideoSpeed(newSpeed);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (danmuControllerHandler != null) {
            danmuControllerHandler.pause();
        }
    }

    @Override
    public void play(long position) {
        super.play(position);
        if (danmuControllerHandler != null) {
            danmuControllerHandler.restore();
        }
    }

    @Override
    public void setOpen(boolean open) {
        if (open) {
            loadDanmu();
        }
    }

    /**
     * 加载远程弹幕信息
     */
    protected void loadDanmu() {
        if (danmuControllerHandler == null) {
            return;
        }

        DanmuUtils.submit(fetchDanmuRunnable);
    }

    public DanmuConfigChangeHandler getDanmuConfigChangeHandler() {
        return danmuConfigChangeHandler;
    }

    protected List<Danmu> getDanmuByItem(BaseItemDto item) {
        Set<String> allOpenSites = config.getAllOpenSites();
        Response<DanmuResult> response = null;
        try {
            response = danmuApi.getDanmuById(item.getId(), allOpenSites);
        } catch (Exception e) {
            Timber.e(e, "弹幕加载异常");
            SimpleDanmuUtil.show(danmuView.getContext(), "加载弹幕信息失败");
        }

        if (response == null) {
            return Collections.emptyList();
        }
        return convertByResult(response);
    }

    private List<Danmu> convertByResult(Response<DanmuResult> response) {
        DanmuResult content = response.getContent();
        List<DanmuSource> sources = content.getData();
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }

        int size = 0;
        for (DanmuSource datum : sources) {
            List<DanmuEvent> danmuEvents = datum.getDanmuEvents();
            if (danmuEvents != null) {
                size += danmuEvents.size();
            }
        }

        if (size == 0) {
            return Collections.emptyList();
        }

        List<Danmu> danmus = new ArrayList<>(size);
        try {
            for (DanmuSource source : sources) {
                if (source.getDanmuEvents() == null) {
                    continue;
                }
                for (DanmuEvent danmuEvent : source.getDanmuEvents()) {
                    danmuEvent.convert();
                    Danmu danmu = new Danmu(danmuEvent.getContent(), danmuEvent.getStartTimeMillis());
                    danmu.setColor(danmuEvent.getColor());
                    danmus.add(danmu);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "弹幕解析异常");
            SimpleDanmuUtil.show(danmuView.getContext(), "弹幕解析异常");
        }
        return danmus;
    }
}
