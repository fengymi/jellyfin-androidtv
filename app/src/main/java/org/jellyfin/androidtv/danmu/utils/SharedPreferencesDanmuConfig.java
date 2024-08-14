package org.jellyfin.androidtv.danmu.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.core.config.DanmuConfigGetter;
import com.tv.fengymi.danmu.model.DanmuApiOption;

import org.jellyfin.androidtv.danmu.model.AutoSkipModel;
import org.jellyfin.apiclient.serialization.GsonJsonSerializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

public class SharedPreferencesDanmuConfig implements DanmuConfigGetter, DanmuConfigChangeHandler {
    private static final String DANMU_SETTING = "fengymi_danmu_setting";
    private static final long AUTO_SKIP_EXPIRE_TIME = 6 * 30 * 24 * 3600 * 1000L;

    public static final String OPEN_KEY = "open";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String SPEED_KEY = "speed";
    public static final String POSITION_KEY = "position";
    public static final String APIS_KEY = "apis";
    public static final String VIDEO_SPEED_KEY = "videoSpeed";

    public static final String AUTO_SKIP_TIMES = "autoSkipTime";

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

    private Map<String, AutoSkipModel> itemAutoSkipTimes;

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

        try {
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(AUTO_SKIP_TIMES, "[]"));
            Map<String, AutoSkipModel> autoSkipModelHashMap = new HashMap<>();
            long expireTime = System.currentTimeMillis() - AUTO_SKIP_EXPIRE_TIME;
            boolean needDelete = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject autoSkipJSON = jsonArray.getJSONObject(i);
                long cTime = autoSkipJSON.getLong("cTime");
                if (cTime < expireTime || autoSkipJSON.isNull("id")) {
                    needDelete = true;
                    continue;
                }

                AutoSkipModel autoSkipModel = new AutoSkipModel();
                autoSkipModel.setTsTime(autoSkipJSON.getInt("tsTime"));
                autoSkipModel.setTeTime(autoSkipJSON.getInt("teTime"));
                autoSkipModel.setWsTime(autoSkipJSON.getInt("wsTime"));
                autoSkipModel.setWeTime(autoSkipJSON.getInt("weTime"));
                autoSkipModel.setcTime(autoSkipJSON.getLong("cTime"));
                autoSkipModel.setId(autoSkipJSON.getString("id"));
                autoSkipModelHashMap.put(autoSkipModel.getId(), autoSkipModel);
            }

            this.itemAutoSkipTimes = autoSkipModelHashMap;
            if (needDelete) {
                updateAutoSkipModels();
            }
        } catch (Exception e) {
            Timber.e(e, "加载片头片尾缓存记录失败");
            this.itemAutoSkipTimes = new HashMap<>();
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

    public AutoSkipModel getAutoSkipModel(String id) {
        return itemAutoSkipTimes.get(id);
    }

    public void addAutoSkipModel(AutoSkipModel autoSkipModel) {
        itemAutoSkipTimes.put(autoSkipModel.getId(), autoSkipModel);
        updateAutoSkipModels();
    }

    private void updateAutoSkipModels() {
        saveOneSetting(AUTO_SKIP_TIMES, itemAutoSkipTimes.values());
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
        return debug;
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
