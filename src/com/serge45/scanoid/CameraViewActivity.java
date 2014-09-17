package com.serge45.scanoid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

public class CameraViewActivity extends Activity {
    static String TAG = "CameraViewActivity";
    private CameraPreview cameraView = null;
    private ImageButton captureButton = null;
    private Bitmap lastCaptureImage = null;
    private int shuttedImageCount = 0;
    private ImageStorageManager imageManager;
    private String shutterTAG = "ShutterCount";
    private boolean isTakingPicture = false;

    private PictureCallback pictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            cameraView.stopPreview();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            if (lastCaptureImage != null) {
                lastCaptureImage.recycle();
            }

            lastCaptureImage = BitmapFactory.decodeByteArray(data, 0,
                    data.length, options);
            int angle = ImageOrientationHelper.getNeedRotationAngle(
                    getApplicationContext(), cameraView.getCameraOrientation());
            Matrix matrix = new Matrix();

            if (cameraView.isFrontCamera()) {
                angle = -angle;
            }

            matrix.postRotate(angle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(lastCaptureImage, 0, 0,
                    lastCaptureImage.getWidth(), lastCaptureImage.getHeight(),
                    matrix, true);
            lastCaptureImage = rotatedBitmap;
            cameraView.startPreview();
            cameraView.resizeToFitPreview();
            ++shuttedImageCount;
            showShuttedImageCount();

            if (imageManager.isExternalStorageWritable()) {
                String fileName = imageManager.saveImage(shuttedImageCount, lastCaptureImage);
                showTakenImage(fileName);
            }

            isTakingPicture = false;

        }
    };

    private void showShuttedImageCount() {
        String msg = "#" + shuttedImageCount + " image took";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void initViews() {
        cameraView = (CameraPreview) findViewById(R.id.cameraPreview);
        captureButton = (ImageButton) findViewById(R.id.captureButton);
    }

    void initListeners() {
        captureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v(TAG, "Capture button clicked");

                if (isTakingPicture) {
                    Log.v(TAG, "is taking picture");
                    return;
                }

                isTakingPicture = true;
                cameraView.takePicture(pictureCallback);
            }
        });
    }
    
    void showTakenImage(String fName) {
        Log.v(TAG, "Image item clicked");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog_image_view");

        if (prev != null) {
        ft.remove(prev);
        }
        ft.addToBackStack(null);
        ImageViewDialog newImageDialog = ImageViewDialog.newInstance(fName);
        newImageDialog.show(ft, "dialog_image_view");
    }

    private void loadPreferences() {
        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
        shuttedImageCount = preferences.getInt(shutterTAG, 0);
    }

    private void savePreferences() {
        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(shutterTAG, shuttedImageCount);
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        initViews();
        initListeners();
        imageManager = new ImageStorageManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPreferences();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadPreferences();
    }

    @Override
    protected void onResume() {
        loadPreferences();
        cameraView.initHolder();
        cameraView.openCamera();
        cameraView.startPreview();
        super.onResume();
    }

    @Override
    protected void onPause() {
        savePreferences();
        cameraView.stopCamera();
        super.onPause();
    }

    @Override
    protected void onStop() {
        savePreferences();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}