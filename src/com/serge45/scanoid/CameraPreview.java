package com.serge45.scanoid;

import java.io.IOException;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;

public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {
    private String TAG = "CameraPreview";
    private Camera camera = null;
    private SurfaceHolder surfaceHolder = null;
    private CameraFactory cameraFactory = null;
    private Camera.Size bestViewSize;

    public void resizeToFitPreview() {
        getLayoutParams().width = bestViewSize.width;
        getLayoutParams().height = bestViewSize.height;
    }

    public int getCameraOrientation() {
        return cameraFactory.getSensorOrientation();
    }

    public boolean isFrontCamera() {
        return cameraFactory.isFront();
    }
    
    public void initHolder() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHolder();
        cameraFactory = new CameraFactory(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.v(TAG, "SurfaceView touched");
            camera.autoFocus(null);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "Surface created.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

        if (camera == null || surfaceHolder == null) {
            Log.e(TAG, "No camera or no surface holder");
            return;
        }

        Camera.Size previewImageSize = cameraFactory.initCameraParams(width,
                height);
        bestViewSize = previewImageSize;

        float imageRatio = (float) previewImageSize.width
                / previewImageSize.height;
        int w = Math.max(width, height);
        int h = Math.min(width, height);

        float viewRatio = (float) w / h;

        /*
        if (imageRatio > viewRatio) {
            int newHeight = (int) (width / imageRatio + 0.5);
            bestViewSize.height = newHeight;
            bestViewSize.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
        */
            int newWidth = (int) (height * imageRatio + 0.5);
            bestViewSize.width = newWidth;
            bestViewSize.height = ViewGroup.LayoutParams.MATCH_PARENT;
        //}
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            resizeToFitPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "Surface destroyed.");
        stopCamera();
        getHolder().removeCallback(this);
        surfaceHolder = null;
    }

    public void startPreview() {
        if (camera != null) {
            camera.startPreview();
        } else {
            Log.e(TAG, "Cannot start preview");
        }
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        } else {
            Log.e(TAG, "Cannot start preview");
        }
    }

    public void stopCamera() {
        camera.release();
    }

    public void openCamera() {
        cameraFactory.getCameraInstance();
        camera = cameraFactory.getCurrentCamera();
    }

    public void takePicture(PictureCallback callback) {
        cameraFactory.takePicture(null, null, callback);
    }

}
