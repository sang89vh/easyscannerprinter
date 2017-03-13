package com.myboxteam.scanner.filters;

import android.graphics.Bitmap;

import com.mukesh.image_processing.ImageProcessor;
import com.myboxteam.scanner.fragment.ScanFragment;
import com.myboxteam.scanner.scanlibrary.ScanUtils;
import com.zomato.photofilters.imageprocessors.Filter;

import net.alhazmy13.imagefilter.ImageFilter;

/**
 * Created by jack on 3/12/17.
 */

public class MixFilter extends Filter {
    private int mode;
    private ImageProcessor imageProcessor;


    public MixFilter(int mode) {
        this.mode = mode;
        this.imageProcessor = new ImageProcessor();
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
                case ScanFragment.MODE_BRIGHTNESS:
                    return imageProcessor.doInvert(inputImage);
                default:
                return inputImage;
            }

        }else{
           return  super.processFilter(inputImage);
        }
    }
}
