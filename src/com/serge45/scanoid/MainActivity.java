package com.serge45.scanoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.serge45.scanoid.CameraViewActivity;

public class MainActivity extends Activity {
	private
		Button cameraButton;
	
	void initViews() {
		cameraButton = (Button) findViewById(R.id.cameraButton);
	}
	
	void initListeners() {
		cameraButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.v("main", "Camera button clicked");
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, CameraViewActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initListeners();
	}
}
