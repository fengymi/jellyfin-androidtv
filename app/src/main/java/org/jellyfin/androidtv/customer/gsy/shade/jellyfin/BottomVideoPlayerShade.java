package org.jellyfin.androidtv.customer.gsy.shade.jellyfin;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.gsy.JellyfinGSYVideoPlayer;
import org.jellyfin.androidtv.customer.gsy.shade.EmptyAnimationListener;
import org.jellyfin.androidtv.customer.gsy.ui.ActionLayout;
import org.jellyfin.sdk.model.api.BaseItemDto;

public class BottomVideoPlayerShade implements JellyfinVideoPlayerShade {
    private final LinearLayout layoutBottom;
    protected ViewGroup viewGroup;
    protected ActionLayout actionGroups;
    protected JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer;

    private final Animation fadeHide;
    private final Animation fadeShow;

    protected boolean visibility = true;

    public BottomVideoPlayerShade(ViewGroup viewGroup, JellyfinGSYVideoPlayer jellyfinGSYVideoPlayer) {
        this.viewGroup = viewGroup;
        this.jellyfinGSYVideoPlayer = jellyfinGSYVideoPlayer;

        layoutBottom = viewGroup.findViewById(R.id.layout_bottom);
        fadeShow = AnimationUtils.loadAnimation(viewGroup.getContext(), androidx.leanback.R.anim.abc_slide_in_bottom);
        fadeShow.setAnimationListener(new EmptyAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutBottom.setVisibility(View.VISIBLE);
            }

        });
        fadeHide = AnimationUtils.loadAnimation(viewGroup.getContext(), androidx.leanback.R.anim.abc_fade_out);
        fadeHide.setAnimationListener(new EmptyAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                layoutBottom.setVisibility(View.GONE);
            }
        });

        show();

        actionGroups = viewGroup.findViewById(R.id.primaryActions);
        actionGroups.init(jellyfinGSYVideoPlayer);
        initActions(viewGroup);
    }

    protected void initActions(ViewGroup viewGroup) {
//        this.playAction = new PlayAction(viewGroup.getContext(), jellyfinGSYVideoPlayer);
//
//        this.actionGroups.addView(playAction);
    }

    @Override
    public void updateDisplay(BaseItemDto item, boolean needShow) {

    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void show() {
        if (visibility) {
            return;
        }

        this.visibility = true;
        this.actionGroups.show();
        this.layoutBottom.startAnimation(fadeShow);
    }

    @Override
    public void hide() {
        if (!visibility) {
            return;
        }

        this.visibility = false;
        layoutBottom.startAnimation(fadeHide);
    }
}
