package com.stackbase.mobapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stackbase.mobapp.activity.PreferencesActivity;

/**
 * Created by gengjh on 1/16/15.
 */
abstract public class Constant {
    /**
     * Whether to use autofocus by default.
     */
    public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;
    /**
     * Whether to initially disable continuous-picture and continuous-video focus modes.
     */
    public static final boolean DEFAULT_DISABLE_CONTINUOUS_FOCUS = true;
    /**
     * Whether the light should be initially activated by default.
     */
    public static final boolean DEFAULT_TOGGLE_LIGHT = false;
    /**
     * The default OCR engine to use.
     */
    public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";
    /**
     * The default page segmentation mode to use.
     */
    public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
    /**
     * Whether to beep by default when the shutter button is pressed.
     */
    public static final boolean DEFAULT_TOGGLE_BEEP = false;
    /**
     * Whether to initially show a looping, real-time OCR display.
     */
    public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;
    /**
     * Whether to initially reverse the image returned by the camera.
     */
    public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
    /**
     * The default subdir to save pictures and data
     */
    public static final String DEFAULT_STORAGE_DIR = "stackbase/mobapp/";

    public static final String INTENT_PACKAGE = "com.stackbase.mobapp";

    public static final String INTENT_KEY_ID = "INTENT_NAME_ID";
    public static final String INTENT_KEY_NAME = "INTENT_KEY_NAME";
    public static final String INTENT_KEY_PIC_FOLDER = "INTENT_KEY_PIC_FOLDER";

}
