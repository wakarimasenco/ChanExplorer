package co.wakarimasen.chanexplorer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.wakarimasen.chanexplorer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

@SuppressWarnings("deprecation")
public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	public final static String KEY_FAVORITE_BOARDS = "co.wakarimasen.chanexplorer.FAV_BOARDS";
	public final static String KEY_HIDDEN_BOARDS = "co.wakarimasen.chanexplorer.prefs.hidden_boards";
	public final static String KEY_DEFAULT_NAME = "co.wakarimasen.chanexplorer.prefs.default_name";
	public final static String KEY_DEFAULT_EMAIL = "co.wakarimasen.chanexplorer.prefs.default_email";
	public final static String KEY_DEFAULT_PASS = "co.wakarimasen.chanexplorer.prefs.default_password";
	public final static String KEY_INTERVAL = "co.wakarimasen.chanexplorer.prefs.auto_update_interval";
	public final static String KEY_INTERVAL_UNIT = "co.wakarimasen.chanexplorer.prefs.auto_updater_unit";
	public final static String KEY_INTERVAL_NOTIFY = "co.wakarimasen.chanexplorer.prefs.auto_updater_notify";
	public final static String KEY_CACHE_SIZE = "co.wakarimasen.chanexplorer.prefs.image_disk_cache";
	public final static String KEY_DL_DIR = "co.wakarimasen.chanexplorer.prefs.download_dir";
	public final static String KEY_DL_PRELOAD = "co.wakarimasen.chanexplorer.prefs.preload";
	public final static String KEY_GET_GOLD = "co.wakarimasen.chanexplorer.get_gold";
	// public final static String KEY_DISABLE_GIF =
	// "co.wakarimasen.chanexplorer.prefs.disable_gif";
	public final static String KEY_SPOILERS = "co.wakarimasen.chanexplorer.prefs.spoilers";
	public final static String KEY_THEME = "co.wakarimasen.chanexplorer.prefs.theme";
	public final static String KEY_TEXT_SZ = "co.wakarimasen.chanexplorer.prefs.text_size";
	public final static String KEY_REPLIES = "co.wakarimasen.chanexplorer.prefs.no_replies";
	public final static String KEY_QUICKSCROLL = "co.wakarimasen.chanexplorer.prefs.quickscroll";
	public final static String KEY_PASS_PIN = "co.wakarimasen.chanexplorer.prefs.4chanpass_pin";
	public final static String KEY_PASS_TOKEN = "co.wakarimasen.chanexplorer.prefs.4chanpass_token";
	public final static String KEY_USE_PASS = "co.wakarimasen.chanexplorer.prefs.4chanpass_enabled";
	public final static String KEY_4CHAN_PASS = "co.wakarimasen.chanexplorer.prefs.4chanpass";

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		updateSummary();
		findPreference(KEY_GET_GOLD).setOnPreferenceClickListener(
				new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// android.util.Log.d("CHAN", "LO");
						setResult(MainActivity.RESULT_GOLD);
						finish();// (MainActivity.REQ_CODE_PREFS);
						return true;
					}
				});
		if (MainActivity.isGold(this)) {
			findPreference(KEY_GET_GOLD).setEnabled(false);
			findPreference(KEY_GET_GOLD).setTitle("Thanks!");
			findPreference(KEY_GET_GOLD).setSummary("Thanks for your support!");
			findPreference("auto_update_group").setEnabled(true);
		} else {
			findPreference("auto_update_group").setEnabled(false);
		}
	}

	public static String getSetting(Context context, String key, String def) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(key, def);
	}

	public static boolean getSetting(Context context, String key, boolean def) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(key, def);
	}

	public static Theme getTheme(Context context, boolean worksafe) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return Theme.getTheme(settings.getString(KEY_THEME, null), worksafe);
	}

	protected void setSummaryAsValue(String key, String def) {
		if (key.equals(KEY_INTERVAL)) {
			String val = findPreference(key).getSharedPreferences().getString(
					key, def);
			if (val.equals("0")) {
				val = "Off";
			}
			findPreference(key).setSummary(val);
		} else if (key.equals(KEY_CACHE_SIZE)) {
			findPreference(key).setSummary(
					findPreference(key).getSharedPreferences().getString(key,
							def)
							+ " MB");
		} else if (key.equals(KEY_INTERVAL_UNIT)) {
			findPreference(key).setSummary(
					getResources().getStringArray(R.array.auto_options)[Math
							.min(2, Integer
									.parseInt(findPreference(key)
											.getSharedPreferences().getString(
													key, def)))]);
		} else if (key.equals(KEY_TEXT_SZ)) {
			findPreference(key).setSummary(
					getResources().getStringArray(R.array.textsz_options)[Math
							.min(1, Integer
									.parseInt(findPreference(key)
											.getSharedPreferences().getString(
													key, def)))]);
		} else if (key.equals(KEY_REPLIES)) {
			findPreference(key).setSummary(
					getResources().getStringArray(R.array.replies_options)[Math
							.min(5, Integer
									.parseInt(findPreference(key)
											.getSharedPreferences().getString(
													key, def)))]);
		} else {
			findPreference(key).setSummary(
					findPreference(key).getSharedPreferences().getString(key,
							def));

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void updateSummary() {
		setSummaryAsValue(KEY_DEFAULT_NAME, "");
		setSummaryAsValue(KEY_DEFAULT_EMAIL, "");
		setSummaryAsValue(KEY_INTERVAL, "0");
		setSummaryAsValue(KEY_CACHE_SIZE, "25");
		setSummaryAsValue(KEY_DL_DIR, "downloads");
		setSummaryAsValue(KEY_INTERVAL_UNIT, "1");
		setSummaryAsValue(KEY_THEME, "Auto");
		setSummaryAsValue(KEY_REPLIES, "0");
		setSummaryAsValue(KEY_TEXT_SZ, "0");
		setSummaryAsValue(KEY_PASS_PIN, "");
		setSummaryAsValue(KEY_PASS_TOKEN, "");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updateSummary();
		if (key.equals(KEY_USE_PASS)) {
			if (sharedPreferences.getBoolean(KEY_USE_PASS, false)) {
				pd = ProgressDialog.show(this, "", "Authenticating pass...",
						true);
				String[] d = { sharedPreferences.getString(KEY_PASS_TOKEN, ""),
						sharedPreferences.getString(KEY_PASS_PIN, "") };
				(new PassChecker()).execute(d);
			}
		}
	}

	/*
	 * public void showDialog(String title, String message) { DialogFragment
	 * newFragment = AlertDialogFragment.newInstance(title, message);
	 * newFragment.show(getSupportFragmentManager(), "dialog.alert"); }
	 */

	public void showProgressDialog(String message) {

	}

	/*
	 * public void dismissProgressDialog() { DialogFragment df =
	 * ((DialogFragment) getSupportFragmentManager().findFragmentByTag(
	 * "dialog.progress")); if (df != null) df.dismiss(); }
	 */

	private class PassChecker extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... data) {
			Map<String, String> postData = new HashMap<String, String>();
			postData.put("act", "do_login");
			postData.put("id", data[0]);
			postData.put("pin", data[1]);
			try {
				String response = Http.postRequestAsString(
						"https://sys.4chan.org/auth", postData,
						"https://sys.4chan.org/auth");
				if (response.indexOf("Success! Your device is now authorized.") == -1) {
					return false;
				} else {
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Void... v) {

		}

		@Override
		protected void onPostExecute(Boolean result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					PrefsActivity.this);
			pd.dismiss();
			if (result) {
				builder.setMessage("4Chan Pass Support is enabled.").setTitle(
						"Success");
			} else {
				builder.setMessage("Failed to enable 4Chan Pass Support.")
						.setTitle("Error");
				Editor e = PrefsActivity.this.findPreference(KEY_USE_PASS)
						.getSharedPreferences().edit();
				e.putBoolean(KEY_USE_PASS, false);
				e.apply();
				((CheckBoxPreference) PrefsActivity.this
						.findPreference(KEY_USE_PASS)).setChecked(false);
			}
			builder.setNeutralButton("Ok", null);
			builder.create().show();
		}
	}

}
