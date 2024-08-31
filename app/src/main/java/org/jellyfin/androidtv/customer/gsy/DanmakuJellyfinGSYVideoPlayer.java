package org.jellyfin.androidtv.customer.gsy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import org.jellyfin.androidtv.customer.ByteReadChannelInputStream;
import org.jellyfin.androidtv.customer.danmu.BiliDanmukuParser;
import org.jellyfin.androidtv.customer.danmu.DanamakuAdapter;
import org.jellyfin.androidtv.danmu.api.DanmuApi;
import org.jellyfin.sdk.api.client.Response;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.koin.java.KoinJavaComponent;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

import io.ktor.utils.io.ByteReadChannel;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import timber.log.Timber;

public class DanmakuJellyfinGSYVideoPlayer extends JellyfinGSYVideoPlayer {

    private BaseItemDto baseItemDto;
    private DanmuApi danmuApi;

    public DanmakuJellyfinGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        this.danmuApi = KoinJavaComponent.get(DanmuApi.class);
        initHandler();
        getDisplay(context);
    }

    public DanmakuJellyfinGSYVideoPlayer(Context context) {
        super(context);
        this.danmuApi = KoinJavaComponent.get(DanmuApi.class);
        initHandler();
        getDisplay(context);
    }

    public DanmakuJellyfinGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.danmuApi = KoinJavaComponent.get(DanmuApi.class);
        initHandler();
        getDisplay(context);
    }

    public BaseItemDto getBaseItemDto() {
        return baseItemDto;
    }

    public void setBaseItemDto(BaseItemDto baseItemDto) {
        this.baseItemDto = baseItemDto;
    }

    @Override
    public TextView getTitleTextView() {
        return super.getTitleTextView();
    }

    private BaseDanmakuParser mParser;//解析器对象
    private IDanmakuView mDanmakuView;//弹幕view
    private DanmakuContext mDanmakuContext;

    private long mDanmakuStartSeekPosition = -1;
    private boolean mDanmaKuShow = true;

    //    @Override
//    public int getLayoutId() {
//        return R.layout.vlc_player_interface;
//    }

    public void setmDanmakuView(IDanmakuView mDanmakuView) {
        this.mDanmakuView = mDanmakuView;
        initDanmaku();
    }

    @Override
    public void startPlayLogic() {
        super.startPlayLogic();
    }

    @Override
    protected void init(Context context) {
        super.init(context);
//        mDanmakuView = findViewById(R.id.danmuView);
//
//        //初始化弹幕显示
//        initDanmaku();
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        onPrepareDanmaku(this);
    }

    @Override
    public void onVideoPause() {
        super.onVideoPause();
        danmakuOnPause();
    }

    @Override
    public void onVideoResume(boolean isResume) {
        super.onVideoResume(isResume);
        danmakuOnResume();
    }

    @Override
    protected void clickStartIcon() {
        super.clickStartIcon();
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            danmakuOnResume();
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            danmakuOnPause();
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        releaseDanmaku(this);
    }


    @Override
    public void onSeekComplete() {
        super.onSeekComplete();
        long time = mProgressBar.getProgress() * getDuration() / 100;
        //如果已经初始化过的，直接seek到对于位置
        if (mHadPlay && getDanmakuView() != null && getDanmakuView().isPrepared()) {
            resolveDanmakuSeek(this, time);
        } else if (mHadPlay && getDanmakuView() != null && !getDanmakuView().isPrepared()) {
            //如果没有初始化过的，记录位置等待
            setDanmakuStartSeekPosition(time);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
//        switch (v.getId()) {
//            case R.id.send_danmaku:
//                addDanmaku(true);
//                break;
//            case R.id.toogle_danmaku:
//                mDanmaKuShow = !mDanmaKuShow;
//                resolveDanmakuShow();
//                break;
//        }
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        ((DanmakuJellyfinGSYVideoPlayer) to).baseItemDto = ((DanmakuJellyfinGSYVideoPlayer) from).baseItemDto;
        super.cloneParams(from, to);
    }

    /**
     处理播放器在全屏切换时，弹幕显示的逻辑
     需要格外注意的是，因为全屏和小屏，是切换了播放器，所以需要同步之间的弹幕状态
     */
    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            DanmakuJellyfinGSYVideoPlayer gsyVideoPlayer = (DanmakuJellyfinGSYVideoPlayer) gsyBaseVideoPlayer;
            //对弹幕设置偏移记录
            gsyVideoPlayer.setDanmakuStartSeekPosition(getCurrentPositionWhenPlaying());
            gsyVideoPlayer.setDanmaKuShow(getDanmaKuShow());
            onPrepareDanmaku(gsyVideoPlayer);
        }
        return gsyBaseVideoPlayer;
    }

    /**
     处理播放器在退出全屏时，弹幕显示的逻辑
     需要格外注意的是，因为全屏和小屏，是切换了播放器，所以需要同步之间的弹幕状态
     */
    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            DanmakuJellyfinGSYVideoPlayer gsyDanmaVideoPlayer = (DanmakuJellyfinGSYVideoPlayer) gsyVideoPlayer;
            setDanmaKuShow(gsyDanmaVideoPlayer.getDanmaKuShow());
            if (gsyDanmaVideoPlayer.getDanmakuView() != null &&
                    gsyDanmaVideoPlayer.getDanmakuView().isPrepared()) {
                resolveDanmakuSeek(this, gsyDanmaVideoPlayer.getCurrentPositionWhenPlaying());
                resolveDanmakuShow();
                releaseDanmaku(gsyDanmaVideoPlayer);
            }
        }
    }

    protected void danmakuOnPause() {
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    protected void danmakuOnResume() {
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    public void setDanmaKuStream(File is) {
        if (!getDanmakuView().isPrepared()) {
            onPrepareDanmaku((DanmakuJellyfinGSYVideoPlayer) getCurrentPlayer());
        }
    }

    private void initDanmaku() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        DanamakuAdapter danamakuAdapter = new DanamakuAdapter(mDanmakuView);
        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
                .setCacheStuffer(new SpannedCacheStuffer(), danamakuAdapter) // 图文混排使用SpannedCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
        if (mDanmakuView != null) {
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void prepared() {
                    if (getDanmakuView() != null) {
                        getDanmakuView().start();
                        if (getDanmakuStartSeekPosition() != -1) {
                            resolveDanmakuSeek(DanmakuJellyfinGSYVideoPlayer.this, getDanmakuStartSeekPosition());
                            setDanmakuStartSeekPosition(-1);
                        }
                        resolveDanmakuShow();
                    }
                }
            });
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
    }

    private InputStream getIsStream() {
        if (baseItemDto == null) {
            return null;
        }

        Response<ByteReadChannel> danmuXmlFileById = danmuApi.getDanmuXmlFileById(baseItemDto.getId(), new HashSet<>());
        ByteReadChannel content = danmuXmlFileById.getContent();
        return new ByteReadChannelInputStream(content);
    }

    /**
     弹幕的显示与关闭
     */
    private void resolveDanmakuShow() {
//        post(new Runnable() {
//            @Override
//            public void run() {
//                if (mDanmaKuShow) {
//                    if (!getDanmakuView().isShown())
//                        getDanmakuView().show();
//                    mToogleDanmaku.setText("弹幕关");
//                } else {
//                    if (getDanmakuView().isShown()) {
//                        getDanmakuView().hide();
//                    }
//                    mToogleDanmaku.setText("弹幕开");
//                }
//            }
//        });
    }

    /**
     开始播放弹幕
     */
    private void onPrepareDanmaku(DanmakuJellyfinGSYVideoPlayer gsyVideoPlayer) {
        if (gsyVideoPlayer.getDanmakuView() != null && !gsyVideoPlayer.getDanmakuView().isPrepared() && gsyVideoPlayer.getParser() != null) {
            gsyVideoPlayer.getDanmakuView().prepare(gsyVideoPlayer.getParser(),
                    gsyVideoPlayer.getDanmakuContext());
        }
    }

    /**
     弹幕偏移
     */
    private void resolveDanmakuSeek(DanmakuJellyfinGSYVideoPlayer gsyVideoPlayer, long time) {
        if (mHadPlay && gsyVideoPlayer.getDanmakuView() != null && gsyVideoPlayer.getDanmakuView().isPrepared()) {
            gsyVideoPlayer.getDanmakuView().seekTo(time);
        }
    }

    /**
     创建解析器对象，解析输入流

     @param stream
     @return
     */
    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }

    /**
     释放弹幕控件
     */
    private void releaseDanmaku(DanmakuJellyfinGSYVideoPlayer danmakuJellyfinGSYVideoPlayer) {
        if (danmakuJellyfinGSYVideoPlayer != null && danmakuJellyfinGSYVideoPlayer.getDanmakuView() != null) {
            Debuger.printfError("release Danmaku!");
            danmakuJellyfinGSYVideoPlayer.getDanmakuView().release();
        }
    }

    public BaseDanmakuParser getParser() {
        if (mParser == null) {
            mParser = createParser(getIsStream());
        }
        return mParser;
    }

    public DanmakuContext getDanmakuContext() {
        return mDanmakuContext;
    }

    public IDanmakuView getDanmakuView() {
        return mDanmakuView;
    }

    public long getDanmakuStartSeekPosition() {
        return mDanmakuStartSeekPosition;
    }

    public void setDanmakuStartSeekPosition(long danmakuStartSeekPosition) {
        this.mDanmakuStartSeekPosition = danmakuStartSeekPosition;
    }

    public void setDanmaKuShow(boolean danmaKuShow) {
        mDanmaKuShow = danmaKuShow;
    }

    public boolean getDanmaKuShow() {
        return mDanmaKuShow;
    }

    /**
     模拟添加弹幕数据
     */
    private void addDanmaku(boolean islive) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.text = "这是一条弹幕 " + getCurrentPositionWhenPlaying();
        danmaku.padding = 5;
        danmaku.priority = 8;  // 可能会被各种过滤器过滤并隐藏显示，所以提高等级
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 500);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        danmaku.borderColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    //处理按键快进和快退
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Timber.d("点击了按钮 keyCode=%s, event=%s", keyCode, event.getAction());
        switch (keyCode) {
            case KeyEvent.ACTION_DOWN:
                //TODO:目前暂未有逻辑
                break;
            case KeyEvent.ACTION_UP:
                //TODO:目前暂未有逻辑
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                onClickUi();
                firstKeyDown();
                mHandler.sendEmptyMessage(LEFT);
                mHandler.sendEmptyMessageDelayed(CANCLE, 1500);
                resetTime();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                onClickUi();
                firstKeyDown();
                mHandler.sendEmptyMessage(RIGHT);
                mHandler.sendEmptyMessageDelayed(CANCLE, 1500);
                resetTime();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                int state = getCurrentState();
                switch (state) {
                    case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                        onVideoPause();
                        break;
                    case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                        onVideoResume();
                        break;
                    case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                        startPlayLogic();
                        isPlayComplete = false;
                        mSeekTimePosition = 0;
                        mProgressBar.setProgress(0);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    //点击屏幕的默认值：屏幕中心点X,Y数值
    private static int pointX;
    private static int pointY;
    private static int moveX;
    private static int moveY;

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private static void getDisplay(Context context) {
        //获得屏幕宽高
        Point size = getDisplaySize(context);
        pointX = size.x / 2;
        pointY = size.y / 2;
        moveX = pointX;
        moveY = pointY;
    }

    private static boolean firstKeyDown = true;

    private Handler mHandler;
    private void initHandler(){
        if(mHandler == null){
            mHandler  =  new TvVideoHandler();
        }
    }

    private void onClickUi() {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            onClickUiToggle(null);
            startDismissControlViewTimer();
//                    return true;
        }
    }

    //第一次按下左右键
    private void firstKeyDown() {
        if (firstKeyDown) {
            touchSurfaceDown(pointX, pointY);
            firstKeyDown = false;
            if (mSeekTimePosition >= getDuration() || isPlayComplete) {
                //TODO : 暂未有逻辑
            } else {
                onStartTrackingTouch(mProgressBar);
            }
        }
    }

    public static boolean isPlayComplete = false;

    public static void setPlayComplete(boolean PlayComplete) {
        isPlayComplete = PlayComplete;
    }

    //连续按下左右键
    private void keyMove() {
        if ((mIfCurrentIsFullscreen && mIsTouchWigetFull)
                || (mIsTouchWiget && !mIfCurrentIsFullscreen)) {
            if (!mChangePosition && !mChangeVolume && !mBrightness) {
                touchSurfaceMoveFullLogic(Math.abs(moveX - pointX), 0);
            }
        }
        if (mSeekTimePosition >= getDuration() || isPlayComplete) {
            mHandler.sendEmptyMessageDelayed(CANCLE, 1500);
            mBottomContainer.setVisibility(GONE);
        }  else {
            mChangePosition = true;
            touchSurfaceMove(moveX - pointX, 0, pointY);
            mBottomContainer.setVisibility(VISIBLE);
            onProgressChanged(mProgressBar, (int) (mSeekTimePosition * 100 / getDuration()), true);
        }
    }

    //定义变量
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int CANCLE = 2;

    private static int tim = 2;

    //程序启动时，初始化并发送消息
    private final class TvVideoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LEFT:
                    if (tim > 0) {
                        if (tim > 1) {
                            moveX = moveX - pointX / 13;
                        } else {
                            moveX = moveX - pointX / 20;
                        }
                        tim -= 1;
                    } else {
                        moveX = moveX - pointX / 200;
                    }
                    keyMove();
                    break;
                case RIGHT:
                    if (tim > 0) {
                        if (tim > 1) {
                            moveX = moveX + pointX / 13;//13-20
                        } else {
                            moveX = moveX + pointX / 20;
                        }
                        tim -= 1;
                    } else {
                        moveX = moveX + pointX / 200;
                    }
                    keyMove();
                    break;
                case CANCLE:                        //停止按键
                    firstKeyDown = true;
                    moveX = pointX;
                    firstKeyDown = true;
                    tim = 2;
                    onStopTrackingTouch(mProgressBar);
                    mBottomContainer.setVisibility(GONE);
                    startDismissControlViewTimer();
                    touchSurfaceUp();
                    startProgressTimer();
                    //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                    if (mHideKey && mShowVKey) {
//                        return true;
                    }
                    break;
            }
        }
    }

    ;

    //重置
    public void resetTime() {
        mHandler.removeMessages(CANCLE);
        mHandler.sendEmptyMessageDelayed(CANCLE, 1500);
    }

    @Override
    protected void dismissProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
