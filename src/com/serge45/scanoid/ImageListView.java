package com.serge45.scanoid;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.polites.android.GestureImageView;

import android.R.bool;
import android.R.integer;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageListView extends ListActivity {
	static String TAG = "ListImageView";
	static String expandedViewVisibilityTag = "expandedViewVisibility";
	static String expandedViewSrcImageIdxTag = "";

	private int currentShowImageIdx;
	private int expandedViewVisibility;
	private File path = null;
	private String[] fileList;
	private ImageLoader imageLoader;
	private GestureImageView expandedImageView;
	private ProgressBar expandedImageViewProgessBar;
	private Animator zoomViewAnimator = null;
	private int zoomViewAnimeDuration;

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
					ImageListItemView i = (ImageListItemView) v.getTag();
					displayExpandedImage(i.imageView, i.index);
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
		expandedImageView.setBackgroundColor(Color.rgb(0, 0, 0));

		expandedImageViewProgessBar = (ProgressBar) findViewById(R.id.expandedImageProgressBar);
		expandedImageViewProgessBar.setVisibility(View.GONE);
	}

	private void initListeners() {
		/*
		 * expandedImageView.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { v.setVisibility(View.GONE); }
		 * });
		 */
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
		zoomViewAnimeDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
	}

	private void initImageLoader() {
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).build();
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

	private void displayExpandedImage(final View v, final int idx) {
		String p = "file://" + path + File.separator + fileList[idx];
		currentShowImageIdx = idx;
		expandedImageView.setImageBitmap(null);
		imageLoader.displayImage(p, expandedImageView,
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						zoomExpandedImage(v, idx);
						expandedImageViewProgessBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {

					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						expandedImageViewProgessBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

					}
				});
		expandedImageView.setVisibility(View.VISIBLE);
	}

	private void zoomExpandedImage(final View view, int idx) {
		if (zoomViewAnimator != null) {
			zoomViewAnimator.cancel();
		}

		final Rect startBound = new Rect();
		final Rect endBound = new Rect();
		final Point globalOffsetPoint = new Point();

		view.getGlobalVisibleRect(startBound);
		findViewById(R.id.image_list_frame_layout).getGlobalVisibleRect(
				endBound, globalOffsetPoint);
		startBound.offset(-globalOffsetPoint.x, -globalOffsetPoint.y);
		endBound.offset(-globalOffsetPoint.x, -globalOffsetPoint.y);
		
		ImageView imageView = (ImageView) view;
		
		if (imageView.getDrawable() != null) {
			Bitmap image = ((BitmapDrawable) ((ImageView) view).getDrawable())
					.getBitmap();
			float imageRatio = (float) (image.getWidth()) / image.getHeight();
			float viewRatio = (float) startBound.width() / startBound.height();
			float imageToViewScale = 1f;

			if (imageRatio > viewRatio) {
				imageToViewScale = (float) startBound.width() / image.getWidth();
				float startHeight = image.getHeight() * imageToViewScale;
				float deltaHeight = (startBound.height() - startHeight) / 2;
				startBound.top += deltaHeight;
				startBound.bottom -= deltaHeight;
			} else {
				imageToViewScale = (float) startBound.height() / image.getHeight();
				float startWidth = image.getWidth() * imageToViewScale;
				float deltaWidth = (startBound.width() - startWidth) / 2;
				startBound.left += deltaWidth;
				startBound.right -= deltaWidth;
			}
		}

		float startScale;

		if ((float) endBound.width() / endBound.height() > (float) startBound
				.width() / startBound.height()) {
			startScale = (float) startBound.height() / endBound.height();
			float startWidth = startScale * endBound.width();
			float deltaWidth = (startWidth - startBound.width()) / 2;
			startBound.left -= deltaWidth;
			startBound.right += deltaWidth;
		} else {
			startScale = (float) startBound.width() / endBound.width();
			float startHeight = startScale * endBound.height();
			float deltaHeight = (startHeight - startBound.height()) / 2;
			startBound.top -= deltaHeight;
			startBound.bottom += deltaHeight;
		}

		view.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		AnimatorSet set = new AnimatorSet();

		set.play(
				ObjectAnimator.ofFloat(expandedImageView, View.X,
						startBound.left, endBound.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
						startBound.top, endBound.top))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
						startScale, 1f))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
						startScale, 1f));
		set.setDuration(zoomViewAnimeDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				animation = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				animation = null;
			}
		});

		set.start();
		zoomViewAnimator = set;

		final float startScaleFinal = startScale;

		expandedImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (zoomViewAnimator != null) {
					zoomViewAnimator.cancel();
				}

				AnimatorSet set = new AnimatorSet();

				set.play(
						ObjectAnimator.ofFloat(expandedImageView, View.X,
								startBound.left))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
								startBound.top))
						.with(ObjectAnimator.ofFloat(expandedImageView,
								View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator.ofFloat(expandedImageView,
								View.SCALE_Y, startScaleFinal));

				set.setDuration(zoomViewAnimeDuration);
				set.setInterpolator(new DecelerateInterpolator());

				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						view.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						animation = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						view.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						animation = null;
					}
				});
				set.start();
				zoomViewAnimator = set;
			}
		});
	}

	private void savePreference(boolean destroy) {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		if (destroy) {
			editor.putInt(expandedViewVisibilityTag, View.GONE);
		} else {
			editor.putInt(expandedViewVisibilityTag,
					expandedImageView.getVisibility());
		}
		editor.putInt(expandedViewSrcImageIdxTag, currentShowImageIdx);
		editor.commit();
	}

	private void loadPreference() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		expandedViewVisibility = preferences.getInt(expandedViewVisibilityTag,
				View.GONE);
		currentShowImageIdx = preferences
				.getInt(expandedViewSrcImageIdxTag, -1);
	}

	private void restoreExpandedView() {
		if (expandedViewVisibility == View.VISIBLE && currentShowImageIdx >= 0) {
			imageLoader.displayImage("file://" + path + File.separator + fileList[currentShowImageIdx], expandedImageView);
			
			expandedImageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					expandedImageView.setVisibility(View.GONE);
					
				}
			});

			expandedImageView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		savePreference(false);
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
		restoreExpandedView();
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
