package com.michglass.glasshouse.glasshouse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.glass.media.CameraManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Oliver
 * Date: 4/18/2014.
 */
public class PictureActivity extends BluetoothActivity {

    // Debug
    private static final String TAG = "Picture Activity";

    // Camera vars
    private Camera mCamera;
    private SurfaceView mView;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    // indicate if user input is enabled
    private boolean isInputEnabled;
    private boolean isTakePic;
    private boolean isGoBack;

    // Texts displayed on preview
    private static final String TAKE_PIC = "Tap to take picture and to go back";

    @Override
    protected void onCreate(Bundle savedInst) {
        Log.v(TAG, "On Create");
        super.onCreate(savedInst);

        // disable user input
        isInputEnabled = false;
        isTakePic = true;
        isGoBack = false;

        // keep screen from dimming
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set up surface view
        mView = new SurfaceView(this);
        mHolder = mView.getHolder();
        mHolder.addCallback(getHolderCallback());

        setContentView(mView);
    }
    @Override
    protected void onStart() {
        Log.v(TAG, "On Start");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.v(TAG, "On Resume");
        super.onResume();

        // set up preview
        PreviewThread previewThread = new PreviewThread(TAKE_PIC);
        previewThread.start();
    }
    @Override
    protected void onStop() {
        Log.v(TAG, "On Stop");
        super.onStop();
        // close camera
        closeCam();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        Log.v(TAG, "On Key Down");
        Log.v(TAG, "Input enabled: " + isInputEnabled);
        Log.v(TAG, "Take Pic: " + isTakePic);
        Log.v(TAG, "Go Back: " + isGoBack);
        if(isInputEnabled && isTakePic) {
            if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
                Log.v(TAG, "On Tap Picture");
                takePicture();
                isInputEnabled = false;
                isTakePic = false;
                isGoBack = true;
                return true;
            }
        } else if(isInputEnabled && isGoBack) {
            if(keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
                Log.v(TAG, "On Tap Close");
                finish();
                return true;
            }
        }
        return super.onKeyDown(keycode, event);
    }
    private void takePicture() {

        try {
            Log.v(TAG, "Try Taking Picture");
            mCamera.setPreviewDisplay(mHolder);

        } catch (IOException ioE) {
            Log.e(TAG, "Setting PreviewDisplay Failed");
        }
        mCamera.startPreview();
        mCamera.takePicture(getShuttCallBack(), getRawCallback(), getPostCallback(), getJpgCallback());

    }
    // set up camera
    private void setupCam() {
        try {
            Log.v(TAG, "Try open cam");
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "Couldn't set up camera", e);
        }
        isInputEnabled = true;
    }
    // close camera
    private void closeCam() {
        if(mCamera != null) {
            Log.v(TAG, "Close Cam");
            mCamera.release();
        } else {
            Log.e(TAG, "Camera Null");
        }
    }
    // save picture on external storage
    private void savePicture(Bitmap img) {
        ByteArrayOutputStream byteOutS = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 40, byteOutS);
        try {
            Log.v(TAG, "Try saving pic");
            String pathEnding = "/Pictures/mytest.jpg";
            String pathBegin = Environment.getExternalStorageDirectory().getPath();
            Log.v(TAG, "Path Start: " + pathBegin);
            String path = pathBegin + pathEnding;
            Log.v(TAG, "SD Card av: " +isSDCARDAvailable());
            Log.v(TAG, "Path: " + path);
            File outFile = new File(path);
            boolean b = outFile.createNewFile();
            Log.v(TAG, "Create File " + b);
            FileOutputStream outFileS = new FileOutputStream(outFile);
            outFileS.write(byteOutS.toByteArray());

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File already exists");
        } catch (IOException ioE) {
            Log.e(TAG, "Write failed");
        }
    }

    // set up the callbacks for surface holder (necessary before messing with camera)
    private SurfaceHolder.Callback getHolderCallback() {
        return new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.v(TAG, "Surface Created");
                // set up camera
                setupCam();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                Log.v(TAG, "Surface Changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.v(TAG, "Surface Destroyed");
            }
        };
    }

    // picture callbacks
    private Camera.ShutterCallback getShuttCallBack() {
        return new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.v(TAG, "On Shutter");
            }
        };
    }
    private Camera.PictureCallback getRawCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Log.v(TAG, "Raw Callback");
            }
        };
    }
    private Camera.PictureCallback getPostCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Log.v(TAG, "postview callback");
            }
        };
    }
    private Camera.PictureCallback getJpgCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Log.v(TAG, "jpg callback");
                isInputEnabled = true;

                // save picture on ext storage
                Bitmap imgBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                savePicture(imgBitmap);
            }
        };
    }
    public boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Preview Thread
     * Display text on the Screen before and after taking a picture
     */
    private class PreviewThread extends Thread {

        // Debug
        private static final String TAG = "Preview Thread";

        // keep running
        private boolean mKeepRunning;
        private String displayText;
        private Paint textPaint;

        public PreviewThread(String text) {
            displayText = text;
            mKeepRunning = true;

            // Text Paint
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(30f);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        public void run() {
            Log.v(TAG, "Run");

            while (mKeepRunning) {

                if(!mHolder.getSurface().isValid())
                    continue;

                mCanvas = mHolder.lockCanvas();

                //mCanvas.drawColor(Color.GRAY);
                mCanvas.drawText(displayText, mCanvas.getWidth()/2, mCanvas.getHeight()/2, textPaint);

                mHolder.unlockCanvasAndPost(mCanvas);

                mKeepRunning = false;
            }
            Log.v(TAG, "Run Return");
        }
    }
}
