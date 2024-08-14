package com.tv.fengymi.danmu.core.container;

import com.tv.fengymi.danmu.core.DanmuContainer;
import com.tv.fengymi.danmu.model.Danmu;
import com.tv.fengymi.danmu.utils.SortUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteDanmuContainer extends DanmuContainer {
    private List<Danmu> danmus;

    public CopyOnWriteDanmuContainer() {
        this.danmus = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<Danmu> getDanmus() {
        return danmus;
    }

    @Override
    public void doResetDanmus(List<Danmu> danmus) {
        if (danmus == null) {
            throw new RuntimeException("弹幕信息不能为null");
        }
        this.danmus = new CopyOnWriteArrayList<>(danmus);
        Collections.sort(this.danmus);
        clearAll.compareAndSet(false, true);
        reset(true);
    }

    @Override
    public void doAddDanmu(Danmu danmu) {
        if (danmu == null) {
            throw new RuntimeException("弹幕信息不能为null");
        }

        if (this.danmus == null) {
            this.danmus = new CopyOnWriteArrayList<>();
        }

        int index = SortUtil.findFirstGreaterThanOrEqual(this.danmus, danmu, 0);
        this.danmus.add(index, danmu);
    }
}
