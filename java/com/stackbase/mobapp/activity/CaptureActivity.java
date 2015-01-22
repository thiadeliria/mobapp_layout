package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.camera.BeepManager;
import com.stackbase.mobapp.camera.CameraManager;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.view.ShutterButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the text correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback,
        ShutterButton.OnShutterButtonListener, Camera.PictureCallback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    // Context menu
    private static final int SETTINGS_ID = Menu.FIRST;
    private static final int ABOUT_ID = Menu.FIRST + 1;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private View cameraButtonView;
    private boolean hasSurface;
    private BeepManager beepManager;
    private ShutterButton shutterButton;
    private SharedPreferences prefs;
    private boolean isPaused;
    private OnSharedPreferenceChangeListener listener;
    private FinishListener finishListener;
    private TextView savePictureTextView;
    private TextView recaptureTextView;
    private ImageView pictureConfirmImageView;

    public FinishListener getFinishListener() {
        return finishListener;
    }

    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
// remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture);
        cameraButtonView = findViewById(R.id.camera_button_view);

        handler = null;
        hasSurface = false;
        beepManager = new BeepManager(this);

        // Camera shutter button
        shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        shutterButton.setOnShutterButtonListener(this);

        savePictureTextView = (TextView) findViewById(R.id.savePictureTextView);
        recaptureTextView = (TextView) findViewById(R.id.recaptureTextView);

        savePictureTextView.setOnClickListener(this);
        recaptureTextView.setOnClickListener(this);
        cameraManager = new CameraManager(getApplication());

        finishListener = new FinishListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetStatusView();

        retrievePreferences();

        // Set up the camera preview surface.
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    /**
     * Called when the shutter button is pressed in continuous mode.
     */
    void onShutterButtonPress() {
        isPaused = true;
        handler.stop();
        beepManager.playBeepSoundAndVibrate();

        savePictureTextView.setVisibility(View.VISIBLE);
        recaptureTextView.setVisibility(View.VISIBLE);
        shutterButton.setVisibility(View.GONE);

        cameraManager.takePicture(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface");
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface) { // && isEngineReady
            Log.d(TAG, "surfaceCreated(): calling initCamera()...");
            initCamera(holder);
        }
        hasSurface = true;
    }

    /**
     * Initializes the camera and starts the handler to begin previewing.
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "initCamera()");
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {

            // Open and initialize the camera
            cameraManager.openDriver(surfaceHolder);

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = new CaptureActivityHandler(this, cameraManager);

        } catch (IOException ioe) {
            Log.e(TAG, "Fail to open camera driver", ioe);
            Helper.showErrorMessage(this, "错误", "不能打开照相机设备, 请重启您的手机或检查权限设置.",
                    finishListener, finishListener);
        } catch (RuntimeException e) {
            Log.e(TAG, "Fail to open camera driver", e);
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Helper.showErrorMessage(this, "错误", "不能打开照相机设备, 请重启您的手机或检查权限设置.",
                    finishListener, finishListener);
        }
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
        }

        // Stop using the camera, to avoid conflicting with other camera-based apps
        cameraManager.closeDriver();

        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    void stopHandler() {
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    protected void onDestroy() {
//        if (baseApi != null) {
//            baseApi.end();
//        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // First check if we're paused in continuous mode, and if so, just unpause.
            if (isPaused) {
                Log.d(TAG, "only resuming continuous recognition, not quitting...");
                resumeContinuousCapture();
                return true;
            }

            // Exit the app if we're not viewing an OCR result.
            setResult(RESULT_CANCELED);
            releaseBitmap();
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            onShutterButtonPress();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Only perform autofocus if user is not holding down the button.
            if (event.getRepeatCount() == 0) {
                cameraManager.requestAutoFocus(500L);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void resumeContinuousCapture() {
        isPaused = false;
        resetStatusView();
        if (handler != null) {
            handler.resetState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //    MenuInflater inflater = getMenuInflater();
        //    inflater.inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
//        menu.add(0, ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case SETTINGS_ID: {
                intent = new Intent().setClass(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            }
//            case ABOUT_ID: {
//                intent = new Intent(this, HelpActivity.class);
//                intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, HelpActivity.ABOUT_PAGE);
//                startActivity(intent);
//                break;
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (handler != null) {
            handler.setCameraDisplayOrientation();
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * Resets view elements.
     */
    private void resetStatusView() {
        cameraButtonView.setVisibility(View.VISIBLE);
        shutterButton.setVisibility(View.VISIBLE);
        savePictureTextView.setVisibility(View.GONE);
        recaptureTextView.setVisibility(View.GONE);
        if (pictureConfirmImageView != null) {
            // release the memory
            BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                drawable.getBitmap().recycle();
                pictureConfirmImageView = null;
            }
        }
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
    /**
     * Request the viewfinder to be invalidated.
     */
    void drawViewfinder() {
//     viewfinderView.drawViewfinder();
    }

    @Override
    public void onShutterButtonClick(ShutterButton b) {
        onShutterButtonPress();
    }

    @Override
    public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
        requestDelayedAutoFocus();
    }

    /**
     * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user
     * just wants to click the shutter button without focusing. Quick button press/release will
     * trigger onShutterButtonClick() before the focus kicks in.
     */
    private void requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraManager.requestAutoFocus(350L);
    }

    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve from preferences, and set in this Activity, the language preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        prefs.registerOnSharedPreferenceChangeListener(listener);

        beepManager.updatePrefs();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "In onPictureTaken");
        if (data != null) {
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
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            if (pictureConfirmImageView == null) {
                pictureConfirmImageView = (ImageView) findViewById(R.id.pictureConfirmImageView);
            }
            if (pictureConfirmImageView != null) {
                pictureConfirmImageView.setImageBitmap(bm);
            }
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close stream.", e);
            }
        } else {
            Log.d(TAG, "Did not get the data when take picture!");
        }
    }

    private File getOutputMediaFile() {
        //get the mobile Pictures directory
        String storage_dir = getIntent().getStringExtra(Constant.INTENT_KEY_PIC_FOLDER);
        if (storage_dir == null || storage_dir.equals("")) {
            storage_dir = getSharedPreferences().getString(PreferencesActivity.KEY_STORAGE_DIR, "");
        }

        File picDir = new File(storage_dir);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(picDir.getAbsolutePath() + File.separator + "IMAGE_" + timeStamp + ".jpg");
    }

    private void savePictureFromView() {
        if (pictureConfirmImageView != null) {
            BitmapDrawable drawable = (BitmapDrawable) pictureConfirmImageView.getDrawable();
            if (drawable != null && drawable.getBitmap() != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                drawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions!!");
                    return;
                }
                releaseBitmap();
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Fail to close stream.", e);
                }
                Helper.saveFile(pictureFile.getAbsolutePath(), byteArray);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recaptureTextView:
                resumeContinuousCapture();
                break;
            case R.id.savePictureTextView:
                savePictureFromView();
                finish();
                break;
        }
    }
}

