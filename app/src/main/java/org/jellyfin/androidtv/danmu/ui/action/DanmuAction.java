package org.jellyfin.androidtv.danmu.ui.action;

import static org.koin.java.KoinJavaComponent.inject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.leanback.widget.Action;

import com.tv.fengymi.danmu.core.config.DanmuConfigChangeHandler;
import com.tv.fengymi.danmu.model.DanmuApiOption;

import org.jellyfin.androidtv.R;
import org.jellyfin.androidtv.danmu.ui.playback.DanmuPlaybackController;
import org.jellyfin.androidtv.danmu.utils.SharedPreferencesDanmuConfig;
import org.jellyfin.androidtv.ui.playback.PlaybackController;
import org.jellyfin.androidtv.ui.playback.overlay.CustomPlaybackTransportControlGlue;
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerAdapter;
import org.jellyfin.androidtv.ui.playback.overlay.action.CustomAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Lazy;
import timber.log.Timber;

public class DanmuAction extends CustomAction {
    private final Context context;
    private final SharedPreferencesDanmuConfig danmuSetting;
    private final Consumer<Action>  buttonRefresher;
    private DanmuConfigChangeHandler danmuConfigChangeHandler;

    public DanmuAction(@NonNull Context context, @NonNull CustomPlaybackTransportControlGlue customPlaybackTransportControlGlue, Consumer<Action> buttonRefresher, PlaybackController playbackController) {
        super(context, customPlaybackTransportControlGlue);
        this.context = context;

        this.buttonRefresher = buttonRefresher;
        Lazy<SharedPreferencesDanmuConfig> config = inject(SharedPreferencesDanmuConfig.class);
        this.danmuSetting = config.getValue();

        if (playbackController instanceof DanmuPlaybackController) {
            this.danmuConfigChangeHandler = ((DanmuPlaybackController) playbackController).getDanmuConfigChangeHandler();
        }

        initializeWithIcon(danmuSetting.isOpen()?R.drawable.ic_danmu_open:R.drawable.ic_danmu_close);
    }

    private void changeIcon(boolean isOpen) {
        Drawable danmuIcon = ContextCompat.getDrawable(context, isOpen ? R.drawable.ic_danmu_open : R.drawable.ic_danmu_close);
        setIcon(danmuIcon);
        buttonRefresher.accept(this);
    }

    @Override
    public void handleClickAction(@NonNull PlaybackController playbackController, @NonNull VideoPlayerAdapter videoPlayerAdapter, @NonNull Context context, @NonNull View view) {
        Timber.i("DanmuAction 被点击了");

        PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        Menu menu = popupMenu.getMenu();
        int order = 1;
        MenuItem allSwitch = menu.add(1, 1, order ++ , "弹幕开关");
        allSwitch.setCheckable(true);
        allSwitch.setChecked(danmuSetting.isOpen());


        allSwitch.setOnMenuItemClickListener(item -> {
            danmuSetting.setOpen(!danmuSetting.isOpen());

            if (danmuConfigChangeHandler != null) {
                danmuConfigChangeHandler.setOpen(danmuSetting.isOpen());
            }

            changeIcon(danmuSetting.isOpen());
            return true;
        });

        // 字体大小
        SubMenu fontSizeSetting = menu.addSubMenu(2, 2, order++, "字体大小");
        fontSizeSetting.setGroupCheckable(0, true, true);
        int minFontSize = 15;
        int fontSizeGroup = 101;
        MenuItem.OnMenuItemClickListener fontItemOnClickListener = item -> {
            CharSequence title = item.getTitle();
            if (title == null) {
                return true;
            }
            int newFontSize = Integer.parseInt(title.toString());

            danmuSetting.setFontSize(newFontSize);
            if (danmuConfigChangeHandler != null) {
                danmuConfigChangeHandler.setFontSize(newFontSize);
            }
            return true;
        };
        for (int i = 0; i < 7; i++) {
            int fontSize = i * 5 + minFontSize;
            MenuItem fontItem = fontSizeSetting.add(fontSizeGroup, 20 + i, i, String.valueOf(fontSize));
            fontItem.setCheckable(true);
            if (fontSize == danmuSetting.getFontSize()) {
                fontItem.setChecked(true);
            }
            fontItem.setOnMenuItemClickListener(fontItemOnClickListener);
        }

        // 弹幕位置
        SubMenu danmuPosition = menu.addSubMenu(3, 1000, order++, "弹幕位置");
        int positionGroup = 102;
        MenuItem.OnMenuItemClickListener positionItemOnClickListener = item -> {
            CharSequence title = item.getTitle();
            if (title == null) {
                return true;
            }
            int position = "上屏".equals(title.toString()) ? 3 : "半屏".equals(title.toString()) ? 2 : 1;
            danmuSetting.setPosition(position);
            if (danmuConfigChangeHandler != null) {
                danmuConfigChangeHandler.setPosition(position);
            }
            return true;
        };

        String[] positionTitles = new String[]{"全屏", "半屏", "上屏"};
        for (int i = 0; i < positionTitles.length; i++) {
            String positionTitle = positionTitles[i];
            MenuItem positionItem = danmuPosition.add(positionGroup, 1000 + i, i, positionTitle);
            positionItem.setCheckable(true);
            if (i == danmuSetting.getPosition() - 1) {
                positionItem.setChecked(true);
            }
            positionItem.setOnMenuItemClickListener(positionItemOnClickListener);
        }

        // 移动速度
        SubMenu danmuMove = menu.addSubMenu(4, 1100, order++, "速度");
        int moveGroup = 1101;
        MenuItem.OnMenuItemClickListener moveItemOnClickListener = item -> {
            CharSequence title = item.getTitle();
            if (title == null) {
                return true;
            }
            danmuSetting.setSpeed(Integer.parseInt(title.toString()));
            if (danmuConfigChangeHandler != null) {
                danmuConfigChangeHandler.setSpeed(danmuSetting.getSpeed());
            }
            return true;
        };

        int speed = 6;
        for (int i = 0; i < 5; i++) {
            String speedTitle = String.valueOf(speed + i * 3);
            MenuItem speedItem = danmuMove.add(moveGroup, 1110 + i, i, speedTitle);
            speedItem.setCheckable(true);
            if (speedTitle.equals(String.valueOf(danmuSetting.getSpeed()))) {
                speedItem.setChecked(true);
            }
            speedItem.setOnMenuItemClickListener(moveItemOnClickListener);
        }

        MenuItem debugItem = menu.add(5, 7, order ++, "调试");
        debugItem.setCheckable(true);
        debugItem.setChecked(danmuSetting.isDebug());
        debugItem.setOnMenuItemClickListener(item -> {
            danmuSetting.setDebug(!item.isChecked());
            return true;
        });

        // 弹幕来源
        if (danmuSetting.isOpen()) {
            String sourceTitle = "弹幕来源支持数: " + (danmuSetting.getDanmuApiList() == null ? 0 : danmuSetting.getDanmuApiList().size());
            MenuItem danmySource = menu.add(5, 3, order ++, sourceTitle);
            danmySource.setEnabled(false);
            danmySource.setCheckable(false);
            List<DanmuApiOption> danmuApiList = danmuSetting.getDanmuApiList();
            Map<Integer, DanmuApiOption> itemSourceMap = new HashMap<>();

            if (danmuApiList != null && !danmuSetting.getDanmuApiList().isEmpty()) {
                int index = 10;
                for (int i = 0; i < danmuApiList.size(); i++) {
                    DanmuApiOption danmu = danmuApiList.get(i);
                    MenuItem damnuItem = menu.add(3, index + i, index + i, "  - " + danmu.getSourceName());

                    damnuItem.setCheckable(true);
                    damnuItem.setChecked(danmu.isOpened());
                    itemSourceMap.put(damnuItem.getItemId(), danmu);

                    damnuItem.setOnMenuItemClickListener(item -> {

                        DanmuApiOption danmuApiOption = itemSourceMap.get(item.getItemId());
                        if (danmuApiOption == null) {
                            return true;
                        }
                        danmuApiOption.setOpened(!item.isChecked());
                        return true;
                    });
                }
            }
        }

        popupMenu.show();
    }





}
