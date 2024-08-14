package org.jellyfin.androidtv.danmu.ui.action;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.danmu.model.AutoSkipModel;
import org.jellyfin.androidtv.danmu.ui.playback.DanmuPlaybackController;
import org.jellyfin.androidtv.danmu.utils.SharedPreferencesDanmuConfig;
import org.jellyfin.androidtv.danmu.utils.SimpleDanmuUtil;
import org.jellyfin.androidtv.ui.playback.PlaybackController;
import org.jellyfin.androidtv.ui.playback.overlay.CustomPlaybackTransportControlGlue;
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerAdapter;
import org.jellyfin.androidtv.ui.playback.overlay.action.CustomAction;
import org.jellyfin.sdk.model.api.BaseItemDto;
import org.koin.java.KoinJavaComponent;

import java.util.UUID;

/**
 * 追剧按钮
 */
public class BingeWatchingAction extends CustomAction {
    private final SharedPreferencesDanmuConfig danmuSetting;

    public BingeWatchingAction(@NonNull Context context, @NonNull CustomPlaybackTransportControlGlue customPlaybackTransportControlGlue) {
        super(context, customPlaybackTransportControlGlue);

        this.danmuSetting = KoinJavaComponent.get(SharedPreferencesDanmuConfig.class);
        initializeWithIcon(R.drawable.ic_settings);
    }

    @Override
    public void handleClickAction(@NonNull PlaybackController playbackController, @NonNull VideoPlayerAdapter videoPlayerAdapter, @NonNull Context context, @NonNull View view) {
        View settingArea = view.getRootView().findViewById(R.id.playbackViewExtraSetting);
        if (settingArea == null) {
            return;
        }

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        context.getDisplay().getMetrics(displayMetrics);
        Button cancelButton = settingArea.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(v -> settingArea.setVisibility(View.INVISIBLE));

        Button confirmButton = settingArea.findViewById(R.id.confirm);
        confirmButton.setOnClickListener(v -> {
            if (!(playbackController instanceof DanmuPlaybackController)) {
                return;
            }

            BaseItemDto currentlyPlayingItem = playbackController.getCurrentlyPlayingItem();
            UUID seasonId = currentlyPlayingItem.getSeasonId();
            if (seasonId == null) {
                seasonId = currentlyPlayingItem.getId();
            }

            AutoSkipModel autoSkipModel = new AutoSkipModel();
            autoSkipModel.setId(seasonId.toString());
            autoSkipModel.setTsTime(getTime(settingArea, R.id.piantou_start_mi, R.id.piantou_start_ss));
            autoSkipModel.setTeTime(getTime(settingArea, R.id.piantou_end_mi, R.id.piantou_end_ss));
            autoSkipModel.setWsTime(getTime(settingArea, R.id.pianwei_start_mi, R.id.pianwei_start_ss));
            autoSkipModel.setWeTime(getTime(settingArea, R.id.pianwei_end_mi, R.id.pianwei_end_ss));

//            String paintouTime = "";
//            if (autoSkipModel.getTsTime() > 0 || autoSkipModel.getTeTime() > 0) {
//                paintouTime += "片头时间: ";
//                paintouTime += autoSkipModel.getTsTime() / 60 + ":" + autoSkipModel.getTsTime() % 60;
//            }
//
//            String pianweiTime = "";
//            if (autoSkipModel.getWsTime() > 0 || autoSkipModel.getWeTime() > 0) {
//                pianweiTime += "片尾时间: ";
//                pianweiTime += autoSkipModel.getWsTime() / 60 + ":" + autoSkipModel.getWsTime() % 60;
//            }

            SimpleDanmuUtil.show(context, "设置成功");
            ((DanmuPlaybackController) playbackController).setAutoSkipModel(autoSkipModel);
            danmuSetting.addAutoSkipModel(autoSkipModel);
            settingArea.setVisibility(View.INVISIBLE);
        });

        BaseItemDto currentlyPlayingItem = playbackController.getCurrentlyPlayingItem();
        UUID seasonId = currentlyPlayingItem.getSeasonId();
        if (seasonId == null) {
            seasonId = currentlyPlayingItem.getId();
        }
        AutoSkipModel autoSkipModel = danmuSetting.getAutoSkipModel(seasonId.toString());
        if (autoSkipModel != null) {
            if (autoSkipModel.getTsTime() > 0) {
                String[] miSs = getMiSs(autoSkipModel.getTsTime());
                ((EditText) settingArea.findViewById(R.id.piantou_start_mi)).setText(miSs[0]);
                ((EditText) settingArea.findViewById(R.id.piantou_start_ss)).setText(miSs[1]);
            }
            if (autoSkipModel.getTeTime() > 0) {
                String[] miSs = getMiSs(autoSkipModel.getTeTime());
                ((EditText) settingArea.findViewById(R.id.piantou_end_mi)).setText(miSs[0]);
                ((EditText) settingArea.findViewById(R.id.piantou_end_ss)).setText(miSs[1]);
            }
            if (autoSkipModel.getWsTime() > 0) {
                String[] miSs = getMiSs(autoSkipModel.getWsTime());
                ((EditText) settingArea.findViewById(R.id.pianwei_start_mi)).setText(miSs[0]);
                ((EditText) settingArea.findViewById(R.id.pianwei_start_ss)).setText(miSs[1]);
            }
            if (autoSkipModel.getWeTime() > 0) {
                String[] miSs = getMiSs(autoSkipModel.getWeTime());
                ((EditText) settingArea.findViewById(R.id.pianwei_end_mi)).setText(miSs[0]);
                ((EditText) settingArea.findViewById(R.id.pianwei_end_ss)).setText(miSs[1]);
            }
        }

        settingArea.setVisibility(View.VISIBLE);
        settingArea.findViewById(R.id.piantou_end_mi).requestFocus();
    }

    private int getTime(View settingArea, int miId, int ssId) {
        String mi = ((EditText) settingArea.findViewById(miId)).getText().toString();
        String ss = ((EditText) settingArea.findViewById(ssId)).getText().toString();

        int time = 0;
        if (!mi.trim().isEmpty()) {
            time += Integer.parseInt(mi.trim()) * 60;
        }

        if (!ss.trim().isEmpty()) {
            time += Integer.parseInt(ss.trim());
        }
        return time;
    }

    private String[] getMiSs(int time) {
        return new String[]{String.format("%02d", time / 60), String.format("%02d", time % 60)};
    }
}
