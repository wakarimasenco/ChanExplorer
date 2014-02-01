package co.wakarimasen.chanexplorer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import co.wakarimasen.chanexplorer.imageboard.Parser;
import co.wakarimasen.chanexplorer.imageboard.Post;
import co.wakarimasen.chanexplorer.imageboard.Parser.BannedException;
import co.wakarimasen.chanexplorer.imageboard.Parser.ChanParserException;
import co.wakarimasen.chanexplorer.imageboard.Parser.NotFoundException;

import co.wakarimasen.chanexplorer.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class AutoUpdater extends Service {
	private int NOTIFICATION = 120913;
	private NotificationManager mNM;

	private BoardsPagerAdapter mAdapter;

	public class AutoUpdaterBinder extends Binder {
		AutoUpdater getService() {
			return AutoUpdater.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new AutoUpdaterBinder();

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		//android.util.Log.d("CHAN", "Received create...");
		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this,
				AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		//android.util.Log.d("CHAN", "Received start...");
		if (!MainActivity.isGold(getApplicationContext())) {
			mgr.cancel(pi);
			mNM.cancel(NOTIFICATION);
			stopSelf();
			return START_NOT_STICKY;
		}
		try {
			int time = Integer.parseInt(PrefsActivity.getSetting(this,
					PrefsActivity.KEY_INTERVAL, "0"));
			if (time == 0) {
				mgr.cancel(pi);
				mNM.cancel(NOTIFICATION);
				stopSelf();
				return START_NOT_STICKY;
			}
			int unit = Integer.parseInt(PrefsActivity.getSetting(this,
					PrefsActivity.KEY_INTERVAL_UNIT, "0"));
			switch (unit) {
			case 0:
				time *= 1000;
				break;
			case 1:
				time *= 1000 * 60;
				break;
			case 2:
				time *= 1000 * 60 * 60;
				break;
			default:
				time *= 1000 * 1000 * 1000;
				break;
			}
			showNotification();
			if (mAdapter != null) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					ChanPage cp = mAdapter.getChanFragmentByPosition(i);
					if (cp != null) {
						if (cp.getThreadId() != -1) {
							String url = Http.Chan.threadURL(cp.getBoard(),
									cp.getThreadId());
							(new ThreadLoader((ThreadFragment) cp.getFragment()))
									.execute(url);
						}
					} else {
						
					}
				}
			} else {
				//android.util.Log.d("CHAN", "nulll...");
			}

			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + time, pi);
		} catch (NumberFormatException e) {
			//e.printStackTrace();
			mNM.cancel(NOTIFICATION);
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	public static class AlarmReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals("co.wakarimasen.chanexplorer.STOP_UPDATER")) {
				Editor e = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
				e.putString(PrefsActivity.KEY_INTERVAL, "0");
				e.commit();
			}

			Intent i = new Intent(context, AutoUpdater.class);
			context.startService(i);

		}
	}

	public BoardsPagerAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(BoardsPagerAdapter adap) {
		mAdapter = adap;
	}

	private void showNotification() {
		int time = Integer.parseInt(PrefsActivity.getSetting(this,
				PrefsActivity.KEY_INTERVAL, "0"));
		int unit = Integer.parseInt(PrefsActivity.getSetting(this,
				PrefsActivity.KEY_INTERVAL_UNIT, "0"));
		String unit_s;
		switch (unit) {
		case 0:
			unit_s = "second";
			break;
		case 1:
			unit_s = "minute";
			break;
		case 2:
			unit_s = "hour";
			break;
		default:
			unit_s = "years";
			break;
		}
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification

		CharSequence text = getResources().getQuantityString(
				R.plurals.auto_updater, time, time, unit_s);
		Intent notificationIntent = new Intent(this, AlarmReceiver.class);
		notificationIntent.setData(Uri.parse("chanexplorer://action/stop_service"));
		notificationIntent.setAction("co.wakarimasen.chanexplorer.STOP_UPDATER");
		
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		Notification notification = builder
				.setContentIntent(pi).setSmallIcon(R.drawable.ic_auto_updater)
				.setTicker(text).setWhen(System.currentTimeMillis())
				.setOngoing(true).setContentTitle("ChanExplorer")
				.setContentText(text).getNotification();

		mNM.notify(NOTIFICATION, notification);

	}

	private static class ThreadLoader extends
			AsyncTask<String, Integer, Post[]> {
		ThreadFragment mFragment;
		

		public ThreadLoader(ThreadFragment f) {
			mFragment = f;
		}

		@Override
		protected Post[] doInBackground(String... url) {

			try {
				String data = Http.getRequestAsString(url[0], "");

				Post[] posts = Parser.parse(data, false, null, 0, mFragment.getTheme().green_text);

				return posts;
			} catch (BannedException e) {
				publishProgress(R.string.status_banned);
			} catch (NotFoundException e) {
				publishProgress(R.string.status_404);
			} catch (ChanParserException e) {
				publishProgress(R.string.status_error);
			} catch (FileNotFoundException e) {
				publishProgress(R.string.status_404);
			} catch (MalformedURLException e) {
				publishProgress(R.string.status_error);
			} catch (IOException e) {
				publishProgress(R.string.status_error);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... error) {
			if (mFragment != null)
				mFragment.showError(error[0]);
		}

		@Override
		protected void onPostExecute(Post[] result) {
			if (mFragment != null && !mFragment.isDeleted()) {
				if (result != null)
					mFragment.setPosts(result, true, true);
				mFragment.finishLoading();
			}
		}

	}
}
