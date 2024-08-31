package org.jellyfin.androidtv.customer;

import android.app.UiModeManager;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.gsy.JellyfinGSYVideoPlayer;
import org.jellyfin.androidtv.ui.playback.VideoQueueManager;
import org.jellyfin.androidtv.util.Utils;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.koin.java.KoinJavaComponent;

import java.util.List;

import timber.log.Timber;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class GSYPlayerActivity<T extends JellyfinGSYVideoPlayer> extends GSYBaseActivityDetail<JellyfinGSYVideoPlayer> {
    public final static String IMG_TRANSITION = "IMG_TRANSITION";
    public final static String TRANSITION = "TRANSITION";

    private static boolean isTv;
    protected T jellyfinGSYVideoPlayer;
    private boolean isTransition;
    private Transition transition;

//    private JellyfinGsyPlayerInterfaceBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_INFO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jellyfin_gsy_player_interface);

//        binding = JellyfinGsyPlayerInterfaceBinding.inflate(getLayoutInflater());
//        jellyfinGSYVideoPlayer = (T) binding.jellyfinGSYVideoPlayer;

        jellyfinGSYVideoPlayer = findViewById(R.id.jellyfinGSYVideoPlayer);

        if (true) {
            // 是否是电视
            UiModeManager uiModeManager = (UiModeManager) getApplicationContext().getSystemService(UI_MODE_SERVICE);
            isTv = uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;

            // 是否支持触屏
            boolean hasTouchScreen = getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN);
            Timber.d("isTv=%s, hasTouchScreen=%s", isTv, hasTouchScreen);
        }

        VideoQueueManager videoQueueManager = KoinJavaComponent.get(VideoQueueManager.class);
        List<BaseItemDto> currentVideoQueue = videoQueueManager.getCurrentVideoQueue();
        if (currentVideoQueue.isEmpty()) {
            Utils.showToast(this, getString(R.string.msg_no_playable_items));
            finish();
            return;
        }

        orientationUtils = new OrientationUtils(this, jellyfinGSYVideoPlayer);

        long mPosition = getIntent().getIntExtra("Position", 0);
        GSYVideoOptionBuilder gsyVideoOptionBuilder = getGSYVideoOptionBuilder()
                .setSeekOnStart(mPosition);
        gsyVideoOptionBuilder.build(jellyfinGSYVideoPlayer);
        jellyfinGSYVideoPlayer.setItems(currentVideoQueue);

        launchExternalPlayer(mPosition);
    }

    protected void launchExternalPlayer(long position) {
        jellyfinGSYVideoPlayer.play(position);
    }

    @Override
    public T getGSYVideoPlayer() {
        return jellyfinGSYVideoPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        return new GSYVideoOptionBuilder()
                .setReleaseWhenLossAudio(true);
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getGSYVideoPlayer().onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGSYVideoPlayer().onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
        }
        getGSYVideoPlayer().release();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            getGSYVideoPlayer().getFullscreenButton().performClick();
            return;
        }
        //释放所有
        jellyfinGSYVideoPlayer.release();
        if (isTransition) {
            super.onBackPressed();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out);
                }
            }, 500);
        }
    }


    private void initTransition() {
        if (isTransition) {
            postponeEnterTransition();
            ViewCompat.setTransitionName(getGSYVideoPlayer(), IMG_TRANSITION);
            addTransitionListener();
            startPostponedEnterTransition();
        } else {
            getGSYVideoPlayer().startPlayLogic();
        }
    }

    private boolean addTransitionListener() {
        transition = getWindow().getSharedElementEnterTransition();
        if (transition != null) {
            transition.addListener(new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    getGSYVideoPlayer().startPlayLogic();
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (getGSYVideoPlayer().dispatchKeyEvent(event)) {
//            return true;
//        }
//        Timber.d("activity按键监控dispatchKeyEvent keyCode=%s, event=%s", event.getKeyCode(), event.getAction());
//        if (KeyEvent.ACTION_UP == event.getAction()) {
//            if (getGSYVideoPlayer().onKeyUp(event.getKeyCode(), event)) {
//                return true;
//            }
//        } else if (KeyEvent.ACTION_DOWN == event.getAction()) {
//            if (getGSYVideoPlayer().onKeyDown(event.getKeyCode(), event)) {
//                return true;
//            }
//
//            switch (event.getKeyCode()) {
//                case KeyEvent.KEYCODE_BACK:
//                case KeyEvent.KEYCODE_BUTTON_B:
//                case KeyEvent.KEYCODE_ESCAPE:
//                    long doubleClickBackTime = System.currentTimeMillis() - backClickTime;
//                    if (doubleClickBackTime > 3000L) {
//                        backClickTime = System.currentTimeMillis();
////                        Utils.showToast(getApplicationContext(), getString(R.string.msg_playback_not_allowed));
//                        Utils.showToast(getApplicationContext(), "双击返回");
//                        return true;
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }

        return super.dispatchKeyEvent(event);
    }

    private long backClickTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
            case KeyEvent.KEYCODE_ESCAPE:
                long doubleClickBackTime = System.currentTimeMillis() - backClickTime;
                if (doubleClickBackTime > 3000L) {
                    backClickTime = System.currentTimeMillis();
//                        Utils.showToast(getApplicationContext(), getString(R.string.msg_playback_not_allowed));
                    Utils.showToast(getApplicationContext(), "双击返回");
                    return true;
                }
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
