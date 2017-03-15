package com.zcy.androidcrop;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by zcy on 2017/3/15.
 */

public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }
}
