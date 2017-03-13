package com.myboxteam.scanner.application;

/**
 * Created by Admin on 3/9/2017.
 */

public class MBApplication extends android.app.Application {
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }
}