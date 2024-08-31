package org.jellyfin.androidtv.customer.gsy.ui;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import org.jellyfin.androidtv.R;

public abstract class ActionButton extends AppCompatImageButton {
    protected Context context;

    public ActionButton(@NonNull Context context) {
        super(context, null, R.style.Button_Icon);


        this.context = context;
        setFocusableInTouchMode(true);
        setVisibility(VISIBLE);

        setOnClickListener(v -> onclick(context, v));
    }

    public ActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//
//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, R.styleable.ImageView, defStyleAttr, defStyleRes);
//        saveAttributeDataForStyleable(context, R.styleable.ImageView,
//                attrs, a, defStyleAttr, defStyleRes);
    }

    /**
     * 监听点击事件
     * @param context 上下文
     * @param v 当前view
     */
    protected abstract void onclick(Context context, View v);

    protected void initializeWithIcon(@DrawableRes int resourceId) {
        Drawable imageDrawable = ContextCompat.getDrawable(context, resourceId);
        initializeWithIcon(imageDrawable);

//        setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                getBackground().setAlpha(255);
//            } else {
//                getBackground().setAlpha(0);
//            }
//        });
    }

    protected void initializeWithIcon(Drawable drawable) {
//        TextUnderButton.create(context, R.drawable.ic_resume, buttonSize, 2, buttonLabel, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FullDetailsFragmentHelperKt.resumePlayback(FullDetailsFragment.this);
//            }
//        });



        setImageDrawable(drawable);


//        setMaxHeight();
        setAdjustViewBounds(true);

//        GradientDrawable backgroundDrawable = new GradientDrawable();
//        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
//        backgroundDrawable.setColor(0xCC4b4b4b);
//        backgroundDrawable.setCornerRadius(20); // 设置圆角半径
//        setBackground(backgroundDrawable);
    }
//
//    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr){
//
//        TypedArray typedArray = context.obtainStyledAttributes(attrs,-1, defStyleAttr, R.style.Button_Icon);
//        try {
//            int defaultDrawableResId = typedArray.getResourceId(R.styleable.CustomButton_drawable_default, -1);
//            int pressedDrawableResId = typedArray.getResourceId(R.styleable.CustomButton_drawable_pressed, -1);
//
//            StateListDrawable stateListDrawable = new StateListDrawable();
//            if(pressedDrawableResId!=-1) {
//                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(context, pressedDrawableResId));
//            }
//            if(defaultDrawableResId!=-1) {
//                stateListDrawable.addState(new int[]{}, ContextCompat.getDrawable(context, defaultDrawableResId));
//            }
//            setBackground(stateListDrawable);
//        }
//        catch (Exception e){
//        }
//        finally {
//            typedArray.recycle();
//        }
//    }
}
