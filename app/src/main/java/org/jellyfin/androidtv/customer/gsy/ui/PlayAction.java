package org.jellyfin.androidtv.customer.gsy.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.gsy.JellyfinGSYVideoPlayer;

@SuppressLint("ViewConstructor")
public class PlayAction extends ActionButton {
    private final JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer;

    public PlayAction(@NonNull Context context, @NonNull JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer) {
        super(context);
        this.jellyfinGSYVideoPlayer = jellyfinGSYVideoPlayer;
        initializeWithIcon(R.drawable.ic_play);
    }

    @Override
    protected void onclick(Context context, View v) {
        Toast.makeText(context, "text", Toast.LENGTH_SHORT).show();

        if (!jellyfinGSYVideoPlayer.isInPlayingState()) {
            return;
        }

        if (jellyfinGSYVideoPlayer.getCurrentState() == GSYVideoView.CURRENT_STATE_PAUSE) {
            jellyfinGSYVideoPlayer.onVideoResume();
        } else {
            jellyfinGSYVideoPlayer.onVideoPause();
        }
    }
}
