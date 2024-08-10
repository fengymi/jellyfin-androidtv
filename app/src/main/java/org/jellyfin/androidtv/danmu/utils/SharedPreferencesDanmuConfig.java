package org.jellyfin.androidtv.danmu.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.DanmuApiOption;

import org.jellyfin.apiclient.serialization.GsonJsonSerializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

public class SharedPreferencesDanmuConfig implements DanmuConfigGetter, DanmuConfigChangeHandler {
    private static final String DANMU_SETTING = "fengymi_danmu_setting";

    public static final String OPEN_KEY = "open";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String SPEED_KEY = "speed";
    public static final String POSITION_KEY = "position";
    public static final String APIS_KEY = "apis";
    public static final String VIDEO_SPEED_KEY = "videoSpeed";

    private static final int DEFAULT_FONT_SIZE = 30;
    private static final int DEFAULT_SPEED = 9;
    private static final int DEFAULT_POSITION = 3;
    private static final float DEFAULT_VIDEO_SPEED = 1.0f;

    private final Context context;
    private final GsonJsonSerializer gsonJsonSerializer;

    private List<DanmuApiOption> danmuApiList;

    /**
     * 是否打开
     */
    private boolean open;

    /**
     * 字体大小
     */
    private int fontSize;

    /**
     * 弹幕位置
     * 3-上屏, 2-半屏, 1-全屏
     */
    private int position;

    private int speed;

    /**
     * 视频播放速度
     */
    private float videoSpeed;

    /**
     * debug模式
     */
    private boolean debug;

    public SharedPreferencesDanmuConfig(Context context) {
        this.context = context;
        this.gsonJsonSerializer = new GsonJsonSerializer();

        SharedPreferences sharedPreferences = getSharedPreferences(context);
        this.open = sharedPreferences.getBoolean(OPEN_KEY, false);
        this.fontSize = sharedPreferences.getInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        this.speed = sharedPreferences.getInt(SPEED_KEY, DEFAULT_SPEED);
        this.position = sharedPreferences.getInt(POSITION_KEY, DEFAULT_POSITION);
        this.videoSpeed = sharedPreferences.getFloat(VIDEO_SPEED_KEY, DEFAULT_VIDEO_SPEED);
        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(APIS_KEY, "[]"));
            List<DanmuApiOption> danmuApiList = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject danmuApiOptionJSON = jsonArray.getJSONObject(i);

                DanmuApiOption danmuApiOption = new DanmuApiOption();
                danmuApiOption.setOpened(danmuApiOptionJSON.getBoolean("opened"));
                danmuApiOption.setSource("source");
                danmuApiOption.setSourceName("sourceName");
                danmuApiList.add(danmuApiOption);
            }

            this.danmuApiList = danmuApiList;
        } catch (Exception e) {
            Timber.e(e, "加载弹幕来源配置失败");
            this.danmuApiList = new ArrayList<>();
        }
    }

    @Override
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        saveOneSetting(FONT_SIZE_KEY, fontSize);
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
        saveOneSetting(SPEED_KEY, speed);
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
        saveOneSetting(POSITION_KEY, position);
    }

    @Override
    public void setOpen(boolean open) {
        this.open = open;
        saveOneSetting(OPEN_KEY, open);
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public float getVideoSpeed() {
        return videoSpeed;
    }

    public void setVideoSpeed(float videoSpeed) {
        this.videoSpeed = videoSpeed;
        saveOneSetting(VIDEO_SPEED_KEY, videoSpeed);
    }

    @Override
    public List<DanmuApiOption> getDanmuApiList() {
        return this.danmuApiList;
    }

    public void setDanmuApiList(List<DanmuApiOption> danmuApiList) {
        this.danmuApiList = danmuApiList;
        saveOneSetting(APIS_KEY, danmuApiList);
    }

    @Override
    public Set<String> getAllOpenSites() {
        if (this.danmuApiList == null || this.danmuApiList.isEmpty()) {
            return Collections.emptySet();
        }
        return danmuApiList
                .stream()
                .filter(DanmuApiOption::isOpened)
                .map(DanmuApiOption::getSource)
                .collect(Collectors.toSet());
    }

    @Override
    public int getSpeed() {
        return (int) (speed * videoSpeed);
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    protected SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(DANMU_SETTING, Context.MODE_PRIVATE);
    }

    /**
     * 保存单个配置
     * @param key 配置key
     * @param value 保存结果
     */
    protected void saveOneSetting(String key, Object value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (value instanceof Boolean) {
            edit.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            edit.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            edit.putString(key, (String) value);
        } else if (value instanceof Float) {
            edit.putFloat(key, (Float) value);
        } else if (value instanceof Collection) {
            edit.putString(key, gsonJsonSerializer.SerializeToString(value));
        }
        edit.apply();
        Timber.d("saveOneSetting = %s: %s", key, gsonJsonSerializer.SerializeToString(value));
    }

    @Override
    public String toString() {
        return "SharedPreferencesDanmuConfig{" +
                "danmuApiList=" + danmuApiList +
                ", open=" + open +
                ", fontSize=" + fontSize +
                ", position=" + position +
                ", speed=" + speed +
                ", videoSpeed=" + videoSpeed +
                ", fps=" + getFps() +
                ", debug=" + debug +
                '}';
    }
}
