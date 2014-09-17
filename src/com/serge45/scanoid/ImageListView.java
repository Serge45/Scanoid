package com.serge45.scanoid;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.R.integer;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageListView extends ListActivity {
    static String TAG = "ListImageView";

    private File path = null;
    private String[] fileList;
    private ImageLoader imageLoader;

    // --Adaptor--------------------------
    class ImageListAdaptor extends BaseAdapter {
        private LayoutInflater inflater;

        // Item
        public class ImageListItemView {
            ImageView imageView;
            TextView textView;
            ProgressBar progressBar;
            int index;

            public ImageListItemView(ImageView v, TextView t, ProgressBar p,
                    int idx) {
                imageView = v;
                textView = t;
                progressBar = p;
                index = idx;
            }
        }

        // end

        public ImageListAdaptor(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return fileList.length;
        }

        @Override
        public Object getItem(int position) {
            return fileList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_image_item,
                        parent, false);
            }

            final ImageListItemView item = new ImageListItemView(
                    (ImageView) convertView.findViewById(R.id.imageItemView),
                    (TextView) convertView.findViewById(R.id.imageIdText),
                    (ProgressBar) convertView.findViewById(R.id.progressBar),
                    position);

            convertView.setTag(item);
            item.textView.setText(String.valueOf(position + 1));
            String p = "file://" + path + File.separator + fileList[position];

            imageLoader.displayImage(p, item.imageView,
                    new ImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            item.imageView.setVisibility(View.GONE);
                            item.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                View view, Bitmap loadedImage) {
                            item.imageView.setVisibility(View.VISIBLE);
                            item.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri,
                                View view) {

                        }
                    });

            ViewGroup.LayoutParams params = convertView.getLayoutParams();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            if (params != null) {
                params.height = size.x;
                convertView.setLayoutParams(params);
            }

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.v(TAG, "Image item clicked");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("dialog_image_view");
                    
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    int idx = ((ImageListItemView) v.getTag()).index;
                    ImageViewDialog newImageDialog = ImageViewDialog.newInstance(fileList[idx]);
                    newImageDialog.show(ft, "dialog_image_view");
                }
            });
            return convertView;
        }

    }

    private void initViews() {
    }

    private void initListeners() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_list);
        path = new File(Environment.getExternalStorageDirectory()
                + File.separator + getResources().getString(R.string.app_name));
        initViews();
        initListeners();
        loadFileList();
        initImageLoader();
        setListAdapter(new ImageListAdaptor(this));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initImageLoader() {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).build();
        imageLoader = ImageLoader.getInstance();
        
        if (!imageLoader.isInited()) {
            imageLoader.init(configuration);
        }
    }

    private void loadFileList() {
        try {
            if (!path.exists()) {
                path.mkdirs();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    String fullName = sel.toString();
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile())
                            && (!sel.isHidden())
                            && (fullName.substring(
                                    fullName.lastIndexOf(".") + 1).toLowerCase(
                                    Locale.ENGLISH).equals("jpg"));

                }
            };

            fileList = path.list(filter);
        }
    }



    private void savePreference() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    }

    private void loadPreference() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        savePreference();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreference();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
}
