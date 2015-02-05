package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.Thumbnail;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.view.adapters.ThumbnailsGridViewAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailsActivity extends Activity implements AbsListView.OnScrollListener {

    private GridView gridView;
    private ThumbnailsGridViewAdapter customGridAdapter;
    private ImageButton takePictureBtn;
    private static final String TAG = ThumbnailsActivity.class.getSimpleName();
    private ArrayList<Thumbnail> mList = null;
    private static Map<String, Bitmap> gridviewBitmapCaches = null;

    public static Map<String, Bitmap> getGridviewBitmapCaches() {
        return gridviewBitmapCaches;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumbnails);
        gridView = (GridView) findViewById(R.id.picturesGridView);
        gridviewBitmapCaches = new HashMap<String, Bitmap>();
        initData();
        setAdapter();

        takePictureBtn = (ImageButton) this.findViewById(R.id.takepictureBtn);
        takePictureBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ThumbnailsActivity.this, CaptureActivity.class);
                String picFolder = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
                intent.putExtra(Constant.INTENT_KEY_PIC_FOLDER, picFolder);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            // refresh the Gridview
            if (resultCode == Activity.RESULT_OK) {
                String newPicture = data.getStringExtra(Constant.INTENT_KEY_PIC_FULLNAME);
                if (newPicture != null && !newPicture.equals("")) {
                    Thumbnail thumbnail = new Thumbnail();
                    thumbnail.setPictureName(newPicture);
                    customGridAdapter.add(thumbnail);
                    customGridAdapter.notifyDataSetChanged();
                }
            }
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

    private void initData() {
        mList = new ArrayList<Thumbnail>();
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
            Thumbnail thumbnail = new Thumbnail();
            thumbnail.setPictureName(pictures[i].getAbsolutePath());
            mList.add(thumbnail);
        }
    }

    private void setAdapter() {
        customGridAdapter = new ThumbnailsGridViewAdapter(this, R.layout.thumbnail_row, mList);
        gridView.setAdapter(customGridAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Log.d(TAG, "onItemClick : " + gridView.getItemAtPosition(position));
                Intent intent = new Intent();
                intent.putExtra(Constant.INTENT_KEY_PIC_FULLNAME, ((Thumbnail)gridView.getItemAtPosition(position)).getPictureName());
                intent.setClass(ThumbnailsActivity.this, FullPictureReviewActivity.class);
                startActivity(intent);

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Helper.showErrorMessage(ThumbnailsActivity.this, "警告", "确定要删除这张图片吗？",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Thumbnail thumbnail = (Thumbnail) gridView.getItemAtPosition(position);
                                File file = new File(thumbnail.getPictureName());
                                file.delete();
                                deleteItem(thumbnail.getPictureName());
                                customGridAdapter.remove(thumbnail);
                                customGridAdapter.notifyDataSetChanged();
                            }
                        });
                return false;
            }
        });
    }

    private void removeBitmapCache(String url) {
        Bitmap delBitmap;
        delBitmap = gridviewBitmapCaches.get(url);
        if (delBitmap != null) {
            Log.d(TAG, "release position:" + url);
            gridviewBitmapCaches.remove(url);
            delBitmap.recycle();
            delBitmap = null;
        }
    }

    private void deleteItem(String url) {
        mList.remove(url);
        removeBitmapCache(url);
    }

    private void recycleBitmapCaches(int fromPosition, int toPosition) {
        for (int del = fromPosition; del < toPosition; del++) {
            Thumbnail thumbnail = mList.get(del);
            if (thumbnail != null) {
                removeBitmapCache(thumbnail.getPictureName());
            }
        }
    }

    private void recycleAllBitmapCaches() {
        Bitmap delBitmap;
        for (Map.Entry<String, Bitmap> entry : gridviewBitmapCaches.entrySet()) {
            delBitmap = entry.getValue();
            if (delBitmap != null) {
                Log.d(TAG, "release bitmap:" + entry.getKey());
                delBitmap.recycle();
                delBitmap = null;
            }
        }
        gridviewBitmapCaches.clear();

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        recycleBitmapCaches(0, firstVisibleItem);
        recycleBitmapCaches(firstVisibleItem + visibleItemCount, totalItemCount);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    protected void onDestroy() {
        // Release all the bitmaps from cache
        recycleAllBitmapCaches();
        Log.d(TAG, "size: " + gridviewBitmapCaches.size());
        gridviewBitmapCaches = null;
        super.onDestroy();
    }
}
