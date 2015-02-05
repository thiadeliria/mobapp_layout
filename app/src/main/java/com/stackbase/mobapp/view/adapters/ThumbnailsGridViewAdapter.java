package com.stackbase.mobapp.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.activity.ThumbnailsActivity;
import com.stackbase.mobapp.objects.Thumbnail;
import com.stackbase.mobapp.utils.BitmapUtilities;
import com.stackbase.mobapp.view.ImageItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

public class ThumbnailsGridViewAdapter extends ArrayAdapter<Thumbnail> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Thumbnail> data = new ArrayList();

    private int width = 100;
    private int height = 120;
    private static final String TAG = ThumbnailsGridViewAdapter.class.getSimpleName();

    public ThumbnailsGridViewAdapter(Context context, int layoutResourceId,
                                     ArrayList<Thumbnail> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public void remove(Thumbnail thumbnail) {
        this.data.remove(thumbnail);
    }

    @Override
    public void add(Thumbnail thumbnail) {
        this.data.add(thumbnail);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageItem viewHolder;
        if (convertView == null) {
            viewHolder = new ImageItem();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            viewHolder.setImageView((ImageView) convertView.findViewById(R.id.pictureImageView));
            viewHolder.setTextView((TextView) convertView.findViewById(R.id.pictureTexView));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ImageItem) convertView.getTag();
        }

        Thumbnail thumbnail = data.get(position);
        if (cancelPotentialLoad(thumbnail, viewHolder.getImageView())) {
            AsyncLoadImageTask task = new AsyncLoadImageTask(viewHolder.getImageView());
            LoadedDrawable loadedDrawable = new LoadedDrawable(task);
            viewHolder.getImageView().setImageDrawable(loadedDrawable);
            task.execute(position);
        }
        if (thumbnail.getDescription() == null || thumbnail.getDescription().equals("")) {
            viewHolder.getTextView().setText("Image#" + position);
        } else {
            viewHolder.getTextView().setText(thumbnail.getDescription());
        }
        return convertView;
    }


    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        Map<String, Bitmap> caches = ThumbnailsActivity.getGridviewBitmapCaches();
        if ( caches != null) {
            bitmap = caches.get(url);
            if (bitmap != null) {
                Log.d(TAG, "Find bitmap from Cache: " + url);
                return bitmap;
            }

            bitmap = BitmapUtilities.getBitmap(url);
            bitmap = BitmapUtilities.getBitmapThumbnail(bitmap, width, height);
        }
        return bitmap;
    }

    private class AsyncLoadImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private String url = null;
        private final WeakReference<ImageView> imageViewReference;

        public AsyncLoadImageTask(ImageView imageview) {
            super();
            imageViewReference = new WeakReference<ImageView>(imageview);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Bitmap bitmap = null;
            if (data != null) {
                Thumbnail thumbnail = data.get(params[0]);
                if (thumbnail != null) {
                    this.url = thumbnail.getPictureName();
                    bitmap = getBitmapFromUrl(url);
                    Map<String, Bitmap> caches = ThumbnailsActivity.getGridviewBitmapCaches();
                    if (caches != null) {
                        caches.put(thumbnail.getPictureName(), bitmap);
                    }
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {
            if (isCancelled()) {
                resultBitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageview = imageViewReference.get();
                AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
                // Change bitmap only if this process is still associated with it
                if (this == loadImageTask) {
                    imageview.setImageBitmap(resultBitmap);
                    imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
            super.onPostExecute(resultBitmap);
        }
    }


    private boolean cancelPotentialLoad(Thumbnail thumbnail, ImageView imageview) {
        AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);

        if (loadImageTask != null) {
            String bitmapUrl = loadImageTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(thumbnail.getPictureName()))) {
                loadImageTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;

    }

    private AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview) {
        if (imageview != null) {
            Drawable drawable = imageview.getDrawable();
            if (drawable instanceof LoadedDrawable) {
                LoadedDrawable loadedDrawable = (LoadedDrawable) drawable;
                return loadedDrawable.getLoadImageTask();
            }
        }
        return null;
    }

    public static class LoadedDrawable extends ColorDrawable {
        private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

        public LoadedDrawable(AsyncLoadImageTask loadImageTask) {
            super(Color.TRANSPARENT);
            loadImageTaskReference =
                    new WeakReference<AsyncLoadImageTask>(loadImageTask);
        }

        public AsyncLoadImageTask getLoadImageTask() {
            return loadImageTaskReference.get();
        }

    }
}
