package org.jellyfin.androidtv.customer.gsy.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.gsy.JellyfinGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ActionLayout extends LinearLayout {
    private ImageButton playerPre;
    private ImageButton playerStart;
    private ImageButton playerNext;
    private ImageButton playerSetting;

    private List<View> actions;

    protected JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer;

    public ActionLayout(Context context) {
        super(context);
    }

    public ActionLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ActionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(@NonNull JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer) {
        this.jellyfinGSYVideoPlayer = jellyfinGSYVideoPlayer;
        playerPre = findViewById(R.id.ic_player_pre);
        playerPre.setOnClickListener(v -> jellyfinGSYVideoPlayer.playPre());

        playerStart = findViewById(R.id.ic_player_start);
        playerStart.setOnClickListener(v -> {
            if (jellyfinGSYVideoPlayer.isPlaying()) {
                jellyfinGSYVideoPlayer.onVideoPause();
            } else {
                jellyfinGSYVideoPlayer.onVideoResume();
            }
        });

        playerNext = findViewById(R.id.ic_player_next);
        playerNext.setOnClickListener(v -> jellyfinGSYVideoPlayer.playNext());

        playerSetting = findViewById(R.id.ic_player_settings);
        playerSetting.setOnClickListener(v -> Toast.makeText(getContext(), "测试", Toast.LENGTH_SHORT));

        actions = new ArrayList<>();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        Timber.d("gainFocus=%s, direction=%s", gainFocus, direction);
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    public void show() {
        initShowActions();
        View firstView = actions.get(0);
        View lastView = actions.get(actions.size() - 1);

        firstView.setNextFocusLeftId(lastView.getId());
        lastView.setNextFocusRightId(firstView.getId());

        firstView.requestFocus();
        this.setVisibility(VISIBLE);
    }

    protected void initShowActions() {
        removeAllViews();
        actions.clear();

        if (jellyfinGSYVideoPlayer.hasPreItem()) {
            playerPre.setVisibility(VISIBLE);
            actions.add(playerPre);
        }
        actions.add(playerStart);
        if (jellyfinGSYVideoPlayer.hasNextItem()) {
            playerNext.setVisibility(VISIBLE);
            actions.add(playerNext);
        }
        actions.add(playerSetting);

        for (View action : actions) {
            addView(action);
        }
    }
//
//    @Override
//    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
//        if (this.playerPre != null && this.playerPre.requestFocus()) {
//            return true;
//        }
//        return super.requestFocus(direction, previouslyFocusedRect);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (VISIBLE != getVisibility()) {
            return false;
        }

//        if (hasFocus() && handleKeyEvent(event)) {
//            return true;
//        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
                // 处理遥控器按键事件，例如在按钮之间切换焦点或执行相应操作
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (hasFocus() && handleKeyEvent(event)) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (hasFocus() && handleKeyEvent(event)) {
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }
}
