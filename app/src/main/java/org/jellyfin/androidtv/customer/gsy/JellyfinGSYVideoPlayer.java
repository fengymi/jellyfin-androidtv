package org.jellyfin.androidtv.customer.gsy;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.GSYPlayerActivity;
import org.jellyfin.androidtv.customer.gsy.shade.jellyfin.BottomVideoPlayerShade;
import org.jellyfin.androidtv.customer.gsy.shade.jellyfin.JellyfinVideoPlayerShade;
import org.jellyfin.androidtv.customer.gsy.shade.jellyfin.TopPanelVideoPlayerShade;
import org.jellyfin.androidtv.data.compat.PlaybackException;
import org.jellyfin.androidtv.data.compat.StreamInfo;
import org.jellyfin.androidtv.data.compat.VideoOptions;
import org.jellyfin.androidtv.data.model.DataRefreshService;
import org.jellyfin.androidtv.preference.UserPreferences;
import org.jellyfin.androidtv.preference.constant.AudioBehavior;
import org.jellyfin.androidtv.ui.livetv.TvManager;
import org.jellyfin.androidtv.ui.playback.PlaybackManager;
import org.jellyfin.androidtv.util.DeviceUtils;
import org.jellyfin.androidtv.util.ImageHelper;
import org.jellyfin.androidtv.util.Utils;
import org.jellyfin.androidtv.util.apiclient.ReportingHelper;
import org.jellyfin.androidtv.util.profile.ExoPlayerProfile;
import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dlna.DeviceProfile;
import org.jellyfin.apiclient.model.session.PlayMethod;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.jellyfin.sdk.model.api.BaseItemKind;
import org.jellyfin.sdk.model.api.LocationType;
import org.jellyfin.sdk.model.api.MediaSourceInfo;
import org.jellyfin.sdk.model.serializer.UUIDSerializerKt;
import org.koin.java.KoinJavaComponent;

import java.time.LocalDateTime;
import java.util.List;

import timber.log.Timber;

public class JellyfinGSYVideoPlayer extends TvNormalGSYVideoPlayer {
    protected static final int RUNTIME_TICKS_TO_MS = 10000;
    // 当前播放媒体index
    protected int mCurrentNdx;
    protected StreamInfo mCurrentStreamInfo;
//    protected long mPosition = 0L;
    protected boolean isLiveTvItem;
    protected VideoOptions mCurrentOptions;

    protected List<BaseItemDto> items;

    protected ReportingHelper reportingHelper;
    protected PlaybackManager playbackManager;
    protected org.jellyfin.sdk.api.client.ApiClient api;
    protected ApiClient apiClient;
    protected DataRefreshService dataRefreshService;
    protected UserPreferences userPreferences;
    protected ImageHelper imageHelper;

    protected LocalDateTime mCurrentProgramEnd = null;
    protected LocalDateTime mCurrentProgramStart = null;

    protected Handler mHandler = new Handler();
    protected Runnable mReportLoop;


//    protected Runnable mHideTask;
    protected GSYVideoProgressListener gsyVideoProgressListener;


    /**
     * 标题部分
     */
    protected JellyfinVideoPlayerShade topContainerShade;
    protected JellyfinVideoPlayerShade bottomContainerShade;


    protected GSYPlayerActivity<JellyfinGSYVideoPlayer> gsyPlayerActivity;

    public JellyfinGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public JellyfinGSYVideoPlayer(Context context) {
        super(context, (AttributeSet) null);
    }

    public JellyfinGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.gsy_player_ui_layout;
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        this.reportingHelper = KoinJavaComponent.get(ReportingHelper.class);
        this.playbackManager = KoinJavaComponent.get(PlaybackManager.class);
        this.api = KoinJavaComponent.get(org.jellyfin.sdk.api.client.ApiClient.class);
        this.apiClient = KoinJavaComponent.get(ApiClient.class);
        this.dataRefreshService = KoinJavaComponent.get(DataRefreshService.class);
        this.userPreferences = KoinJavaComponent.get(UserPreferences.class);
        this.imageHelper = KoinJavaComponent.get(ImageHelper.class);

        this.topContainerShade = new TopPanelVideoPlayerShade(this);
        this.bottomContainerShade = new BottomVideoPlayerShade(this, this);
        // setup fade task
//        mHideTask = this::shadeHide;

        setDismissControlTime(5000);
        gsyVideoProgressListener = (progress, secProgress, currentPosition, duration) ->{
//            Timber.d("mCurrentPosition=%s, currentPosition=%s", mCurrentPosition, currentPosition);
            mCurrentPosition = currentPosition;

        } ;
        setGSYVideoProgressListener(gsyVideoProgressListener);
    }

//    public void shadeShow() {
//        topContainerShade.show();
//        this.bottomContainerShade.show();
//    }
//
//    @Override
//    protected void hideAllWidget() {
//        topContainerShade.hide();
//        this.bottomContainerShade.hide();
//        super.hideAllWidget();
//    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        if (view == mTopContainer) {
            if (visibility == View.VISIBLE) {
                this.topContainerShade.show();
            } else {
                this.topContainerShade.hide();
            }
            return;
        }

        if (view == mBottomContainer) {
            if (visibility == View.VISIBLE) {
                this.bottomContainerShade.show();
            } else {
                this.bottomContainerShade.hide();
            }
            return;
        }

        super.setViewShowState(view, visibility);
    }

    public boolean shadeIsVisible() {
        return topContainerShade.isVisible();
    }

    protected void updateShadeDisplay(BaseItemDto item) {
        topContainerShade.updateDisplay(item, true);
//        mHandler.postDelayed(mHideTask, 5000L);
    }

    public boolean hasPreItem(){
        return items != null && mCurrentNdx > 0;
    }

    public boolean hasNextItem() {
        return items != null && mCurrentNdx < items.size() - 1;
    }

    /**
     * 播放上一集
     */
    public void playPre(){
        if (!hasPreItem()) {
            Utils.showToast(getContext(), R.string.msg_no_playable_items);
            return;
        }

        mCurrentNdx --;
        onVideoReset();
        play(0);
    }

    @Override
    public void onAutoCompletion() {
        if (hasNextItem()) {
            playNext();
            return;
        }

        super.onAutoCompletion();
    }

    public void playNext() {
        if (!hasNextItem()) {
            Utils.showToast(getContext(), R.string.msg_no_playable_items);
            return;
        }

        mCurrentNdx ++;
        onVideoReset();
        play(0);
    }

    public boolean isPlaying() {
        return mCurrentState == CURRENT_STATE_PREPAREING
                || mCurrentState == CURRENT_STATE_PLAYING
                || mCurrentState == CURRENT_STATE_PLAYING_BUFFERING_START;
    }

    public void play(long position) {
        if(isInPlayingState()) {
            return;
        }

        switch (mCurrentState) {
            case CURRENT_STATE_PAUSE:
                // just resume
                onVideoResume();
                startReportLoop();
                break;
            case -1:
            case CURRENT_STATE_NORMAL:
                // start new playback

                // set mSeekPosition so the seekbar will not default to 0:00
                mCurrentPosition = position;
                mSeekOnStart = position;
                mCurrentPosition = 0;

                BaseItemDto item = getCurrentlyPlayingItem();
                if (item == null) {
                    Timber.d("item is null - aborting play");
                    Utils.showToast(getContext(), getContext().getString(R.string.msg_cannot_play));
                    finish();
                    return;
                }
                updateShadeDisplay(item);

                // make sure item isn't missing
                if (item.getLocationType() == LocationType.VIRTUAL) {
                    if (hasNextItem()) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.episode_missing)
                                .setMessage(R.string.episode_missing_message)
                                .setPositiveButton(R.string.lbl_yes, (dialog, which) -> playNext())
                                .setNegativeButton(R.string.lbl_no, (dialog, which) -> finish())
                                .create()
                                .show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.episode_missing)
                                .setMessage(R.string.episode_missing_message_2)
                                .setPositiveButton(R.string.lbl_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .create()
                                .show();
                    }
                    return;
                }

                isLiveTvItem = item.getType() == BaseItemKind.TV_CHANNEL;
//                startSpinner();

                // undo setting mSeekPosition for liveTV
//                if (isLiveTvItem) mSeekPosition = -1;



//                long duration = getCurrentlyPlayingItem().getRunTimeTicks() != null ? getCurrentlyPlayingItem().getRunTimeTicks() / 10000 : -1;
//                if (duration > 0) {
//                    mSeekOnStart = duration;
//                }
//                int maxBitrate = Utils.getMaxBitrate(userPreferences.getValue());
//                Timber.d("Max bitrate is: %d", maxBitrate);
                VideoOptions internalOptions = buildExoPlayerOptions(null, item, 2_000_000_000);

                playInternal(item, position, internalOptions);
                break;
        }
    }

    public void setItems(List<BaseItemDto> items) {
        this.items = items;
    }

    public BaseItemDto getCurrentlyPlayingItem() {
        return items.size() > mCurrentNdx ? items.get(mCurrentNdx) : null;
    }

    protected void finish() {
        if (this.gsyPlayerActivity != null) {
            this.gsyPlayerActivity.finish();
        }
    }

    protected void playInternal(final BaseItemDto item, final Long position, final VideoOptions internalOptions) {
        if (isLiveTvItem) {
            updateTvProgramInfo();
            TvManager.setLastLiveTvChannel(item.getId());
            //internal/exo player
            Timber.i("Using internal player for Live TV");
            playbackManager.getVideoStreamInfo(api.getDeviceInfo(), internalOptions, position * 10000, apiClient, new Response<StreamInfo>() {
                @Override
                public void onResponse(StreamInfo response) {
                    if (!hasInitializedVideoManager())
                        return;
                    mCurrentOptions = internalOptions;
                    startItem(item, position, response);
                }

                @Override
                public void onError(Exception exception) {
                    handlePlaybackInfoError(exception);
                }
            });
        } else {
            playbackManager.getVideoStreamInfo(api.getDeviceInfo(), internalOptions, position * 10000, apiClient, new Response<StreamInfo>() {
                @Override
                public void onResponse(StreamInfo internalResponse) {
                    Timber.i("Internal player would %s", internalResponse.getPlayMethod().equals(PlayMethod.Transcode) ? "transcode" : "direct stream");
                    mCurrentOptions = internalOptions;
                    startItem(item, position, internalResponse);
                }

                @Override
                public void onError(Exception exception) {
                    Timber.e(exception, "Unable to get stream info for internal player");
                }
            });
        }
    }

    public void updateTvProgramInfo() {
        // Get the current program info when playing a live TV channel
        final BaseItemDto channel = getCurrentlyPlayingItem();
        if (channel.getType() == BaseItemKind.TV_CHANNEL) {
//            PlaybackControllerHelperKt.getLiveTvChannel(this, channel.getId(), updatedChannel -> {
//                BaseItemDto program = updatedChannel.getCurrentProgram();
//                if (program != null) {
//                    mCurrentProgramEnd = program.getEndDate();
//                    mCurrentProgramStart = program.getPremiereDate();
//                }
//                return null;
//            });
        }
    }

    protected boolean hasInitializedVideoManager() {
        return this.getGSYVideoManager() != null;
    }

    protected void startItem(BaseItemDto item, long position, StreamInfo response) {
        if (!hasInitializedVideoManager()) {
            Timber.e("Error - attempting to play without:%s%s", hasInitializedVideoManager() ? "" : " [videoManager]", gsyPlayerActivity != null ? "" : " [overlay fragment]");
            return;
        }

        mCurrentPosition = position;
        mCurrentStreamInfo = response;

        // set media source Id in case we need to switch to transcoding
//        mCurrentOptions.setMediaSourceId(response.getMediaSource().getId());

        // get subtitle info
//        mSubtitleStreams = response.getSubtitleProfiles(false, apiClient.getApiUrl(), apiClient.getAccessToken());
//        mDefaultSubIndex = response.getMediaSource().getDefaultSubtitleStreamIndex() != null ? response.getMediaSource().getDefaultSubtitleStreamIndex() : mDefaultSubIndex;
//        setDefaultAudioIndex(response);

        // if burning in, set the subtitle index and the burningSubs flag so that onPrepared and switchSubtitleStream will know that we already have subtitles enabled
//        burningSubs = false;
//        if (mCurrentStreamInfo.getPlayMethod() == PlayMethod.Transcode && getSubtitleStreamInfo(mDefaultSubIndex) != null &&
//                getSubtitleStreamInfo(mDefaultSubIndex).getDeliveryMethod() == SubtitleDeliveryMethod.Encode) {
//            mCurrentOptions.setSubtitleStreamIndex(mDefaultSubIndex);
//            Timber.d("stream started with burnt in subs");
//            burningSubs = true;
//        } else {
//            mCurrentOptions.setSubtitleStreamIndex(null);
//        }

//        // set refresh rate
//        if (refreshRateSwitchingBehavior != RefreshRateSwitchingBehavior.DISABLED) {
//            setRefreshRate(JavaCompat.getVideoStream(response.getMediaSource()));
//        }

        setUp(response.getMediaUrl(), mCache, null);
        startPlayLogic();
        //wait a beat before attempting to start so the player surface is fully initialized and video is ready
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startPlayLogic();
//            }
//        }, 750);

        dataRefreshService.setLastPlayedItem(item);
        // 上报数据
        reportingHelper.reportStart(item, mCurrentPosition * RUNTIME_TICKS_TO_MS);
        startReportLoop();
    }


    private void handlePlaybackInfoError(Exception exception) {
        Timber.e(exception, "Error getting playback stream info");
        if (gsyPlayerActivity == null) return;
        if (exception instanceof PlaybackException) {
            PlaybackException ex = (PlaybackException) exception;
            switch (ex.getErrorCode()) {
                case NotAllowed:
                    Utils.showToast(getContext(), getContext().getString(R.string.msg_playback_not_allowed));
                    break;
                case NoCompatibleStream:
                    Utils.showToast(getContext(), getContext().getString(R.string.msg_playback_incompatible));
                    break;
                case RateLimitExceeded:
                    Utils.showToast(getContext(), getContext().getString(R.string.msg_playback_restricted));
                    break;
            }
        } else {
            Utils.showToast(getContext(), getContext().getString(R.string.msg_cannot_play));
        }

        release();
        finish();
    }

    public org.jellyfin.sdk.model.api.MediaSourceInfo getCurrentMediaSource() {
        if (mCurrentStreamInfo != null && mCurrentStreamInfo.getMediaSource() != null) {
            return mCurrentStreamInfo.getMediaSource();
        } else {
            BaseItemDto item = getCurrentlyPlayingItem();
            List<org.jellyfin.sdk.model.api.MediaSourceInfo> mediaSources = item.getMediaSources();

            if (mediaSources == null || mediaSources.isEmpty()) {
                return null;
            } else {
                // Prefer the media source with the same id as the item
                for (MediaSourceInfo mediaSource : mediaSources) {
                    if (UUIDSerializerKt.toUUIDOrNull(mediaSource.getId()).equals(item.getId())) {
                        return mediaSource;
                    }
                }
                // Or fallback to the first media source if none match
                return mediaSources.get(0);
            }
        }
    }

    private VideoOptions buildExoPlayerOptions(@Nullable Integer forcedSubtitleIndex, BaseItemDto item, int maxBitrate) {
        VideoOptions internalOptions = new VideoOptions();
        internalOptions.setItemId(item.getId());
        internalOptions.setMediaSources(item.getMediaSources());
        internalOptions.setMaxBitrate(maxBitrate);
        internalOptions.setEnableDirectStream(true);
        internalOptions.setSubtitleStreamIndex(forcedSubtitleIndex);
        MediaSourceInfo currentMediaSource = getCurrentMediaSource();
        if (!isLiveTvItem && currentMediaSource != null) {
            internalOptions.setMediaSourceId(currentMediaSource.getId());
        }
        DeviceProfile internalProfile = new ExoPlayerProfile(
                isLiveTvItem && !userPreferences.get(UserPreferences.Companion.getLiveTvDirectPlayEnabled()),
                userPreferences.get(UserPreferences.Companion.getAc3Enabled()),
                userPreferences.get(UserPreferences.Companion.getAudioBehaviour()) == AudioBehavior.DOWNMIX_TO_STEREO,
                !DeviceUtils.has4kVideoSupport()
        );
        internalOptions.setProfile(internalProfile);
        return internalOptions;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && shadeIsVisible()) {
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_BACKSLASH:
                    hideAllWidget();
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // 中间确定键
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // 下键
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!shadeIsVisible()) {
                    changeUiToPreparingShow();
                    return true;
                }
                break;

            // 返回键
            case KeyEvent.KEYCODE_BACK:
                if (shadeIsVisible()) {
                    hideAllWidget();
                    return true;
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
////        if (bottomContainerShade != null) {
////            if (bottomContainerShade.onKey(this, keyCode, event)) {
////                return true;
////            }
////        }
//        Timber.d("按键监控onKeyDown keyCode=%s, event=%s", keyCode, event.getAction());
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onVideoResume() {
        startReportLoop();
        super.onVideoResume();
    }

    @Override
    protected void releaseVideos() {
        stopReportLoop();
        reportingHelper.reportStopped(getCurrentlyPlayingItem(), mCurrentStreamInfo, mCurrentPosition * RUNTIME_TICKS_TO_MS);
        super.releaseVideos();
    }

    @Override
    public void onVideoPause() {
        if (mCurrentState != CURRENT_STATE_PAUSE) {
            stopReportLoop();
            startPauseReportLoop();
        }
        super.onVideoPause();
    }

    /**
     * 开始上报数据
     */
    protected void startReportLoop() {
        reportingHelper.reportProgress(null, items.get(mCurrentNdx), mCurrentStreamInfo, null, false);
        mReportLoop = new Runnable() {
            @Override
            public void run() {
                if (mCurrentState != CURRENT_STATE_PLAYING) return;

                reportingHelper.reportProgress(null, items.get(mCurrentNdx), mCurrentStreamInfo, mCurrentPosition * RUNTIME_TICKS_TO_MS, false);
                mHandler.postDelayed(this, 5000);
            }
        };
        mHandler.postDelayed(mReportLoop, 5000);
    }

    /**
     * 停止上报数据
     */
    protected void stopReportLoop() {
        if (mHandler != null && mReportLoop != null) {
            mHandler.removeCallbacks(mReportLoop);
        }
    }

    private void startPauseReportLoop() {
        stopReportLoop();
        reportingHelper.reportProgress(null, getCurrentlyPlayingItem(), mCurrentStreamInfo, mCurrentPosition * RUNTIME_TICKS_TO_MS, true);
        mReportLoop = new Runnable() {
            @Override
            public void run() {
                BaseItemDto currentItem = getCurrentlyPlayingItem();
                if (currentItem == null) {
                    // Loop was called while nothing was playing!
                    stopReportLoop();
                    return;
                }

                if (mCurrentState != CURRENT_STATE_PAUSE) {
                    // Playback is not paused anymore, stop reporting
                    return;
                }
//                refreshCurrentPosition();
//                long currentTime = isLiveTv ? getTimeShiftedProgress() : mCurrentPosition;
//                if (isLiveTv && !directStreamLiveTv && mFragment != null) {
//                    mFragment.setSecondaryTime(getRealTimeProgress());
//                }

                reportingHelper.reportProgress(null, currentItem, mCurrentStreamInfo, mCurrentPosition * RUNTIME_TICKS_TO_MS, true);
                mHandler.postDelayed(this, 15 * 1000L);
            }
        };
        mHandler.postDelayed(mReportLoop, 15 * 1000L);
    }
}
