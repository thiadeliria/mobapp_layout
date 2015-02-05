package com.stackbase.mobapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

public class BitmapUtilities {

    public static Bitmap getBitmap(String fileName) {
        Bitmap bitmap = null;
        byte[] decodedData = Helper.loadFile(fileName);
        bitmap = BitmapFactory.decodeByteArray(decodedData, 0, decodedData.length);
        return bitmap;
    }

    public static Bitmap getBitmapThumbnail(Bitmap bmp, int width, int height) {
        Bitmap bitmap = null;
        if (bmp != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bmp, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}
