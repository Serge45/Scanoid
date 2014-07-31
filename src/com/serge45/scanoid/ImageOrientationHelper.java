package com.serge45.scanoid;

import android.content.Context;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class ImageOrientationHelper {
	static String TAG = "OrientationHelper";
	
	static int getNeedRotationAngle(Context context, int cameraOrientation) {
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		if (display.getRotation() == Surface.ROTATION_0) {
			return 90 - cameraOrientation;
		} else if (display.getRotation() == Surface.ROTATION_90) {
			return 0 - cameraOrientation;
		} else if (display.getRotation() == Surface.ROTATION_180) {
			return 0 - cameraOrientation;
		} else if (display.getRotation() == Surface.ROTATION_270) {
			return 180 - cameraOrientation;
		} else {
			return 0;
		}
		
	}
}
