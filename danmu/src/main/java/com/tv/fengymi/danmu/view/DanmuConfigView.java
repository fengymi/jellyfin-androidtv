package com.tv.fengymi.danmu.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import timber.log.Timber;

public class DanmuConfigView extends View {
    public DanmuConfigView(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK != keyCode) {
            return true;
        }

        Timber.d("返回键点击了");
        return false;
    }
}
