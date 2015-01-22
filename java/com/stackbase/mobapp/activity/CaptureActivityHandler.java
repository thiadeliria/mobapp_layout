package com.stackbase.mobapp.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.camera.CameraManager;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
final class CaptureActivityHandler extends Handler implements Helper.ErrorCallback {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();
    //  private final DecodeThread decodeThread;
    private static State state;
    private final CaptureActivity activity;
    private final CameraManager cameraManager;

    CaptureActivityHandler(CaptureActivity activity, CameraManager cameraManager) {
        this.activity = activity;
        this.cameraManager = cameraManager;

        // Start ourselves capturing previews (and decoding if using continuous recognition mode).
        cameraManager.startPreview();

        state = State.SUCCESS;
    }

    @Override
    public void handleMessage(Message message) {

        switch (message.what) {
            case R.id.restart_preview:
                restartPreview();
                break;
        }
    }

    void stop() {
        // TODO See if this should be done by sending a quit message to decodeHandler as is done
        // below in quitSynchronously().

        Log.d(TAG, "Setting state to CONTINUOUS_PAUSED.");
        state = State.CONTINUOUS_PAUSED;

        // Freeze the view displayed to the user.
        cameraManager.stopPreview();
    }

    void resetState() {
        //Log.d(TAG, "in restart()");
        if (state == State.CONTINUOUS_PAUSED) {
            Log.d(TAG, "Setting state to CONTINUOUS");
            state = State.CONTINUOUS;
            restartOcrPreviewAndDecode();
        }
    }

    void quitSynchronously() {
        state = State.DONE;
        if (cameraManager != null) {
            cameraManager.stopPreview();
        }
    }

    /**
     * Start the preview, but don't try to OCR anything until the user presses the shutter button.
     */
    private void restartPreview() {
        // Display the shutter and torch buttons
        activity.resumeContinuousCapture();

        if (state == State.SUCCESS) {
            state = State.PREVIEW;

            // Draw the viewfinder
            // TODO: this is for OCR
            activity.drawViewfinder();
        }
    }

    /**
     * Send a decode request for realtime OCR mode
     */
    private void restartOcrPreviewAndDecode() {
        // Continue capturing camera frames
        cameraManager.startPreview();

    }

    @Override
    public void onErrorTaken(String title, String message) {
        Helper.showErrorMessage(activity, title, message, activity.getFinishListener(),
                activity.getFinishListener());
    }

    public void setCameraDisplayOrientation() {
        cameraManager.setDisplayOrientation(activity);
    }

    private enum State {
        PREVIEW,
        PREVIEW_PAUSED,
        CONTINUOUS,
        CONTINUOUS_PAUSED,
        SUCCESS,
        DONE
    }

}
