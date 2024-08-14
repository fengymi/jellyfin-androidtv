package org.jellyfin.androidtv.danmu.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.jellyfin.apiclient.serialization.GsonJsonSerializer;

public class SimpleDanmuUtil {
    private static volatile Handler mainThreadHandler;
    public static GsonJsonSerializer gsonJsonSerializer = new GsonJsonSerializer();

    public static String toJsonString(Object object) {
        return gsonJsonSerializer.SerializeToString(object);
    }

    public static void show(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String message, int time) {
        getMainHandler().post(() -> Toast.makeText(context, message, time).show());
    }

    public static Handler getMainHandler() {
        if (SimpleDanmuUtil.mainThreadHandler == null) {
            synchronized (SimpleDanmuUtil.class) {
                if (SimpleDanmuUtil.mainThreadHandler == null) {
                    SimpleDanmuUtil.mainThreadHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return SimpleDanmuUtil.mainThreadHandler;
    }
}
