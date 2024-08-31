package org.jellyfin.androidtv.customer.gsy.shade.jellyfin;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.customer.gsy.shade.EmptyAnimationListener;
import org.jellyfin.androidtv.ui.AsyncImageView;
import org.jellyfin.androidtv.ui.ClockUserView;
import org.jellyfin.androidtv.util.ImageHelper;
import org.jellyfin.androidtv.util.sdk.BaseItemExtensionsKt;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.jellyfin.sdk.model.api.BaseItemKind;
import org.koin.java.KoinJavaComponent;

public class TopPanelVideoPlayerShade implements JellyfinVideoPlayerShade {
    protected ViewGroup viewGroup;
    protected ImageHelper imageHelper;

    private final View topPanel;
    private final TextView mItemTitle;
    private final TextView mItemSubtitle;
    private final AsyncImageView mItemLogo;
    private boolean mIsVisible;

    private final Animation fadeHide;
    private final Animation fadeShow;

    protected ClockUserView mTextClock;

    public TopPanelVideoPlayerShade(ViewGroup viewGroup) {
        this.imageHelper = KoinJavaComponent.get(ImageHelper.class);
        this.viewGroup = viewGroup;
        this.topPanel = viewGroup.findViewById(R.id.layout_top);

        // 标题部分
        this.mItemTitle = viewGroup.findViewById(R.id.item_title);
        this.mItemSubtitle = viewGroup.findViewById(R.id.item_subtitle);
        this.mItemLogo = viewGroup.findViewById(R.id.item_logo);

        this.mTextClock = viewGroup.findViewById(R.id.textClock);

        fadeShow = AnimationUtils.loadAnimation(viewGroup.getContext(), androidx.leanback.R.anim.abc_slide_in_top);
        fadeShow.setAnimationListener(new EmptyAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                topPanel.setVisibility(View.VISIBLE);
            }

        });
        fadeHide = AnimationUtils.loadAnimation(viewGroup.getContext(), androidx.leanback.R.anim.abc_fade_out);
        fadeHide.setAnimationListener(new EmptyAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                topPanel.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public void show() {
        if (mIsVisible) {
            return;
        }

        topPanel.startAnimation(fadeShow);
        mIsVisible = true;
    }

    @Override
    public void hide() {
        if (!mIsVisible) {
            return;
        }

        mIsVisible = false;
        topPanel.startAnimation(fadeHide);
    }

    @Override
    public void updateDisplay(BaseItemDto item, boolean needShow) {
        if (item == null || viewGroup == null) {
            return;
        }

        if (item.getType() == BaseItemKind.EPISODE) {
            this.mItemTitle.setText(item.getSeriesName());
            this.mItemSubtitle.setText(BaseItemExtensionsKt.getDisplayName(item, viewGroup.getContext()));
        } else {
            this.mItemTitle.setText(item.getName());
        }
        // Update the logo
        String imageUrl = imageHelper.getLogoImageUrl(item, 440, false);
        if (imageUrl != null) {
            mItemLogo.setVisibility(View.VISIBLE);
            mItemTitle.setVisibility(View.GONE);
            mItemLogo.setContentDescription(item.getName());
            mItemLogo.load(imageUrl, null, null, 1.0, 0);
        } else {
            mItemLogo.setVisibility(View.GONE);
            mItemTitle.setVisibility(View.VISIBLE);
        }

        if (needShow) {
            show();
        }
    }
}
