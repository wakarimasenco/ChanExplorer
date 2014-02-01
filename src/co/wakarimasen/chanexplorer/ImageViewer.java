package co.wakarimasen.chanexplorer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import co.wakarimasen.android.graphics.GifDrawable;
import co.wakarimasen.chanexplorer.Http.HttpStream;

import co.wakarimasen.chanexplorer.R;

@SuppressWarnings("deprecation")
public class ImageViewer extends FragmentActivity {
	private ViewPager mPager;
	private int mPosition = 0;
	private Parcelable[] mImages;
	private DiskLruCache mCache;
	private DownloadManager mDownMan = null;
	private Gallery mGallery;
	private float start_x = 0;
	private float start_y = 0;

	AlphaAnimation fadeIn;
	AlphaAnimation fadeOut;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer);
		mDownMan = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		mImages = getIntent().getParcelableArrayExtra("IMAGES");
		mPosition = getIntent().getIntExtra("POSITION", 0);

		int cacheSz = 25 * 1024 * 1024;
		try {
			cacheSz = Integer.parseInt(PrefsActivity.getSetting(this,
					PrefsActivity.KEY_CACHE_SIZE, "25")) * 1024 * 1024;
		} catch (NumberFormatException e) {
		}
		cacheSz = (int)Math.ceil(cacheSz * .7);
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/.chanexplorer/cache");
		dir.mkdirs();
		try {
			mCache = DiskLruCache.open(dir, 10, 1, cacheSz);
		} catch (IOException e) {
			e.printStackTrace();
			finish();
		}
		mGallery = (Gallery) findViewById(R.id.thumb_gallery);
		mGallery.setAdapter(new GalleryImageAdapter());
		boolean gall = PrefsActivity.getSetting(this,
				PrefsActivity.KEY_QUICKSCROLL, true);
		mGallery.setVisibility(gall ? View.VISIBLE : View.GONE);
		mGallery.setEnabled(gall);
		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mPager.setCurrentItem(position);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}});
		mPager = (ViewPager) findViewById(R.id.mainPager);
		final ImageAdapter ia = new ImageAdapter(getSupportFragmentManager());
		mPager.setAdapter(ia);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				barVisible(false);
			}

			@Override
			public void onPageSelected(int arg0) {
				((TextView) findViewById(R.id.image_top_text)).setText(String
						.format("%d of %d", arg0 + 1, mImages.length));
				((TextView) findViewById(R.id.image_bottom_text))
						.setText(((ImageInfo) mImages[arg0]).filename);
				mPosition = arg0;
				mGallery.setSelection(arg0, true);
				ia.sendUpdatePosition(mPosition);
				
				// Gif require more memory...

			}
		});
		mPager.setCurrentItem(mPosition);
		
		((TextView) findViewById(R.id.image_top_text)).setText(String.format(
				"%d of %d", mPosition + 1, mImages.length));
		((TextView) findViewById(R.id.image_bottom_text))
				.setText(((ImageInfo) mImages[mPosition]).filename);

		((ImageButton) findViewById(R.id.image_viewer_back))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();

					}
				});

		((ImageButton) findViewById(R.id.image_viewer_save))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						registerForContextMenu(findViewById(R.id.image_viewer_save));
						openContextMenu(findViewById(R.id.image_viewer_save));
						unregisterForContextMenu(findViewById(R.id.image_viewer_save));

					}
				});

		fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setDuration(220);
		fadeIn.setFillAfter(true);

		fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setDuration(150);
		fadeOut.setFillAfter(true);
		fadeOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				(findViewById(R.id.image_top_bar)).setVisibility(View.GONE);
				(findViewById(R.id.image_bottom_bar)).setVisibility(View.GONE);
				mGallery.setVisibility(View.GONE);
				mGallery.setEnabled(false);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		
		//ia.sendUpdatePosition(mPosition);

	}
	
	public void setBottomText() {
		((TextView) findViewById(R.id.image_bottom_text))
		.setText(((ImageInfo) mImages[mPosition]).filename);
	}

	public ImageInfo getImageInfoByUrl(String url) {
		for (Parcelable p : mImages) {
			if (((ImageInfo)p).url == url) {
				return (ImageInfo)p;
			}
		}
		return null;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			start_x = ev.getX();
			start_y = ev.getY();
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			int xDiff = (int) Math.abs(ev.getX() - start_x);
			int yDiff = (int) Math.abs(ev.getY() - start_y);
			if (xDiff < 3 && yDiff < 3)
				barVisible(!isBarVisible());
		}
		return super.dispatchTouchEvent(ev);
	}

	protected void barVisible(boolean visible) {
		if (visible == isBarVisible()) {
			return;
		}
		AlphaAnimation fade = (visible) ? fadeIn : fadeOut;
		if (visible) {
			(findViewById(R.id.image_top_bar)).setVisibility(View.VISIBLE);
			(findViewById(R.id.image_bottom_bar)).setVisibility(View.VISIBLE);
			mGallery.setVisibility(View.VISIBLE);
			mGallery.setEnabled(true);
		}
		if (!fade.hasStarted() || fade.hasEnded()) {
			findViewById(R.id.image_top_bar).startAnimation(fade);
			findViewById(R.id.image_bottom_bar).startAnimation(fade);
			mGallery.startAnimation(fade);
		}
		// ( findViewById(R.id.image_top_bar)).setVisibility((visible) ?
		// View.VISIBLE : View.GONE);
		// ( findViewById(R.id.image_bottom_bar)).setVisibility((visible) ?
		// View.VISIBLE : View.GONE);
	}

	protected boolean isBarVisible() {
		return (findViewById(R.id.image_top_bar)).getVisibility() == View.VISIBLE;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (v.equals(findViewById(R.id.image_viewer_save))) {
			menu.clear();
			menu.add(0, 0, 0, "Save Image");
			menu.add(0, 1, 1, "Save All Images").setEnabled(
					MainActivity.isGold(this));
		}
	}

	@TargetApi(11)
	private DownloadManager.Request Request11(Uri uri, Uri dest, ImageInfo image) {
		DownloadManager.Request req = (new DownloadManager.Request(uri)
				.setTitle("Downloading " + image.filename + "...")
				.setNotificationVisibility(
						DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setDestinationUri(dest));
		req.allowScanningByMediaScanner();
		return req;
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getGroupId() == 0) {
			if (item.getItemId() == 0) {
				ImageInfo image = (ImageInfo) mImages[mPosition];
				Uri uri = Uri.parse(image.url);
				File dir = new File(Environment.getExternalStorageDirectory(),
						PrefsActivity.getSetting(this,
								PrefsActivity.KEY_DL_DIR, "downloads"));
				dir.mkdirs();
				File path = new File(dir, image.filename);
				int i = 2;
				while (path.exists()) {
					String name = image.filename.substring(0,
							image.filename.lastIndexOf('.'));
					String ext = image.filename.substring(image.filename
							.lastIndexOf('.'));
					String newfname = String.format("%s(%d)%s", name, i++, ext);
					path = new File(dir, newfname);
				}
				Uri dest = Uri.fromFile(path);
				if (Build.VERSION.SDK_INT >= 11) {
					mDownMan.enqueue(Request11(uri, dest, image));
				} else {
					mDownMan.enqueue((new DownloadManager.Request(uri)
							.setTitle("Downloading " + image.filename + "...")
							.setShowRunningNotification(true)
							.setDestinationUri(dest)));
				}
			} else if (item.getItemId() == 1) {
				ImageInfo image = (ImageInfo) mImages[mPosition];
				// Uri uri = Uri.parse(image.url);
				File dir = new File(Environment.getExternalStorageDirectory(),
						PrefsActivity.getSetting(this,
								PrefsActivity.KEY_DL_DIR, "downloads"));
				if (image.threadId == -1) {
					dir = new File(dir,
							String.format("%s", image.board.getId()));
				} else {
					dir = new File(dir, String.format("%s.%d",
							image.board.getId(), image.threadId));
				}
				dir.mkdirs();
				DownloadReceiver onComplete = new DownloadReceiver(mDownMan,
						dir, mImages);
				registerReceiver(onComplete, new IntentFilter(
						DownloadManager.ACTION_DOWNLOAD_COMPLETE));
				onComplete.downloadImage((ImageInfo) mImages[0], 0);
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private static class DownloadReceiver extends BroadcastReceiver {
		private int mDownloadPosition = 0;
		private Parcelable[] mImages;
		private File mDest;
		private DownloadManager mDownMan = null;

		public DownloadReceiver(DownloadManager downMan, File dest,
				Parcelable[] images) {
			mDest = dest;
			mImages = images;
			mDownMan = downMan;
		}

		public void downloadImage(ImageInfo image, int pos) {
			Uri uri = Uri.parse(image.url);
			File path = new File(mDest, image.filename);
			int i = 2;
			while (path.exists()) {
				String name = image.filename.substring(0,
						image.filename.lastIndexOf('.'));
				String ext = image.filename.substring(image.filename
						.lastIndexOf('.'));
				String newfname = String.format("%s(%d)%s", name, i++, ext);
				path = new File(mDest, newfname);
			}
			Uri dest = Uri.fromFile(path);
			if (Build.VERSION.SDK_INT >= 11) {
				mDownMan.enqueue(Request11(uri, dest, image, pos));
			} else {
				mDownMan.enqueue((new DownloadManager.Request(uri).setTitle(
						String.format("Downloading %s (%d/%d)...",
								image.filename, pos + 1, mImages.length))
						.setShowRunningNotification(true)
						.setDestinationUri(dest)));
			}
			mDownloadPosition = pos + 1;
		}

		@TargetApi(11)
		private DownloadManager.Request Request11(Uri uri, Uri dest,
				ImageInfo image, int pos) {
			DownloadManager.Request req = (new DownloadManager.Request(uri)
					.setTitle(
							String.format("Downloading %s (%d/%d)...",
									image.filename, pos + 1, mImages.length))
					.setNotificationVisibility(
							(pos + 1 == mImages.length) ? DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
									: DownloadManager.Request.VISIBILITY_VISIBLE)
					.setDestinationUri(dest));
			req.allowScanningByMediaScanner();
			return req;
		}

		@Override
		public void onReceive(Context ctxt, Intent intent) {
			if (mDownloadPosition < mImages.length)
				downloadImage((ImageInfo) mImages[mDownloadPosition],
						mDownloadPosition);
		}
	}

	final class ImageAdapter extends FragmentStatePagerAdapter {
		private List<WeakReference<Handler>> selectedHandlers;
		
		public ImageAdapter(FragmentManager fm) {
			super(fm);
			selectedHandlers = new ArrayList<WeakReference<Handler>>();
		}

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public Fragment getItem(int position) {
			ImageFragment f = new ImageFragment();
			
			f.imageInfo = (ImageInfo) mImages[position];
			f.position = position;
			f.mCache = mCache;
			//f.h = new ImageFragment.MessageHandler(f);
			if (mPosition == position) {
				f.loadClear = true;
			}
			//selectedHandlers.add(new WeakReference<Handler>(f.h));
			return f;
		}
		
		public void addHandler(Handler h) {
			selectedHandlers.add(new WeakReference<Handler>(h));
		}
		
		
		
		public void sendUpdatePosition(int position) {
			//Log.d("ChanExplorer", "Sending position"+position);
			//int z = 0;
			for (int i=selectedHandlers.size()-1; i>=0; i--) {
				if (selectedHandlers.get(i).get() == null) {
					selectedHandlers.remove(i);
				} else {
					Message m = Message.obtain();
					m.arg1 = position;
					selectedHandlers.get(i).get().sendMessage(m);
					//z++;
				}
			}
			//Log.d("ChanExplorer", "Sent to "+z+".");
		}
	}

	public class GalleryImageAdapter extends BaseAdapter {

		public GalleryImageAdapter() {

		}

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public Object getItem(int position) {
			return mImages[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
			ImageCache.initializeIfNull(parent.getContext());
	        if (convertView == null) {

	        	convertView = new ImageView(ImageViewer.this);
	        	Resources r = getResources();
	        	int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
	        	convertView.setLayoutParams(new Gallery.LayoutParams(px, px));
	        	((ImageView)convertView).setScaleType(ImageView.ScaleType.CENTER_INSIDE);

	        }
	        String imageKey = ( (ImageInfo) mImages[position]).thumb;
			if (imageKey.startsWith("//")) {
				imageKey = String.format("http:%s", imageKey);
			}
			ImageView imv = (ImageView) convertView;

			convertView.setTag(imageKey);

			Bitmap im = ImageCache.get(imageKey, new ImageCache.ImageWaiter() {
				ImageView mImv;
				@Override
				public void onLoadedImage(String key, Bitmap image) {
					if (mImv.getTag().equals(key)) {
						mImv.setImageBitmap(image);
					}
				}
				
				public ImageCache.ImageWaiter setImageView(ImageView imv) {
					mImv = imv;
					return this;
				}
			}.setImageView(imv));
			if (!ImageCache.empty(im)) {
				imv.setImageBitmap(im);
			} else {
				Runnable animate = new Runnable() {
					ImageView mImv;
					@Override
					public void run() {
						Drawable ld =mImv.getDrawable();
						if (ld instanceof LayerDrawable) {
							for (int i = 0; i < ((LayerDrawable)ld).getNumberOfLayers(); i++) {
								((LayerDrawable)ld).getDrawable(i)
										.setLevel(
												(int) (((int) (System.currentTimeMillis() % 4000) / (float) 4000) * 10000));
							}
							mImv.postDelayed(this, 350);
						}
					}
					public Runnable setImageView(ImageView imv) {
						mImv = imv;
						return this;
					}
				}.setImageView(imv);
				imv.setImageResource(Theme.Holo.progress_large);
				imv.postDelayed(animate, 350);
			}

	        return convertView;
	    }
	}

	public static class ImageFragment extends Fragment {
		ImageInfo imageInfo = null;
		Integer position = -1;
		LoadingImageView mImg = null;
		ImageLoader mTask = null;
		DiskLruCache mCache = null;
		Handler h;
		boolean sizeLoaded = false;
		boolean detached;
		boolean loadClear = false;

		// mImageLoaded = false;

		public ImageFragment() {
			super();
			setRetainInstance(true);
		}

		public void setCache(DiskLruCache cache) {
			mCache = cache;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			h = new ImageFragment.MessageHandler(this);
			((ImageViewer.ImageAdapter)((ImageViewer)getActivity()).mPager.getAdapter()).addHandler(h);
			mImg = new LoadingImageView(getActivity());

			mImg.setPadding(6, 6, 6, 6);
			mImg.setLayoutParams(new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
					Gravity.CENTER));
			mImg.setLoading(-1, -1);
			mImg.setTag(false);
			// ImageViewHelperURL.setUrlDrawable((ImageView) img, url,
			// R.drawable.no_image) ;

			mTask = new ImageLoader(getActivity().getResources(),
					false, loadClear, true);
			mTask.execute(imageInfo.url);
			detached = false;
			
			loadClear = false;
			return mImg;
		}
		
		public void loadClearImage() {
			if (!detached) {
				if ((Boolean)mImg.getTag()) {
					return;
				}
				if (mTask.isGif()) {
					return;
				}
				if (mTask.isDownloading()) {
					mTask.mLoadFull = true;
					return;
				}
				if (mTask.getStatus() == AsyncTask.Status.FINISHED || mTask.getStatus() == AsyncTask.Status.PENDING) {
					
				} else if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
					mTask.cancel(false);	
				}
				mTask = new ImageLoader(getActivity().getResources(),
						false, true, false);
				mTask.execute(imageInfo.url);
			}
		}
		
		public void loadThumbImage() {
			if (!detached) {
				if (!(Boolean)mImg.getTag()) {
					return;
				}
				if (mTask.isGif()) {
					return;
				}
				if (mTask.isDownloading()) {
					mTask.mLoadFull = true;
					return;
				}
				if (mTask.getStatus() == AsyncTask.Status.FINISHED || mTask.getStatus() == AsyncTask.Status.PENDING) {
					
				} else if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
					mTask.cancel(false);	
				}
				mTask = new ImageLoader(getActivity().getResources(),
						false, false, false);
				mTask.execute(imageInfo.url);
			}
		}
	    
	    static class MessageHandler extends Handler {
	    	private final WeakReference<ImageFragment> mImf;
	    	
	    	MessageHandler(ImageFragment imf) {
	            mImf = new WeakReference<ImageFragment>(imf);
	        }
	    	
	    	@Override
	        public void handleMessage(Message msg) {
	    		//Log.d("ChanExplorer", "Got position"+msg.arg1);
	            if (mImf.get() != null && msg.arg1 == mImf.get().position) {
	            	mImf.get().loadClearImage();
	            } else if (mImf.get() != null) {
	            	mImf.get().loadThumbImage();
	            }
	        }
	    }

		@Override
		public void onDetach() {
			super.onDetach();
			// android.util.Log.d("CHAN", "on detach called");
			mTask.cancel(false);
			detached = true;
			if (!mImg.isLoading()) {
				if (mImg.getDrawable() instanceof GifDrawable) {
					GifDrawable d = (GifDrawable) mImg.getDrawable();
					mImg.setImageDrawable(null);
					d.recycle();
					mTask = null;
					mImg = null;
					//System.gc();
					// android.util.Log.d("CHAN", "recycling gif");
				} else if (mImg.getDrawable() instanceof co.wakarimasen.android.graphics.GifDrawable) {
					co.wakarimasen.android.graphics.GifDrawable d = (co.wakarimasen.android.graphics.GifDrawable) mImg.getDrawable();
					mImg.setImageDrawable(null);
					d.recycle();
					mTask = null;
					mImg = null;
				}
				else if (mImg.getDrawable() instanceof BitmapDrawable) {
					BitmapDrawable d = (BitmapDrawable) mImg.getDrawable();
					mImg.setImageDrawable(null);
					if (d.getBitmap() != null)
						d.getBitmap().recycle();
					mTask = null;
					mImg = null;
					//System.gc();
					// android.util.Log.d("CHAN", "recycling");
				}
			}
		}

		private class ImageLoader extends AsyncTask<String, Integer, Drawable> {
			private Resources mRes;
			double maxRes;
			//final static long maxSize = 8 * 1024 * 1024;
			private boolean mGifs;
			private boolean mLoadFull;
			private boolean mDownloading = false;
			private boolean mGif = false;
			private boolean mUpdateProgress = true;
			private String nullReason = "no reason";
			// private DiskLruCache mCache;

			public ImageLoader(Resources res, boolean disableGif, boolean maxQuality, boolean updateProgress) {
				mRes = res;
				mGifs = !disableGif;
				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay()
						.getMetrics(dm);
				maxRes = dm.widthPixels * dm.heightPixels * 4;
				mLoadFull = maxQuality;
				mDownloading = true;
				mUpdateProgress = updateProgress;
			}
			
			public boolean isDownloading() {
				return mDownloading;
			}
			
			public boolean isGif() {
				return mGif;
			}

			@Override
			protected Drawable doInBackground(String... url) {
				nullReason = "No Reason.";

				Drawable d = null;
				mDownloading = true;
				mGif = url[0].endsWith(".gif") && mGifs;
				try {
					byte[] bytes = MessageDigest.getInstance("MD5").digest(
							url[0].toLowerCase().getBytes("UTF-8"));
					StringBuilder sb = new StringBuilder(2 * bytes.length);
					for (byte b : bytes) {
						sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
						sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
					}
					String hash = sb.toString();
					DiskLruCache.Snapshot snapshot = mCache.get(hash);
					// android.util.Log.d("CHNA", hash + "==>" + snapshot);
					if (snapshot == null) {
						
						InputStream data = Http.getRequest(url[0], "");
						DiskLruCache.Editor creator = mCache.edit(hash);
						if (creator == null) {
							nullReason = "Creator was null.";
							return null;
						}
						publishProgress(0, 100);
						OutputStream out = null;
						out = new BufferedOutputStream(
								creator.newOutputStream(0), 4096);
						int max = ((HttpStream) data).length();
						int current = 0;
						publishProgress(current, max);
						byte[] buffer = new byte[8 * 1024];
						int bytesRead;
						while ((bytesRead = data.read(buffer)) > -1) {
							out.write(buffer, 0, bytesRead);
							current += bytesRead;
							publishProgress(current, max);
							if (isCancelled()) {
								nullReason = "cancelled";
								out.close();
								data.close();
								creator.abort();
								mCache.flush();
								return null;
							}
						}
						if (current == 0) {
							// The download failed.
							out.close();
							data.close();
							creator.abort();
							mCache.flush();
							nullReason = "download failed";
							return null;
						}
						out.close();
						creator.commit();
						mCache.flush();
						snapshot = mCache.get(hash);
						data.close();
					}
					if (snapshot == null) {
						// lolwat
						publishProgress(-1, -1);
						nullReason = "snapshot was null?";
						return null;
					}
					// android.util.Log.d("CHNA", hash + "==>" + snapshot);
					mDownloading = false;
					publishProgress(-1, -1);
					FileInputStream is = (FileInputStream) snapshot.getInputStream(0);
					if (url[0].endsWith(".gif") && mGifs) {
						is.close();
						snapshot.close();
						File file = new File(mCache.getDirectory(), hash+".0");
						//android.util.Log.d("ChanExplorer", "JVM Loading "+file.toString());
						d = co.wakarimasen.android.graphics.GifDrawable.gifFromFile(mRes, file.toString());
						/*
						if (isCancelled())
							return null;
						GifDrawable.GifInfo g = GifDrawable.infoFromStream(is);
						is.close();
						snapshot.close();
						snapshot = mCache.get(hash);
						is = new BufferedInputStream(
								snapshot.getInputStream(0), 4092);
						int resolution = g.getWidth() * g.getHeight();
						int skipcount = 1;
						if (4 * resolution * g.getFrames() > maxSize) {
							double maxFrames = maxSize
									/ (double) (4 * resolution);
							skipcount = (int) Math.ceil(g.getFrames()
									/ maxFrames);
						}
						if (isCancelled())
							return null;
						d = GifDrawable.loadGifFromStream(mRes, is, skipcount);
						is.close();
						if (isCancelled()) {
							((GifDrawable) d).recycle();
							return null;
						}*/

					} else {
						final BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						options.inScaled = false;
						if (isCancelled()) {
							is.close();
							nullReason = "cancelled";
							return null;
						}
						BitmapFactory.decodeFileDescriptor(is.getFD(), null, options);
						//BitmapFactory.decodeStream(is, null, options);
						//is.close();
						//snapshot.close();
						//snapshot = mCache.get(hash);
						int resolution = options.outHeight * options.outWidth;

						options.inSampleSize = 1;
						if (resolution > maxRes) {
							// options.inSampleSize = (int)
							// Math.pow(2,Math.ceil(Math.log((resolution/maxRes))
							// / Math.log(2)));
							// if (options.inSampleSize > 16) {
							options.inSampleSize = 2;
							// }
							// android.util.Log.d("CHAN", "Reduced size to: "
							// + options.inSampleSize);
						}
						
						
						if (!mLoadFull) {
							//Log.d("ChanExplorer", "Loading image "+url[0]+" sample.");
							double mres = maxRes/16;
							if (resolution > mres) {
								//int newSS = (int) Math.ceil(resolution
								//		/ maxRes);
								if (resolution < maxRes/8) {
									options.inSampleSize = 2;
								} else if (resolution < maxRes/4) {
									options.inSampleSize = 4;
								} else {
									options.inSampleSize = 8;//Math.max(newSS, options.inSampleSize);
								}
							}
							//options.inSampleSize = 8;
						} else {
							//Log.d("ChanExplorer", "Loading image "+url[0]+" full.");
						}
						
						//options.inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(options.inSampleSize)/Math.log(2)));
						//Log.d("ChanExplorer", "Sample Size:"+options.inSampleSize+" "+mLoadFull);
						options.inJustDecodeBounds = false;
						for (int t=0; ; t++) {
							try {
								if (isCancelled()) {
									is.close();
									nullReason = "cancelled";
									return null;
								}
								d = new BitmapDrawable(mRes,
										BitmapFactory.decodeFileDescriptor(is.getFD(), null, options));
								break;
							} catch (OutOfMemoryError err) {
								switch (t) {
								case 0:
									options.inSampleSize += 1;
									Log.e("ChanExplorer", "Image Loading Failure - 0");
									break;
								case 1:
									options.inSampleSize += 1;
									Log.e("ChanExplorer", "Image Loading Failure - 1");
									break;
								case 2:
									options.inSampleSize += 1;
									Log.e("ChanExplorer", "Image Loading Failure - 2");
									break;
								default:
									return null;
								}
								options.inSampleSize = (int) Math.pow(2, Math.ceil(Math.log(options.inSampleSize)/Math.log(2)));
								System.gc();
								is.close();
								snapshot.close();
								snapshot = mCache.get(hash);
								is = (FileInputStream) snapshot.getInputStream(0);
							}
						}

						is.close();
						if (isCancelled()) {
							if (((BitmapDrawable) d).getBitmap() != null)
								((BitmapDrawable) d).getBitmap().recycle();
							nullReason = "cancelled";
							return null;
						}
					}

					if (snapshot != null)
						snapshot.close();
					nullReason = "got to end, but d was null";
					return d;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					publishProgress(0, 1);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					publishProgress(0, 1);
				} catch (IOException e) {
					e.printStackTrace();
					publishProgress(0, 1);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					publishProgress(0, 1);
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... progress) {
				if (mUpdateProgress || progress[1] != -1)
					mImg.setLoading(progress[0], progress[1]);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				if (result == null) {
					Log.e("ChanExplorer", "Image fialed to load. "+nullReason);
					return;	
				}
				if (mImg == null) {
					Log.e("ChanExplorer", "Target image was null.");
					return;
				}
				mImg.finishLoading();
				if (result instanceof AnimationDrawable) {
					int width = ((BitmapDrawable) ((AnimationDrawable) result)
							.getFrame(0)).getBitmap().getWidth();
					int height = ((BitmapDrawable) ((AnimationDrawable) result)
							.getFrame(0)).getBitmap().getHeight();
					if (width > mImg.getWidth() || height > mImg.getHeight()) {
						mImg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					} else {
						mImg.setScaleType(ImageView.ScaleType.CENTER);
					}
					// android.util.Log.d("IMAGE", "width: " + width);
					// android.util.Log.d("IMAGE", "height: " + height);
					((FrameLayout.LayoutParams) mImg.getLayoutParams()).gravity = Gravity.CENTER;
					((FrameLayout.LayoutParams) mImg.getLayoutParams()).width = width;
					((FrameLayout.LayoutParams) mImg.getLayoutParams()).height = height;
					// android.util.Log.d("IMAGE", "Gif Size: "
					// + ((GifDrawable) result).byteSize());
					// android.util.Log.d("IMAGE", "Gif Size MB: "
					// + ((GifDrawable) result).byteSize() / 1024 / 1024);
					// android.util.Log.d("IMAGE", "Frame count: "
					// + ((GifDrawable) result).getNumberOfFrames());
				} else {
					((FrameLayout.LayoutParams) mImg.getLayoutParams()).width = LayoutParams.MATCH_PARENT;
					((FrameLayout.LayoutParams) mImg.getLayoutParams()).height = LayoutParams.MATCH_PARENT;
					// int sz = ((BitmapDrawable) result).getBitmap().getWidth()
					// * ((BitmapDrawable) result).getBitmap().getHeight()
					// * 4;
					// android.util.Log.d("IMAGE", "Im Size: " + sz);
					// android.util.Log.d("IMAGE", "Im Size MB: " + sz / 1024
					// / 1024);

				}
				
				float[] m = null;
				
				if (mImg.getDrawable() != null && mImg.getDrawable() instanceof BitmapDrawable) {
					BitmapDrawable bm = (BitmapDrawable) mImg.getDrawable();
					mImg.setImageDrawable(null);
					if (bm.getBitmap() != null)
						bm.getBitmap().recycle();
					
					if (mLoadFull)
						mImg.swapImageDrawable(result);
					else
						mImg.setImageDrawable(result);
				} else {
					mImg.setImageDrawable(result);
				}
				mImg.setTag(mLoadFull);
				/*if (mLoadFull) {
					mImg.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.LIGHTEN);
				} else {
					mImg.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.LIGHTEN);
				}*/
				

				if (result instanceof AnimationDrawable) {
					mImg.post(new Runnable() {
						@Override
						public void run() {
							((AnimationDrawable) mImg.getDrawable())
									.setOneShot(false);
							((AnimationDrawable) mImg.getDrawable()).start();

						}
					});
				} else {
					mImg.setScaleType(ImageView.ScaleType.MATRIX);
				}
			}

		}
	}

}