package co.wakarimasen.chanexplorer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.wakarimasen.chanexplorer.imageboard.Board;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import co.wakarimasen.chanexplorer.R;

public class PhoneHomeFragment extends Fragment  implements ViewPager.OnPageChangeListener, TabController, ActionBar.TabListener {
	
	ViewPager mViewPager;
	BoardsPagerAdapter mBoardsPagerAdapter;
	private PagerTitleStrip mTitleStrip;
	Theme mTheme = Theme.Holo;
	//SparseArray<ChanPage> mFragments;
	
	public PhoneHomeFragment() {
		super();
		setRetainInstance(true);
	}
	
	public static PhoneHomeFragment newInstance() {
		PhoneHomeFragment f = new PhoneHomeFragment();

        return f;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_main, container, false);
		mViewPager = (ViewPager) v.findViewById(R.id.mainPager);
		mTitleStrip = (PagerTitleStrip) v.findViewById(R.id.pager_title_strip);
		mBoardsPagerAdapter = new BoardsPagerAdapter(getActivity().getSupportFragmentManager(),  PreferenceManager.getDefaultSharedPreferences(getActivity()), this);
		mBoardsPagerAdapter.addBoardsPage();
        mViewPager.setAdapter(mBoardsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
		return v;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			if (savedInstanceState.getParcelable("BoardsAdapter.State") != null)
				mBoardsPagerAdapter.restoreState(savedInstanceState.getParcelable("BoardsAdapter.State"), Fragment.SavedState.class.getClassLoader());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setTheme(PrefsActivity.getTheme(getActivity(), false));
	}

	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putParcelable("BoardsAdapter.State", mBoardsPagerAdapter.saveState());
	}
	
	public void selectOrAddThread(Board b, int threadId, int postId) {
		int idx = mBoardsPagerAdapter.indexOfChanPage(b, threadId);
		if (idx == -1)
			addThread(b, threadId, postId);
		else {
			mViewPager.setCurrentItem(idx);
			((ThreadFragment)mBoardsPagerAdapter.getChanFragmentByPosition(idx)).gotoPost(b, threadId, postId, false, true);
		}
	}
	
	public void addThread(Board b, int threadId, int postId) {
		mBoardsPagerAdapter.addThread(b, threadId, postId);
		mViewPager.setCurrentItem(mBoardsPagerAdapter.getCount()-1);
	}
	
	public boolean isBoardsPage() {
		return mViewPager.getCurrentItem() == 0;
	}
	
	public void refreshPage(int position) {
		mBoardsPagerAdapter.getChanFragmentByPosition(position).refresh();
	}
	
	public void removePage(int position) {
		mBoardsPagerAdapter.removeThread(position);
	}
	
	public int getCurrentPage() {
		return mViewPager.getCurrentItem();
	}
	
	public ChanPage getCurrentChanPage() {
		return (mBoardsPagerAdapter.getChanFragmentByPosition(getCurrentPage()));
	}
	
	public void setCurrentPage(int item) {
		if (((MainActivity)getActivity()).getSlidingMenu().isBehindShowing()) {
			((MainActivity)getActivity()).getSlidingMenu().showAbove();
		}
		mViewPager.setCurrentItem(item, true);
	}
	
	public void setCurrentPage(int item, boolean animate) {
		if (((MainActivity)getActivity()).getSlidingMenu().isBehindShowing()) {
			((MainActivity)getActivity()).getSlidingMenu().showAbove();
		}
		mViewPager.setCurrentItem(item, animate);
	}
	
	public void setTheme(Theme t) {
		mTheme = t;
		if (mTitleStrip != null) {
			mTitleStrip.setTextColor(t.pager_title_text);
			mTitleStrip.setBackgroundColor(t.pager_title_bg);
		}
		for (int i =0; i<mBoardsPagerAdapter.getCount(); i++) {
			ChanPage cp = mBoardsPagerAdapter.getChanFragmentByPosition(i);
			if (cp != null) {
				boolean worksafe = cp.getBoard() == null ? false : cp.getBoard().isWorksafe();
				cp.updateTheme(PrefsActivity.getTheme(getActivity(), worksafe));
			}
		}
	}
	
	public BoardsPagerAdapter getAdapter() {
		return mBoardsPagerAdapter;
	}
	
	public int getParentPage(int position) {
		if (position == 0) 
			return -1;
		if (mBoardsPagerAdapter.getChanFragmentByPosition(position).getThreadId() != -1) {
			return mBoardsPagerAdapter.indexOfChanPage(mBoardsPagerAdapter.getChanFragmentByPosition(position).getBoard(), -1);
		} else {
			return 0;
		}
	}
	
	public NewPost getCurrentNewPost() {
		return mBoardsPagerAdapter.getNewPost(getCurrentPage());
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}
	
	@Override
	public void onPageSelected(int position) {
		((MainActivity)getActivity()).getSlidingMenu().setSlidingEnabled(position != 0);
		((MainActivity)getActivity()).invalidateOptionsMenu();
		if (position != 0 && Theme.validTheme(PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_THEME, null)) == null) {
			Theme t = (mBoardsPagerAdapter.getChanFragmentByPosition(position)).getBoard().isWorksafe() ? 
					Theme.YotsubaBlue : Theme.Yotsuba;
			if (!mTheme.equals(t)) {
				((MainActivity)getActivity()).setThemeFromHome(t);
				mTheme = t;
				if (mTitleStrip != null) {
					mTitleStrip.setTextColor(mTheme.pager_title_text);
					mTitleStrip.setBackgroundColor(mTheme.pager_title_bg);
				}
				//mBoardsPagerAdapter.getChanFragmentByPosition(0).updateTheme(mTheme);
			}
		} else if (position == 0) {
			mBoardsPagerAdapter.getChanFragmentByPosition(0).updateTheme(mTheme);
		}
		if (MainActivity.isTablet)
			((MainActivity)getActivity()).getSupportActionBar().setSelectedNavigationItem(position);
	}
	
	
	public boolean onBackPressed() {
		return getCurrentChanPage().onBackPressed();
	}

	@Override
	public void addTab(String title) {
		ActionBar bar = ((MainActivity)getActivity()).getSupportActionBar();  
		bar.addTab(bar.newTab().setText(title).setTabListener(this));
		
	}

	@Override
	public void removeTab(int position) {
		ActionBar bar = ((MainActivity)getActivity()).getSupportActionBar();  
		bar.removeTabAt(position);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
}
