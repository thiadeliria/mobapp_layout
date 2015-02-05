package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureConfirmActivity extends Activity implements View.OnClickListener {

    private static final String TAG = PictureConfirmActivity.class.getSimpleName();
    private TextView savePictureTextView;
    private TextView recaptureTextView;
    private ImageView pictureConfirmImageView;
    private String tempImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_picture_confirm);
        savePictureTextView = (TextView) findViewById(R.id.savePictureTextView);
        recaptureTextView = (TextView) findViewById(R.id.recaptureTextView);
        pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
        savePictureTextView.setOnClickListener(this);
        recaptureTextView.setOnClickListener(this);

        initImageView();
    }

    private void initImageView() {
        tempImageFile = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
        if (tempImageFile != null) {
            byte[] data = Helper.loadFile(tempImageFile);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Notice that width and height are reversed
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                int w = scaled.getWidth();
                int h = scaled.getHeight();
                // Setting post rotate to 90
                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                // Rotating Bitmap
                bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
            } else {// LANDSCAPE MODE
                //No need to reverse width and height
                Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, true);
                bm = scaled;
            }
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            pictureConfirmImageView.setImageBitmap(bm);
//            try {
//                stream.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Fail to close stream.", e);
//            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String savePictureFromView() {
        String fileName = "";
        if (pictureConfirmImageView == null) {
            pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
        }
        BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
        if (drawable != null && drawable.getBitmap() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            drawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions!!");
            } else {
                Helper.saveFile(pictureFile.getAbsolutePath(), byteArray);
                fileName = pictureFile.getAbsolutePath();
            }
            releaseBitmap();
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close stream.", e);
            }

        }
        return fileName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recaptureTextView:
                break;
            case R.id.savePictureTextView:
                String fileName = savePictureFromView();
                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_PIC_FULLNAME, fileName);
                this.setResult(Activity.RESULT_OK, intent);
                break;
        }
        releaseBitmap();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tempImageFile != null && !tempImageFile.equals("")) {
            File file = new File(tempImageFile);
            file.delete();
        }
        super.onDestroy();
    }

    private File getOutputMediaFile() {
        //get the mobile Pictures directory
        String storage_dir = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
        if (storage_dir == null || storage_dir.equals("")) {
            storage_dir = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesActivity.KEY_STORAGE_DIR, "");
        }

        File picDir = new File(storage_dir);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(picDir.getAbsolutePath() + File.separator + "IMAGE_" + timeStamp + ".jpg");
    }


    private void releaseBitmap() {
        if (pictureConfirmImageView != null) {
            // release the memory
            BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                drawable.getBitmap().recycle();
                pictureConfirmImageView = null;
            }
        }
    }

}
