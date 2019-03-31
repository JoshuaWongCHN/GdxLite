package com.joshua.gdx.gdxlite;

import android.app.Application;

import com.joshua.gdx.gdxlite.utils.FileUtil;

public class GLApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileUtil.init(getAssets());
    }
}
