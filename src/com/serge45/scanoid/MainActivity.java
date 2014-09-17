package com.serge45.scanoid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mburman.fileexplore.FileExplore;
import com.serge45.scanoid.CameraViewActivity;

public class MainActivity extends Activity {
    static String TAG = "Main";
    private Button cameraButton;
    private Button documentButton;
    private Button testJniButton;

    void initViews() {
        cameraButton = (Button) findViewById(R.id.imageList);
        documentButton = (Button) findViewById(R.id.viewDocButton);
        testJniButton = (Button) findViewById(R.id.jni_button); 
    }

    void initListeners() {
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Camera button clicked");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CameraViewActivity.class);
                startActivity(intent);
            }
        });

        documentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Document button clicked");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ImageListView.class);
                startActivity(intent);
            }
        });
        
        testJniButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, JniTestActivity.class);
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
