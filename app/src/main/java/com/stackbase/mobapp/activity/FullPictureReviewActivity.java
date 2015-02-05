package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.utils.BitmapUtilities;
import com.stackbase.mobapp.utils.Constant;

public class FullPictureReviewActivity extends Activity {

    private ImageView imageView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_picture_review);

        // get intent data
        Intent i = getIntent();

        // Selected image name
        String pictureName = i.getExtras().getString(Constant.INTENT_KEY_PIC_FULLNAME);
        imageView = (ImageView) findViewById(R.id.full_image_view);
        imageView.setImageBitmap(BitmapUtilities.getBitmap(pictureName));
    }

    @Override
    protected void onDestroy() {
        if (imageView != null) {
            // release the memory
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                drawable.getBitmap().recycle();
                imageView = null;
            }
        }
        super.onDestroy();
    }
}