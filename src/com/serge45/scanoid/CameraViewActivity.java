package com.serge45.scanoid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

public class CameraViewActivity extends Activity {
	static String TAG = "CameraViewActivity";
	private CameraPreview cameraView = null;
	private Button captureButton = null;
	private ImageView imageView = null;
	private Bitmap lastCaptureImage;
	
	private PictureCallback pictureCallback = new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			cameraView.stopPreview();
			//File pFile = new File(mContext.getExternalFilesDir(null), "test.jpg");
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			
			if (lastCaptureImage != null) {
				lastCaptureImage.recycle();
			}

			lastCaptureImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			int angle = ImageOrientationHelper.getNeedRotationAngle(imageView.getContext(), cameraView.getCameraOrientation());
			Matrix matrix = new Matrix();

			if (cameraView.isFrontCamera()) {
				angle = -angle;
			}

			matrix.postRotate(angle);
			Bitmap rotatedBitmap = Bitmap.createBitmap(lastCaptureImage, 
														0, 
														0,
														lastCaptureImage.getWidth(),
														lastCaptureImage.getHeight(), 
														matrix, 
														true);
			lastCaptureImage = rotatedBitmap;
			imageView.setImageBitmap(lastCaptureImage);
			imageView.setVisibility(View.VISIBLE);
			slideFromTop(imageView);
			cameraView.startPreview();
		}
	};


	void initViews() {
		cameraView = (CameraPreview) findViewById(R.id.cameraPreview);
		captureButton = (Button)findViewById(R.id.captureButton);
		imageView = (ImageView)findViewById(R.id.imageView);
		imageView.setVisibility(View.INVISIBLE);
	}
	
	void initListeners() {
		captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v(TAG,"Capture button clicked");
				cameraView.takePicture(pictureCallback);
			}
		});
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (v.getVisibility() == View.VISIBLE) {
					slideToButtom(imageView);
					v.setVisibility(View.INVISIBLE);
				}
				
			}
		});
	}
	
	public void slideFromTop(View v) {
		TranslateAnimation animate = new TranslateAnimation(0, 0, -v.getHeight(), 0);
		animate.setDuration(500);
		v.setAnimation(animate);
	}

	public void slideToButtom(View v) {
		TranslateAnimation animate = new TranslateAnimation(0, 0, 0, v.getHeight());
		animate.setDuration(500);
		v.setAnimation(animate);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_layout);
		initViews();
		initListeners();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		cameraView.openCamera();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}