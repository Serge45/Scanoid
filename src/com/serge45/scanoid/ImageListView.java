package com.serge45.scanoid;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.polites.android.GestureImageView;

import android.app.ListActivity;
import android.content.Context;
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
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageListView extends ListActivity {
	static String TAG = "ListImageView";
	
	private File path = null;
	private String[] fileList;
	private ImageLoader imageLoader;
	private GestureImageView expandedImageView;
	private ProgressBar expandedImageViewProgessBar;
	
	//----------------------------
	class ImageListAdaptor extends BaseAdapter {
		private LayoutInflater inflater;
		
		//Item
		public class ImageListItemView {
			ImageView imageView;
			TextView textView;
			ProgressBar progressBar;
			int index;
			
			public ImageListItemView(ImageView v, TextView t, ProgressBar p, int idx) {
				imageView = v;
				textView = t;
				progressBar = p;
				index = idx;
			}
		}
		//end

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
				convertView = inflater.inflate(R.layout.list_image_item, parent, false);
			}

			final ImageListItemView item = new ImageListItemView((ImageView)convertView.findViewById(R.id.imageItemView),
															(TextView)convertView.findViewById(R.id.imageIdText),
															(ProgressBar)convertView.findViewById(R.id.progressBar),
															position);
			convertView.setTag(item);
			item.textView.setText(String.valueOf(position + 1));
			String p = "file://" + path + File.separator + fileList[position];

			imageLoader.displayImage(p, item.imageView, new ImageLoadingListener() {
				
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
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					item.imageView.setVisibility(View.VISIBLE);
					item.progressBar.setVisibility(View.GONE);
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					
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
					ImageListItemView i = (ImageListItemView) v.getTag();
					displayExpandedImage(i.index);
				}
			});
			return convertView;
		}
		
	}

	private void initViews() {
		expandedImageView = (GestureImageView) findViewById(R.id.expandedImageView);
		expandedImageView.setMaxScale(10f);
		expandedImageView.setMinScale(0.1f);
		expandedImageView.setFocusable(true);
		expandedImageView.setFocusableInTouchMode(true);
		expandedImageView.setScaleType(ScaleType.CENTER);
		expandedImageView.setVisibility(View.GONE);
		
		expandedImageViewProgessBar = (ProgressBar) findViewById(R.id.expandedImageProgressBar);
		expandedImageViewProgessBar.setVisibility(View.GONE);
	}
	
	private void initListeners() {
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_list);
		path = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name));
		initViews();
		initListeners();
		loadFileList();
		initImageLoader();
		setListAdapter(new ImageListAdaptor(this));
	}
	
	private void initImageLoader() {
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(configuration);
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
					return (sel.isFile()) && 
						   (!sel.isHidden()) && 
						   (fullName.substring(fullName.lastIndexOf(".")+1).toLowerCase(Locale.ENGLISH).equals("jpg"));

				}
			};
			
			fileList = path.list(filter);
		}
	}
	
	private void displayExpandedImage(int idx) {
		String p = "file://" + path + File.separator + fileList[idx];

		imageLoader.displayImage(p, expandedImageView, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				expandedImageView.setAlpha(0f);
				expandedImageViewProgessBar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				expandedImageView.setAlpha(1f);
				expandedImageViewProgessBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				
			}
		});
		expandedImageView.setVisibility(View.VISIBLE);
	}
	
}
