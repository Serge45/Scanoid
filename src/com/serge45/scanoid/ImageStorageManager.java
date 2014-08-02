package com.serge45.scanoid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class ImageStorageManager {
	static String TAG = "StorageManager";
	String appName = null;

	public String defaultDirName = null; 
	
	public ImageStorageManager(Context context) {
		appName = context.getResources().getString(R.string.app_name);
		defaultDirName= Environment.getExternalStorageDirectory() + File.separator + appName + File.separator;
	}
	
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
	
	public void saveImage(int idx, Bitmap img) {
		File dir = new File(defaultDirName);

		if (!dir.exists()) {
			dir.mkdir();
		}
		
		String fileName = "img%04d.jpg";
		File imageFile = new File(defaultDirName + String.format(fileName, idx));
		FileOutputStream output = null;

		try {
			output = new FileOutputStream(imageFile);
			img.compress(Bitmap.CompressFormat.JPEG, 90, output);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (Throwable ignore) {
				
			}
		}
	}

}
