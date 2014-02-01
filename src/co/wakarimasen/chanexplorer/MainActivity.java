package co.wakarimasen.chanexplorer;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import co.wakarimasen.chanexplorer.imageboard.Board;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings.Secure;

import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import co.wakarimasen.chanexplorer.R;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

@SuppressLint("NewApi")
public class MainActivity extends SlidingFragmentActivity {
	public static final long serialVersionUID = 2551829628569953991L;
	public static final int MAIN_ACTIVITY_CONTEXT_GROUP = 9001;
	public static final int REQ_CODE_PICK_IMAGE = 9002;
	public static final int REQ_CODE_PREFS = 9003;

	public static final int RESULT_GOLD = 1111;
	
	private static final String PREF_GOLD_KEY = "wakarimasen.chanexplorer.gold";

	public NewPostView mNewPostView;
	
	private AutoUpdater mBoundService;
	
	private boolean mIsBound = false;
	
	private Theme mTheme = Theme.Holo;
	
	public static boolean isTablet = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//android.util.Log.d("CHANEXPLORE", String.format("Mem class: %d", ((android.app.ActivityManager)getSystemService( ACTIVITY_SERVICE )).getMemoryClass()));
		setContentView(R.layout.chanexplorer);
		ImageCache.initialize(this);
		
		isTablet = getResources().getBoolean(R.bool.isTablet);
				
		mNewPostView = (NewPostView) getLayoutInflater().inflate(
				R.layout.new_post, null);
		setBehindContentView(mNewPostView);
		mNewPostView.mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getHomeFragment().getCurrentPage() != 0) {
					getHomeFragment().getCurrentChanPage().uploadPost(
							mNewPostView);
				}
			}
		});
		mNewPostView.mBrowse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, REQ_CODE_PICK_IMAGE);
				
			}
		});
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadow);
		getSlidingMenu().setBehindOffsetRes(R.dimen.actionbar_home_width);
		getSlidingMenu().setSlidingEnabled(false);
		getSlidingMenu().setOnCloseListener(new SlidingMenu.OnCloseListener() {

			@Override
			public void onClose() {
				mNewPostView.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromInputMethod(mNewPostView.getWindowToken(),
						0);
				if (getHomeFragment().getCurrentPage() != 0)
					mNewPostView.saveNewPost(getHomeFragment()
							.getCurrentNewPost());

			}
		});
		getSlidingMenu().setOnOpenListener(new SlidingMenu.OnOpenListener() {

			@Override
			public void onOpen() {
				if (getHomeFragment().getCurrentPage() != 0)
					mNewPostView.setNewPost(getHomeFragment()
							.getCurrentChanPage().getBoard(), getHomeFragment()
							.getCurrentChanPage().getThreadId(),
							getHomeFragment().getCurrentNewPost());
			}
		});
		
		mTheme = PrefsActivity.getTheme(this, false);
		getSupportActionBar().setLogo(mTheme.action_icon);
		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mTheme.action_bar_bg));
		if (isTablet) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
		invalidateOptionsMenu();

		mNewPostView.requestFocus();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_USE_LOGO);

		// ((PagerTitleStrip)findViewById(R.id.pager_title_strip)).setTextSize(TypedValue.COMPLEX_UNIT_SP,
		// 13);
		// ActionBar bar = getSupportActionBar();

		// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// mTabsAdapter = new TabsAdapter(this, mViewPager);

			// mPurchaseDatabase = new PurchaseDatabase(this);
		// setupWidgets();

		// Check if billing is supported.
		
		
		
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		//android.util.Log.d("PREF", ""+requestCode+"ff "+resultCode);
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				String[] filePathColumn = { MediaColumns.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				mNewPostView.setImage(filePath);
			} else {
				mNewPostView.setImage("");
			}
			if (!getSlidingMenu().isBehindShowing()) {
				getSlidingMenu().showBehind();
			}
			break;
		case REQ_CODE_PREFS:			
			setTheme(PrefsActivity.getTheme(this, (getHomeFragment().getCurrentChanPage() == null) ? false : getHomeFragment().getCurrentChanPage().isWorksafe()));
			if (((BoardsFragment) getHomeFragment().mBoardsPagerAdapter
					.getChanFragmentByPosition(0)) != null)
				((BaseAdapter) ((BoardsFragment) getHomeFragment().mBoardsPagerAdapter
						.getChanFragmentByPosition(0)).getListAdapter())
						.notifyDataSetChanged();
			if (resultCode == RESULT_GOLD) {
				showDialog("Error", "Couldn't connect to android market.");
			}
			try {
				//android.util.Log.d("CHAN", "Starting...");
				int time = Integer.parseInt(PrefsActivity.getSetting(this,
						PrefsActivity.KEY_INTERVAL, "0"));
				//android.util.Log.d("CHAN", "time..."+time);
				if (time != 0) {
					Intent i = new Intent(this, 
				            AutoUpdater.class);
					startService(i);
					bindService(i, mConnection, Context.BIND_AUTO_CREATE);
					mIsBound = true;
				}
			} catch (NumberFormatException e) {
				
			}
			
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getAction() == null) {
			return;
		}
		else if (intent.getAction()
				.equals("co.wakarimasen.chanexplorer.ACTION_VIEW")) {
			Board b = Board.getBoardById(intent.getStringExtra("Board"));
			int threadId = intent.getIntExtra("Thread", -1);
			int postId = intent.getIntExtra("Post", -1);
			getHomeFragment().selectOrAddThread(b, threadId, postId);
		} else if (intent.getAction().equals(
				"co.wakarimasen.chanexplorer.FIX_POST")) {
			Board b = Board.getBoardById(intent.getStringExtra("Board"));
			int threadId = intent.getIntExtra("Thread", -1);
			NewPost np = intent.getParcelableExtra("NewPostInfo");
			getHomeFragment().selectOrAddThread(b, threadId, -1);
			ChanPage cp = getHomeFragment().getCurrentChanPage();
			mNewPostView.setNewPost(cp.getBoard(), cp.getThreadId(), np);
			cp.setNewPost(np);
			mNewPostView.refreshCaptcha();
			getSlidingMenu().showBehind();
		} else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			Uri data = getIntent().getData();
			if (data == null) {
				return;
			}
			String host = data.getHost(); // "twitter.com"
			if (host.equals("boards.4chan.org")) {
				List<String> params = data.getPathSegments();
				if (params.size() == 1) {
					Board b = Board.getBoardById(params.get(0));
					if (b != null) {
						getHomeFragment().selectOrAddThread(b, -1, -1);
					}
				}
				if (params.size() == 3) {
					Board b = Board.getBoardById(params.get(0));
					if (b != null) {
						int threadId = Integer.parseInt(params.get(2));
						int postId = -1;
						if (data.getFragment() != null) {
							postId = Integer.parseInt(data.getFragment());
						}
						getHomeFragment().selectOrAddThread(b, threadId, postId);
					}
				}
			}
		}
	}

	public PhoneHomeFragment getHomeFragment() {
		return (PhoneHomeFragment) getSupportFragmentManager()
				.findFragmentByTag("chanexplorer.HomeViewPager");
	}
	
	public void setTheme(Theme t) {
		mTheme = t;
		getSupportActionBar().setLogo(mTheme.action_icon);
		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mTheme.action_bar_bg));
		getSupportActionBar();
		invalidateOptionsMenu();
		getHomeFragment().setTheme(mTheme);
	}
	
	public void setThemeFromHome(Theme t) {
		mTheme = t;
		getSupportActionBar().setLogo(mTheme.action_icon);
		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mTheme.action_bar_bg));
		invalidateOptionsMenu();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		//android.util.Log.d("CHAN", ""+menu.findItem(R.id.menu_reply));
		//android.util.Log.d("CHAN", ""+getHomeFragment());
		menu.findItem(R.id.menu_reply).setEnabled(
				!getHomeFragment().isBoardsPage());
		menu.findItem(R.id.menu_refresh).setEnabled(
				!getHomeFragment().isBoardsPage());
		menu.findItem(R.id.menu_close).setEnabled(
				!getHomeFragment().isBoardsPage());
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_reply).setEnabled(
				!getHomeFragment().isBoardsPage());
		menu.findItem(R.id.menu_refresh).setEnabled(
				!getHomeFragment().isBoardsPage());
		menu.findItem(R.id.menu_close).setEnabled(
				!getHomeFragment().isBoardsPage());
		
		menu.findItem(R.id.menu_reply).setIcon(mTheme.ic_action_reply);
		menu.findItem(R.id.menu_refresh).setIcon(mTheme.ic_action_refresh);
		menu.findItem(R.id.menu_close).setIcon(mTheme.ic_action_close);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getHomeFragment().setCurrentPage(0);
			return true;
		case R.id.menu_reply:
			toggle();
			return true;
		case R.id.menu_refresh:
			if (!getHomeFragment().isBoardsPage()) {
				getHomeFragment().refreshPage(
						getHomeFragment().getCurrentPage());
			}
			return true;
		case R.id.menu_close:
			if (!getHomeFragment().isBoardsPage()) {
				int currPage = getHomeFragment().getCurrentPage();
				// getHomeFragment().setCurrentPage(currPage-1);
				getHomeFragment().removePage(currPage);
			}
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
			intent.putExtra("IS_GOLD", isGold(MainActivity.this));
			startActivityForResult(intent, REQ_CODE_PREFS);
			return true;
		case R.id.menu_jump:
			registerForContextMenu(findViewById(R.id.phone_main));
			openContextMenu(findViewById(R.id.phone_main));
			unregisterForContextMenu(findViewById(R.id.phone_main));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		if (getSlidingMenu().isBehindShowing()) {
			getSlidingMenu().showAbove();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getSlidingMenu().isBehindShowing()) {
			getSlidingMenu().showAbove();
		}

	}

	public static String getFragmentTag(Board b, int threadId) {
		if (threadId == -1) {
			return String.format("/%s/", b.getId());
		} else {
			return String.format("/%s/%d", b.getId(), threadId);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		if (v.equals(findViewById(R.id.phone_main))) {
			menu.clear();
			int pos = 0;
			for (String title : getHomeFragment().getAdapter().getTitles()) {
				menu.add(MAIN_ACTIVITY_CONTEXT_GROUP, pos++, (pos - 1), title);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getGroupId() == MAIN_ACTIVITY_CONTEXT_GROUP) {
			getHomeFragment().setCurrentPage(item.getItemId());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		int parentPage = getHomeFragment().getParentPage(
				getHomeFragment().getCurrentPage());
		if (getHomeFragment().onBackPressed()) {
			return;
		} else if (parentPage != -1) {
			getHomeFragment().setCurrentPage(parentPage);
		} else {
			super.onBackPressed();
		}
	}

	public void showDialog(String title, String message) {
		DialogFragment newFragment = AlertDialogFragment.newInstance(title,
				message);
		newFragment.show(getSupportFragmentManager(), "dialog.alert");
	}

	public void showProgressDialog(String message) {
		DialogFragment newFragment = ProgressDialogFragment
				.newInstance(message);
		newFragment.show(getSupportFragmentManager(), "dialog.progress");
	}

	public void dismissProgressDialog() {
		DialogFragment df = ((DialogFragment) getSupportFragmentManager().findFragmentByTag(
				"dialog.progress"));
		if (df != null)
			df.dismiss();
	}

	public static class AlertDialogFragment extends DialogFragment {

		public static AlertDialogFragment newInstance(String title,
				String message) {
			AlertDialogFragment frag = new AlertDialogFragment();
			Bundle args = new Bundle();
			args.putString("title", title);
			args.putString("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String title = getArguments().getString("title");
			String message = getArguments().getString("message");

			return new AlertDialog.Builder(getActivity())
					.setTitle(title)
					.setMessage(message)
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									return;
								}
							}).create();
		}
	}

	public static class ProgressDialogFragment extends DialogFragment {

		public static ProgressDialogFragment newInstance(String message) {
			ProgressDialogFragment frag = new ProgressDialogFragment();
			Bundle args = new Bundle();
			args.putString("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setMessage(getArguments().getString("message"));
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			return dialog;
		}
	}

	/*
	 * Billing
	 */

	public static boolean isGold(Context context) {
		return true;
	}

	private final static String Key(Context ctx) {
		String id_s = Secure.getString(ctx.getContentResolver(),
				Secure.ANDROID_ID);
		if (id_s == null || id_s.length() == 0) {
			id_s = "78a10a7b33e0b0b5a7d3a89c1d260de1173a28fe";
		}
		byte[] id;
		try {
			id = MessageDigest.getInstance("MD5")
					.digest(id_s.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		byte[] key = new byte[id.length + 32];
		for (int i = 0; i < key.length; i++) {
			key[i] = (byte) ((id[i % id.length] ^ (id[i % id.length] * 0x01000193)) & 0xff);
		}
		StringBuilder sb = new StringBuilder(2 * key.length);
		for (byte b : key) {
			sb.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
			sb.append("0123456789ABCDEF".charAt((b & 0x0F)));
		}
		return sb.toString();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
	    @Override
		public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((AutoUpdater.AutoUpdaterBinder)service).getService();
	        mBoundService.setAdapter(getHomeFragment().getAdapter());

	    }

	    @Override
		public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	    }
	};
}
