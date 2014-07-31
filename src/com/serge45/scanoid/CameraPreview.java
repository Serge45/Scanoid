package com.serge45.scanoid;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG = "CameraPreview";
	private Camera camera = null;
	private SurfaceHolder surfaceHolder = null;
	private CameraFactory cameraFactory = null;
	
	public int getCameraOrientation() {
		return cameraFactory.getSensorOrientation();
	}
	
	public boolean isFrontCamera() {
		return cameraFactory.isFront();
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
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
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		if (camera == null || surfaceHolder == null) {
			Log.e(TAG, "No camera or no surface holder");
			return;
		}

        camera.stopPreview();
        cameraFactory.initCameraParams(width, height);

		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopCamera();
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
		//getHolder().removeCallback(this);
		//surfaceHolder = null;
	}
	
	public void openCamera() {
		cameraFactory.getCameraInstance();
		camera = cameraFactory.getCurrentCamera();
	}
	
	public void takePicture(PictureCallback callback) {
		cameraFactory.takePicture(null, null, callback);
	}

}
