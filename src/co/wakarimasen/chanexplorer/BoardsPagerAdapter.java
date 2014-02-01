package co.wakarimasen.chanexplorer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import co.wakarimasen.chanexplorer.imageboard.Board;
import co.wakarimasen.chanexplorer.imageboard.Post;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BoardsPagerAdapter extends PagerAdapter {
    private static final String TAG = "BoardsPagerAdapter";
    private static final boolean DEBUG = false;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private List<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private List<FragmentInfo> mFragmentInfo = new ArrayList<FragmentInfo>();
    private Fragment mCurrentPrimaryItem = null;
    
    private SharedPreferences mSettings;
    private TabController mTabs;
    
    public BoardsPagerAdapter(FragmentManager fm, SharedPreferences settings, TabController tb) {
        mFragmentManager = fm;
        mSettings = settings;
        mTabs = tb;
    }
    
    public void addBoardsPage() {
    	mFragmentInfo.add(new FragmentInfo(null, -1, null, -1));
    	notifyDataSetChanged();
    	mTabs.addTab(mFragmentInfo.get(mFragmentInfo.size()-1).getTitle());
    }

    public void addThread(Board b, int threadId, int postId) {
    	mFragmentInfo.add(new FragmentInfo(b, threadId, 
    			new NewPost(mSettings.getString(PrefsActivity.KEY_DEFAULT_NAME, ""),
    					mSettings.getString(PrefsActivity.KEY_DEFAULT_EMAIL, ""),
    					NewPost.getPassword(mSettings)), postId));
    	mTabs.addTab(mFragmentInfo.get(mFragmentInfo.size()-1).getTitle());
    	notifyDataSetChanged();
    }
    
    public int indexOfChanPage(ChanPage cp) {
    	for (int i=0; i<mFragmentInfo.size(); i++) {
    		if (mFragmentInfo.get(i).belongsTo(cp)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public int indexOfChanPage(Board b, int threadId) {
    	if (b == null) return 0;
    	for (int i=0; i<mFragmentInfo.size(); i++) {
    		if (b.equals(mFragmentInfo.get(i).getBoard()) && threadId == mFragmentInfo.get(i).getThreadId()) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public ChanPage getChanFragment(String title) {
    	return (ChanPage) mFragmentManager.findFragmentByTag(title);
    }
    
    public ChanPage getChanFragmentByPosition(int position) {
    	return (ChanPage) mFragmentManager.findFragmentByTag(mFragmentInfo.get(position).getTitle());
    }
    
    public NewPost getNewPost(int position) {
    	return mFragmentInfo.get(position).getNewPost();
    }
    
    public void usePosts(int position, Post[] posts) {
    	mFragmentInfo.get(position).setPosts(posts);
    }
    
   public List<String> getTitles() {
	   List<String> r = new ArrayList<String>();
	   for (FragmentInfo info : mFragmentInfo) {
		   r.add(info.getTitle());
	   }
	   return r;
   }
    
    public void removeThread(int position) {
    	// Check if we have a fragment at this position,
    	// if so, destroy it.
    	getChanFragmentByPosition(position).setDeleted();
    	if (mFragments.get(position) != null) {
    		destroyItem(null, position, mFragments.get(position));
    	}
    	// Then get rid of the remaining refernces
    	mFragmentInfo.remove(position);
    	mFragments.remove(position);
    	mSavedState.remove(position);
    	mTabs.removeTab(position);
    	notifyDataSetChanged();
    }
    
    /**
     * Return the Fragment associated with a specified position.
     */
    public Fragment getItem(int position) {
    	if (mFragmentInfo.get(position).getBoard() == null) {
    		return new BoardsFragment();
    	} else {
    		return (new ThreadFragment()).setNewPost(mFragmentInfo.get(position).getNewPost());
    	}
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
    	return mFragmentInfo.get(position).getTitle();
    }

    @Override
    public void startUpdate(View container) {
    }
    


    public void setArguments(Fragment fragment, int position) {
    	Bundle args = new Bundle();
    	if (mFragmentInfo.get(position).getBoard() != null) {
    		args.putString("board", mFragmentInfo.get(position).getBoard().getId());
    		args.putInt("threadId", mFragmentInfo.get(position).getThreadId());
    		args.putInt("postId", mFragmentInfo.get(position).getPostId());
    	}
        fragment.setArguments(args);
    }
    
    @Override
    public Object instantiateItem(View container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position); 
        if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            } else {
            	setArguments(fragment, position);
            }
        }
        else {
        	setArguments(fragment, position);
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment, mFragmentInfo.get(position).getTitle());
        if (mFragmentInfo.get(position).getPosts() != null) {
        	if (!mFragmentInfo.get(position).getPosts().equals(((ThreadFragment)fragment).getPosts())) {
        		((ThreadFragment)fragment).setPosts(mFragmentInfo.get(position).getPosts(), true, false);
        		mFragmentInfo.get(position).setPosts(null); 
        	}
        }
        return fragment;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (getItemPosition(object) == -2) {
        	return;
        }
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment)object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        
        mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        mFragments.set(position, null);

        mCurTransaction.remove(fragment);
    }
    
    @Override
	public int getCount() {
		return mFragmentInfo.size();
	}
    
    @Override
    public int getItemPosition(Object object) {
    	if (object instanceof ChanPage) {
    		int idx = indexOfChanPage((ChanPage)object);
	    	return (idx == -1) ? -2 : idx;
    	} else {
    		return super.getItemPosition(object);
    	}
    }

    @Override
    public void setPrimaryItem(View container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(View container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i=0; i<mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        if (state == null) {
            state = new Bundle();
        }
        FragmentInfo[] fis = new FragmentInfo[mFragmentInfo.size()];
        mFragmentInfo.toArray(fis);
        state.putParcelableArray("__fragment_infomation", fis);
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle)state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            Parcelable[] fis = bundle.getParcelableArray("__fragment_infomation");
            mSavedState.clear();
            mFragments.clear();
            mFragmentInfo.clear();
            if (fss != null) {
                for (int i=0; i<fss.length; i++) {
                    mSavedState.add((Fragment.SavedState)fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key: keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
            if (fis != null) {
            	for (int i=0; i<fis.length; i++) {
            		mFragmentInfo.add((FragmentInfo)fis[i]);
            		if (i != 0) {
            			mTabs.addTab(((FragmentInfo)fis[i]).getTitle());
            			if (i < mFragments.size() && mFragments.get(i) != null) {
            				((ChanPage)mFragments.get(i)).setNewPost(((FragmentInfo)fis[i]).getNewPost());
            			}
            		}
            	}
            }
        }
    }
    
    public static class FragmentInfo implements Parcelable {
    	private Board mBoard = null;
    	private int mThreadId = -1;
    	private NewPost mNewPost = null;
    	private int mPostId = -1;
    	private Post[] usePosts = null;
    	
    	public FragmentInfo(Board b, int threadId, NewPost np, int postId) {
    		mBoard = b;
    		mThreadId = threadId;
    		mNewPost = np;
    		mPostId = postId;
    	}
    	
    	public FragmentInfo(Parcel in) {
            readFromParcel(in);
        }
    	
    	public Board getBoard() {
    		return mBoard;
    	}
    	public int getThreadId() {
    		return mThreadId;
    	}
    	
    	public NewPost getNewPost() {
    		return mNewPost;
    	}
    	
    	public int getPostId() {
    		return mPostId;
    	}
    	
    	public boolean isThread() {
    		return mThreadId != -1;
    	}
    	
    	public boolean belongsTo(ChanPage cp) {
    		return mThreadId == cp.getThreadId() && ((mBoard == null) ? (mBoard == cp.getBoard()) : mBoard.equals(cp.getBoard()));
    	}
    	public String getTitle() {
    		if (mThreadId != -1) {
    			return String.format("/%s/%d", mBoard.getId(), mThreadId);
    		} else if (mBoard != null) {
    			return String.format("/%s/ - %s", mBoard.getId(), mBoard.getName());
    		} else {
    			return "Boards";
    		}
    	}
    	
    	public void setPosts(Post[] p) {
    		usePosts = p;
    	}
    	
    	public Post[] getPosts() {
    		return usePosts;
    	}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (mBoard != null)
				dest.writeString(mBoard.getId());
			else
				dest.writeString(null);
			dest.writeInt(mThreadId);
			dest.writeParcelable(mNewPost, 0);
		}
		
		public void readFromParcel(Parcel in) {
	       String boardId = in.readString();
	       if (boardId != null) {
	    	   mBoard = Board.getBoardById(boardId);
	       } else {
	    	   mBoard = null;
	       }
	       mThreadId = in.readInt();
	       mNewPost = in.readParcelable(NewPost.class.getClassLoader());
	    }
		
		public static final Parcelable.Creator<FragmentInfo> CREATOR = new Parcelable.Creator<FragmentInfo>() {
	        @Override
			public FragmentInfo createFromParcel(Parcel in) {
	            return new FragmentInfo(in);
	        }
	 
	        @Override
			public FragmentInfo[] newArray(int size) {
	            return new FragmentInfo[size];
	        }
	    };
    } 

	
}