package com.stackbase.mobapp.view;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;

    private String picPath;

    public ImageItem(Bitmap image, String title, String picPath) {
        super();
        this.image = image;
        this.title = title;
        this.picPath = picPath;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

}