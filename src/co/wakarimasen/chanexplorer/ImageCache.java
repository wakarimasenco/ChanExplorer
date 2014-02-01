package co.wakarimasen.chanexplorer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

public class ImageCache {
	private LoadingCache<String, Bitmap> mCache;
	private ConcurrentHashMap<String, List<WeakReference<ImageWaiter>>> mWaiters;
	private static final long MAX_WEIGHT = 5*1024*1024; // 5 MB
	private static final int MAX_TIME = 60;
	private static ImageCache mInstance = null;
	private DiskLruCache mDiskCache;
	private Bitmap EMPTY = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);

	public ImageCache(Context ctx) {
		mCache = CacheBuilder.newBuilder().maximumWeight(MAX_WEIGHT)
				.weigher(new Weigher<String, Bitmap>() {
			          public int weigh(String key, Bitmap bmp) {
			        	  return bmp.getRowBytes() * bmp.getHeight();
			            }
			          })
				.expireAfterAccess(MAX_TIME, TimeUnit.MINUTES)
				.removalListener(new RemovalListener<String, Bitmap>() {
					@Override
					public void onRemoval(
							RemovalNotification<String, Bitmap> removal) {
						if (!removal.getCause().equals(RemovalCause.REPLACED)) {
							removal.getValue().recycle();
							mWaiters.remove(removal.getKey());
						}
					}
				}).build(new CacheLoader<String, Bitmap>() {
					@Override
					public Bitmap load(String key) { // no checked exception
						ImageLoader il = new ImageLoader();
						il.execute(key);
						return EMPTY;
					}
				});
		mWaiters = new ConcurrentHashMap<String, List<WeakReference<ImageWaiter>>>();
		
		int cacheSz = 25 * 1024 * 1024;
		try {
			cacheSz = Integer.parseInt(PrefsActivity.getSetting(ctx,
					PrefsActivity.KEY_CACHE_SIZE, "25")) * 1024 * 1024;
		} catch (NumberFormatException e) {
		}
		cacheSz = (int)Math.ceil(cacheSz * .3);
		if (cacheSz == 0) {
			cacheSz = (int)Math.ceil(25 * 1024 * 1024 * .3);
		}
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/.chanexplorer/thumbs");
		dir.mkdirs();
		try {
			mDiskCache = DiskLruCache.open(dir, 10, 1, cacheSz);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initialize(Context ctx) {
		mInstance = new ImageCache(ctx);
	}
	
	public static void initializeIfNull(Context ctx) {
		if (mInstance == null) {
			initialize(ctx);
		}
	}

	public static Bitmap get(String key, ImageWaiter w) {
		return mInstance.getBitmap(key, w);
	}
	
	public static boolean empty(Bitmap bm) {
		return bm == mInstance.EMPTY;
	}
	
	public static void remove(String key) {
		mInstance.invalidate(key);
	}
	
	public void invalidate(String key) {
		mCache.invalidate(key);
	}

	public Bitmap getBitmap(String key, ImageWaiter w) {
		if (w != null) {
			List<WeakReference<ImageWaiter>> waiters = mWaiters.get(key);
			if (waiters == null) {
				waiters = new ArrayList<WeakReference<ImageWaiter>>();
				waiters.add(new WeakReference<ImageWaiter>(w));
				mWaiters.put(key, waiters);
			} else if (waiters.size() == 0 && mCache.getIfPresent(key) != null) {
				// There's nobody waiting, so its done.
			} else {
				waiters.add(new WeakReference<ImageWaiter>(w));
			}
		}
		Bitmap r;
		try {
			r = mCache.get(key);
		} catch (ExecutionException e) {
			return null;
		}
		return r;
	}

	private class ImageLoader extends AsyncTask<String, Void, Bitmap> {
		private String mKey;

		@Override
		protected Bitmap doInBackground(String... key) {
			mKey = key[0];
			String hash;
			
			try {
				byte[] bytes = MessageDigest.getInstance("MD5").digest(
						mKey.toLowerCase().getBytes("UTF-8"));
				StringBuilder sb = new StringBuilder(2 * bytes.length);
				for (byte b : bytes) {
					sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
					sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
				}
				hash = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				hash = mKey.substring(mKey.length()-32, mKey.length());
			} catch (UnsupportedEncodingException e) {
				hash = mKey.substring(mKey.length()-32, mKey.length());
			}
			
			try {
				DiskLruCache.Snapshot snapshot = mDiskCache.get(hash);
				if (snapshot == null) {
					InputStream data = Http.getRequest(key[0], "");
					DiskLruCache.Editor creator = mDiskCache.edit(hash);
					if (creator == null)
						return null;
					OutputStream out = null;
					out = new BufferedOutputStream(
							creator.newOutputStream(0), 4096);
					byte[] buffer = new byte[8 * 1024];
					int bytesRead;
					int sz = 0;
					while ((bytesRead = data.read(buffer)) > -1) {
						out.write(buffer, 0, bytesRead);
						sz += bytesRead;
					}
					if (sz == 0) {
						// download failed.
						data.close();
						out.close();
						creator.abort();
						mDiskCache.flush();
						return null;
					}
					out.close();
					mDiskCache.flush();
					creator.commit();
					snapshot = mDiskCache.get(hash);
					data.close();
				}
				if (snapshot == null) {
					return null;
				}
				FileInputStream is = (FileInputStream) snapshot.getInputStream(0);
				Bitmap x;
				x = BitmapFactory.decodeFileDescriptor(is.getFD());
				is.close();
				snapshot.close();
				return x;
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				// Turn result into an X
				android.util.Log.d("Chan", "Failed to load: "+mKey);
				return;
			}
			mCache.put(mKey, result);
			List<WeakReference<ImageWaiter>> waiters = mWaiters.get(mKey);
			if (waiters != null) {
				for (WeakReference<ImageWaiter> waiter : waiters) {
					ImageWaiter iw = waiter.get();
					if (iw != null) {
						iw.onLoadedImage(mKey, result);
					}
				}
				waiters.clear();
			}
		}

	}

	public static interface ImageWaiter {
		public void onLoadedImage(String key, Bitmap image);
	}
}
