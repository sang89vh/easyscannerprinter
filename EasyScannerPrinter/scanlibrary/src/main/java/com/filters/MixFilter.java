package com.filters;

import android.graphics.Bitmap;

import com.scanlibrary.ScanFragment;
import com.scanlibrary.ScanUtils;
import com.zomato.photofilters.imageprocessors.Filter;

import net.alhazmy13.imagefilter.ImageFilter;

/**
 * Created by jack on 3/12/17.
 */

public class MixFilter extends Filter {
    private int mode;

    public MixFilter(int mode) {
        this.mode = mode;
    }

    @Override
    public Bitmap processFilter(Bitmap inputImage) {

        if(this instanceof MixFilter){

            switch (mode){
                case ScanFragment.MODE_BLACK_AND_WHITE:
                    return ScanUtils.getGrayBitmap(inputImage);
                case ScanFragment.MODE_MAGIC:
                    return ScanUtils.getMagicColorBitmap(inputImage);
                case ScanFragment.MODE_OLD:
                    return ImageFilter.applyFilter(inputImage, ImageFilter.Filter.OLD);
                default:
                return inputImage;
            }

        }else{
           return  super.processFilter(inputImage);
        }
    }
}
