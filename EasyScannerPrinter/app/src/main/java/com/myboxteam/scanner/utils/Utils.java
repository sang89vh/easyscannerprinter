package com.myboxteam.scanner.utils;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by jack on 3/18/17.
 */

public class Utils {
    public static Integer h = null;
    public static Integer w = null;
    public static  int getHeightScreen(Activity context){
        if(h == null) {
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            //int width = size.x;
            h = size.y;
        }
        return h;
    }

    public static  int getWidthScreen(Activity context){
        if(w == null) {
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            //int width = size.x;
            w = size.x;
        }
        return w;
    }
}
