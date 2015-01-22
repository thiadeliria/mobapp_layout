package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.view.GridViewAdapter;
import com.stackbase.mobapp.view.ImageItem;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ThumbnailsActivity extends Activity {

    private GridView gridView;
    private GridViewAdapter customGridAdapter;
    private ImageButton takepictureBtn;
    private static final String TAG = ThumbnailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnails);
        gridView = (GridView) findViewById(R.id.picturesGridView);
        customGridAdapter = new GridViewAdapter(this, R.layout.thumbnail_row, getData());
        gridView.setAdapter(customGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(ThumbnailsActivity.this, position + "#Selected",
                        Toast.LENGTH_SHORT).show();
            }
        });

        takepictureBtn = (ImageButton) this.findViewById(R.id.takepictureBtn);
        takepictureBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
//                View layout = inflater.inflate(R.layout.select_picture_type,
//                        (ViewGroup) content.findViewById(R.id.selectPictureTypeDialog));
//
//                // Init the type list in the dialog
//                Spinner dropdown = (Spinner) layout.findViewById(R.id.ptypeSpinner);
//                List items = new ArrayList();
//                ArrayList<View> folders = getViewsByTag((ViewGroup) content,
//                        getResources().getString(R.string.album_folder_tag), TextView.class);
//                for (View folder : folders) {
//                    items.add(((TextView) folder).getText());
//                }
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(active,
//                        android.R.layout.simple_spinner_item, items);
//                dropdown.setAdapter(adapter);
//
//                DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(ThumbnailsActivity.this, CaptureActivity.class);
                String picFolder = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
                intent.putExtra(Constant.INTENT_KEY_PIC_FOLDER, picFolder);
                startActivityForResult(intent, 0);
//                    }
//                };
//
//                new AlertDialog.Builder(active).setTitle("选择照片类型").setView(layout)
//                        .setPositiveButton("确定", alertListener)
//                        .setNegativeButton("取消", null)
//                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            // refresh the Gridview
            customGridAdapter = new GridViewAdapter(this, R.layout.thumbnail_row, getData());
            gridView.setAdapter(customGridAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thumbnails, menu);
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

    private ArrayList<ImageItem> getData() {
        final ArrayList imageItems = new ArrayList();
        // retrieve String drawable array
        String picFolder = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
        File pF = new File(picFolder);
        File[] pictures = pF.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        for (int i = 0; i < pictures.length; i++) {
            try {
                byte[] decodedData = Helper.loadFile(pictures[i].getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedData, 0, decodedData.length);
                imageItems.add(new ImageItem(ThumbnailUtils.extractThumbnail(bitmap, 40, 60,
                        ThumbnailUtils.OPTIONS_RECYCLE_INPUT),
                        "Image#" + i, pictures[i].getAbsolutePath()));
            } catch (Exception ex) {
                Log.d(TAG, "Fail to load File: " + ex.getMessage());
            }
        }

        return imageItems;

    }

//    private Bitmap getRotateBitmap(String pictureName) {
//        try {
//            BitmapFactory.Options bounds = new BitmapFactory.Options();
//            bounds.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(pictureName, bounds);
//
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            Bitmap bm = BitmapFactory.decodeFile(pictureName, opts);
//            ExifInterface exif = new ExifInterface(pictureName);
//            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
//
//            int rotationAngle = 0;
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
//            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
//
//            Matrix matrix = new Matrix();
//            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
//            return rotatedBitmap;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//http://www.trinea.cn/android/android-image-outofmemory-bitmap-size-exceeds-vm-budget/
//    private static int   IMAGE_MAX_WIDTH  = 480;
//    private static int   IMAGE_MAX_HEIGHT = 960;
//    private static int getImageScale(String imagePath) {
//        BitmapFactory.Options option = new BitmapFactory.Options();
//        // set inJustDecodeBounds to true, allowing the caller to query the bitmap info without having to allocate the
//        // memory for its pixels.
//        option.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imagePath, option);
//
//        int scale = 1;
//        while (option.outWidth / scale >= IMAGE_MAX_WIDTH || option.outHeight / scale >= IMAGE_MAX_HEIGHT) {
//            scale *= 2;
//        }
//        return scale;
//    }
}
