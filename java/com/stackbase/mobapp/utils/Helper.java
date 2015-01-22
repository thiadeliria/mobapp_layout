package com.stackbase.mobapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

abstract public class Helper {

    private static final String TAG = Helper.class.getSimpleName();

    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title   The title for the dialog box
     * @param message The error message to be displayed
     */
    public static void showErrorMessage(Context context, String title, String message,
                                        DialogInterface.OnCancelListener cancelListener,
                                        DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(cancelListener)
                .setPositiveButton("确认", positiveListener)
                .show();
    }

    /**
     * Finds the proper location on the SD card where we can save files.
     */
    public static File getStorageDirectory(Context context, ErrorCallback callback) {
        //Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Is the SD card visible?", e);
            if (callback != null) {
                callback.onErrorTaken("错误", "需要的外部存储(例如: SD卡)不可用.");
            }
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            // We can read and write the media
            //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above

            try {
                return context.getExternalFilesDir(Environment.MEDIA_MOUNTED);
            } catch (NullPointerException e) {
                // We get an error here if the SD card is visible, but full
                Log.e(TAG, "External storage is unavailable");
                if (callback != null) {
                    callback.onErrorTaken("错误", "需要的外部存储(例如: SD卡)不可用或者已经没有可用空间.");
                }
            }

            //        } else {
            //          // For Android 2.1 and below, explicitly give the path as, for example,
            //          // "/mnt/sdcard/Android/data/edu.sfsu.cs.orange.ocr/files/"
            //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator +
            //                  "Android" + File.separator + "data" + File.separator + getPackageName() +
            //                  File.separator + "files" + File.separator);
            //        }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
            if (callback != null) {
                callback.onErrorTaken("错误", "需要的外部存储(例如: SD卡)是只读的, 无法存储数据.");
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
            if (callback != null) {
                callback.onErrorTaken("错误", "需要的外部存储(例如: SD卡)不可用或者已经损坏.");
            }
        }
        return null;
    }

    public static String getMD5String(String source) {
        String result = source;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source.getBytes());
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            result = new String(str);

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Fail to generate md5 string", e);
        }
        return result;
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static boolean isValidMD5(String s) {
        return s.matches("[a-fA-F0-9]{32}");
    }

    public static String findMd5fromPath(File file) {
        String parent = file.getParent();
        String strs[] = parent.split(File.separator);
        String result = "com.stackbase.mobapp"; // default password
        for (int i = strs.length - 1; i >= 0; i--) {
            if (Helper.isValidMD5(strs[i])) {
                result = strs[i];
                break;
            }
        }
        return result;
    }

    public static byte[] generateKey(String password) throws Exception {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData, boolean isPlainText) throws Exception {
        byte[] encrypted;
        if (!isPlainText) {
            encrypted = new byte[key.length + fileData.length + key.length];
            System.arraycopy(key, 0, encrypted, 0, key.length);
            System.arraycopy(fileData, 0, encrypted, key.length, fileData.length);
            System.arraycopy(key, 0, encrypted, key.length + fileData.length, key.length);
        } else {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            encrypted = cipher.doFinal(fileData);
        }
        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData, boolean isPlainText) throws Exception {
        byte[] decrypted;
        if (!isPlainText) {
            decrypted = new byte[fileData.length - key.length * 2];
            System.arraycopy(fileData, key.length, decrypted, 0, decrypted.length);
        } else {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            decrypted = cipher.doFinal(fileData);
        }
        return decrypted;
    }

    /**
     * save file and encode the content
     *
     * @param fileFullName file name include the path info
     * @param data         content for the file
     * @return
     */
    public static boolean saveFile(String fileFullName, byte[] data) {
        File file = new File(fileFullName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        boolean result = false;
        try {
            fos = new FileOutputStream(file);
            String key = Helper.findMd5fromPath(file);
            try {
                byte[] encodedData = Helper.encodeFile(Helper.generateKey(key), data, isPlainText(fileFullName));
//                byte[] encodedData = data;
                fos.write(encodedData);
            } catch (Exception ex) {
                Log.d(TAG, "Fail to encode data!!");
                fos.write(data);
            }
            result = true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Load file and decrypt the content
     *
     * @param fileFullName file name include the path info
     * @return
     */
    public static byte[] loadFile(String fileFullName) {
        File file = new File(fileFullName);
        String key = Helper.findMd5fromPath(file);
        byte[] result = null;
        try {
            byte[] data = Helper.readFile(file);
            result = Helper.decodeFile(Helper.generateKey(key), data, isPlainText(fileFullName));
//            result = data;
        } catch (Exception ex) {
            Log.d(TAG, "Fail to load File: " + ex.getMessage());
        }
        return result;
    }

    private static boolean isPlainText(String fileFullName) {
        boolean isPlainText = false;
        if (fileFullName.endsWith(".json")) {
            isPlainText = true;
        }
        return isPlainText;
    }

    public interface ErrorCallback {
        /**
         * Called when hint error
         *
         * @param title,   error title
         * @param message, error message
         */
        void onErrorTaken(String title, String message);
    }
}
