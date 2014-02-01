package co.wakarimasen.chanexplorer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import co.wakarimasen.chanexplorer.imageboard.Board;
import co.wakarimasen.chanexplorer.imageboard.Parser;
import co.wakarimasen.chanexplorer.imageboard.Post;
import co.wakarimasen.chanexplorer.imageboard.Parser.BannedException;
import co.wakarimasen.chanexplorer.imageboard.Parser.ChanParserException;
import co.wakarimasen.chanexplorer.imageboard.Parser.NotFoundException;

import co.wakarimasen.chanexplorer.R;

public class ThreadFragment extends ListFragment implements ChanPage,
		ThreadNavigator {

	Board mBoard = null;
	int mThreadId = -1;
	private int mPostId = -1;
	private int mPageNo = 0;
	Post[] mPosts = null;
	int mStatusError = -1;
	private boolean mLoading = false;
	private boolean mLoadingNext = false;
	private FrameLayout mView;

	// private ReplyState??
	private Theme mTheme = Theme.Holo;
	// private Object mState;
	private ThreadAdapter mThreadAdapter;
	private Animation mAnimSlideIn;
	private Animation mAnimSlideOut;
	private Animation mAnimDialogIn;
	private Animation mAnimDialogOut;
	private Animation mAnimFadeIn;
	private Animation mAnimFadeOut;
	private NewPost mNewPost;

	private TextView mStatusText;
	private ProgressBar mProgressBar;
	private ProgressBar mNextBar;
	private PostView mContextPostView;

	private View mOverlay;
	private PostView mOverlayPost;

	private boolean mLastVisible;
	private int mSelectedTop;
	private int mSelectedPos;

	private ListView mListView;

	private boolean mDeleted;

	public ThreadFragment() {
		super();
		mDeleted = false;
		mListView = null;
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = (FrameLayout) inflater.inflate(R.layout.thread_fragment,
				container, false);
		mStatusText = (TextView) mView.findViewById(R.id.status_text);
		mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_spinner);
		mOverlay = mView.findViewById(R.id.overlay);

		LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mOverlayPost = (PostView) vi.inflate(R.layout.post_view, null);
		mOverlayPost.setTheme(mTheme);
		mOverlayPost.setNavigator(ThreadFragment.this);
		mOverlayPost.mLinksBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mOverlayPost
						.setOnCreateContextMenuListener(new PostContextMenu(
								mOverlayPost, PostView.MODE_LINKS));
				getActivity().openContextMenu(mOverlayPost);
				// unregisterForContextMenu(mOverlayPost);

			}
		});
		mOverlayPost.mRepliesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mOverlayPost
						.setOnCreateContextMenuListener(new PostContextMenu(
								mOverlayPost, PostView.MODE_REPLY));
				getActivity().openContextMenu(mOverlayPost);

			}
		});
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);
		mView.addView(mOverlayPost, params);
		mOverlayPost.setVisibility(View.GONE);
		mOverlay.setVisibility(View.GONE);

		mOverlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//mOverlayPost.setVisibility(View.GONE);
				mOverlayPost.startAnimation(mAnimDialogOut);
				//mOverlay.setVisibility(View.GONE);

			}

		});
		
		mAnimFadeIn = new AlphaAnimation(0,1);
		mAnimFadeIn.setDuration(220);
		mAnimFadeIn.setFillAfter(true);
		
		mAnimFadeOut = new AlphaAnimation(1,0);
		mAnimFadeOut.setDuration(150);
		//mAnimFadeOut.setFillAfter(true);

		mAnimDialogIn = AnimationUtils.loadAnimation(getActivity(),
				R.anim.dialog_enter);
		mAnimDialogIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				if (mOverlay.getVisibility() != View.VISIBLE)
					mOverlay.startAnimation(mAnimFadeIn);
			}});
		mAnimDialogOut = AnimationUtils.loadAnimation(getActivity(),
				R.anim.dialog_exit);
		mAnimDialogOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				mOverlayPost.setVisibility(View.GONE);
				mOverlay.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mOverlay.startAnimation(mAnimFadeOut);
			}});
		
		
		mAnimSlideIn = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_in);
		mAnimSlideOut = AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_out);
		mAnimSlideOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				mStatusText.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		return mView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mThreadAdapter = new ThreadAdapter();
		if (savedInstanceState != null) {
			savedInstanceState.setClassLoader(ThreadState.class.getClassLoader());
			fromThreadState(new ThreadState( savedInstanceState
					.getBundle("BOARD_STATE")));
		} else { // we are totally new!
			mBoard = Board.getBoardById(getArguments().getString("board"));
			mThreadId = getArguments().getInt("threadId");
			mPostId = getArguments().getInt("postId");
			refresh();
			// new ThreadLoader().execute(Http.Chan.threadURL(mBoard,
			// mThreadId));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		if (savedInstanceState != null) {
			int[] list_state = savedInstanceState
					.getIntArray("BOARD_LIST_STATE");
			if (list_state != null)
				mListView.setSelectionFromTop(list_state[0], list_state[1]);
		}
		if (mStatusError != -1) {
			mStatusText.setVisibility(View.VISIBLE);
			mStatusText.setText(mStatusError);
		}
		mListView.setFastScrollEnabled(true);
		updateTheme(PrefsActivity.getTheme(getActivity(), getBoard()
				.isWorksafe()));
		if (getThreadId() == -1) {
			int dp = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 48, getResources()
							.getDisplayMetrics());
			mNextBar = new ProgressBar(getActivity());
			mNextBar.setLayoutParams(new FrameLayout.LayoutParams(dp, dp,
					Gravity.CENTER));
			mNextBar.setIndeterminateDrawable(getResources().getDrawable(
					mTheme.progress_large));
			FrameLayout fl = new FrameLayout(getActivity());
			fl.addView(mNextBar);
			fl.setLayoutParams(new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT,
					AbsListView.LayoutParams.WRAP_CONTENT));
			mListView.addFooterView(fl);
			// mNextBar.setVisibility(View.GONE);
			mListView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {

				}

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					if ((scrollState == SCROLL_STATE_TOUCH_SCROLL)
							|| (scrollState == SCROLL_STATE_IDLE)) {
						if (view.getLastVisiblePosition() + 1 >= mPosts.length) {
							if (!mLoading) {
								if (!mLoadingNext) {
									loadNextPage();
								}
							}
						}
					}

				}

			});
		}
		setListAdapter(mThreadAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mListView != null) {
			if (mListView.getChildAt(0) != null) {
				outState.putIntArray("BOARD_LIST_STATE",
						new int[] { mListView.getFirstVisiblePosition(),
								mListView.getChildAt(0).getTop() });
			}
			outState.putBundle("BOARD_STATE", (new ThreadState(this)).getBundle());
		}
	}

	@Override
	public boolean onBackPressed() {
		if (mOverlayPost.getVisibility() != View.GONE) {
			//mOverlayPost.setVisibility(View.GONE);
			mOverlayPost.startAnimation(mAnimDialogOut);
			//mOverlay.setVisibility(View.GONE);
			return true;
		}
		return false;
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

	public void fromThreadState(ThreadState t) {
		t.apply(this);
	}

	@Override
	public Theme getTheme() {
		return mTheme;
	}

	@Override
	public void updateTheme(Theme t) {
		if (mListView == null)
			return;
		mTheme = t;
		mView.setBackgroundColor(t.bg_color);
		mProgressBar.setIndeterminateDrawable(getResources().getDrawable(
				mTheme.progress_large));
		if (mNextBar != null) {

			mNextBar.setIndeterminateDrawable(getResources().getDrawable(
					mTheme.progress_large));
		}

		for (int i = 0; i < mListView.getChildCount(); i++) {
			View v = mListView.getChildAt(i);
			if (v instanceof PostView) {
				((PostView) v).setTheme(t);
			}
		}
		mListView.setCacheColorHint(t.bg_color);
		mOverlayPost.setTheme(mTheme);
	}

	@Override
	public void refresh() {
		if (mThreadAdapter.getCount() > 0 && mThreadId != -1
				&& mListView != null) {
			mLastVisible = false; //mListView.getLastVisiblePosition() == (mThreadAdapter.getCount() - 1);
			mSelectedPos = mListView.getFirstVisiblePosition();
			mSelectedTop = mListView.getChildAt(0).getTop();
		} else {
			mLastVisible = false;
			mSelectedTop = -1;
			mSelectedPos = -1;
		}
		hideError();
		setLoading();
		int max_replies = -1;
		try {
			//max_replies = Integer.parseInt(PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_REPLIES, "0"));
			max_replies = Integer.parseInt(PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_REPLIES, "0"));
		} catch(NumberFormatException ex){}
		new ThreadLoader(false, null, max_replies).execute(Http.Chan.threadURL(mBoard, mThreadId));
	}

	@Override
	public Fragment getFragment() {
		return this;
	}
	
	
	
	public Post[] getPosts() {
		return mPosts;
	}

	public void setPosts(Post[] posts, boolean checkLast, boolean inPlace) {
		if (checkLast && mListView != null) {
			mPostId = -1;
			mLastVisible = false; //mListView.getLastVisiblePosition() == (mThreadAdapter.getCount() - 1);
		}
		
		if (inPlace && mListView != null && mSelectedTop == -1 && mListView.getChildAt(0) != null) {
			mSelectedPos = mListView.getFirstVisiblePosition();
			mSelectedTop = mListView.getChildAt(0).getTop();
		}

		if (posts != null) {
			mPosts = posts;
			mThreadAdapter.notifyDataSetChanged();
			if (mPostId != -1) {
				int pos = findPostPos(mPostId);
				if (pos != -1 && mListView != null)
					mListView.setSelection(pos);
				mPostId = -1;
			} else if (mLastVisible && mListView != null) {
				mListView.setSelection(posts.length - 1);
				mLastVisible = false;
			} else if (mSelectedTop != -1 && mListView != null) {
				mListView.setSelectionFromTop(mSelectedPos, mSelectedTop);
				mSelectedPos = -1;
			}
		}
	}

	public void appendPosts(Post[] posts) {
		Post[] newPosts = new Post[posts.length + mPosts.length];
		System.arraycopy(mPosts, 0, newPosts, 0, mPosts.length);
		System.arraycopy(posts, 0, newPosts, mPosts.length, posts.length);
		mPosts = newPosts;
		mThreadAdapter.notifyDataSetChanged();
	}

	@Override
	public Fragment setNewPost(NewPost np) {
		mNewPost = np;
		return this;
	}

	@Override
	public NewPost getNewPost() {
		return mNewPost;
	}

	@Override
	public void uploadPost(NewPostView v) {
		v.saveNewPost(mNewPost);
		if (mThreadId == -1 && !mNewPost.hasFile()) {
			((MainActivity) getActivity()).showDialog("Error",
					"You must include an image when creating a thread.");
			return;
		}
		if (mNewPost.getComment().length() == 0 && !mNewPost.hasFile()) {
			((MainActivity) getActivity()).showDialog("Error",
					"Write a comment.");
			return;
		}
		((MainActivity) getActivity())
				.showProgressDialog("Verifying captcha...");
		(new ThreadPoster()).execute(v.saveNewPost(new NewPost(mNewPost)));
	}

	@Override
	public Board getBoard() {
		return mBoard;
	}

	@Override
	public int getThreadId() {
		return mThreadId;
	}

	@Override
	public void addQuote(int id) {
		String comment = mNewPost.getComment();
		mNewPost.setComment(String.format("%s%s>>%d", comment,
				(comment.length() == 0) ? "" : "\n", id));
	}

	public void showError(int error_resource) {
		mStatusError = error_resource;
		if (error_resource != -1) {
			mStatusText.setVisibility(View.VISIBLE);
			mStatusText.setText(mStatusError);
			mStatusText.startAnimation(mAnimSlideIn);
		}

	}

	public void hideError() {
		if (mStatusError != -1)
			mStatusText.startAnimation(mAnimSlideOut);
		mStatusError = -1;

	}

	public void setLoading() {
		mLoading = true;
		mThreadAdapter.notifyDataSetChanged();
	}

	public void finishLoading() {
		if (mLoading) {
			mLoading = false;
			mThreadAdapter.notifyDataSetChanged();
		}
		if (mLoadingNext) {
			mLoadingNext = false;
			mThreadAdapter.notifyDataSetChanged();
			// mListView.removeFooterView(mNextBar);
		}
	}

	public void loadNextPage() {

		// mListView.addFooterView(mNextBar);
		mLoadingNext = true;
		mPageNo++;
		String url = String.format("http://boards.4chan.org/%s/%d", getBoard()
				.getId(), mPageNo);
		Set<Integer> ignore = new HashSet<Integer>();
		for (int i=0; i<mPosts.length; i++) {
			ignore.add(mPosts[i].getThreadId());
		}
		int max_replies = -1;
		try {
			//max_replies = Integer.parseInt(PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_REPLIES, "0"));
			max_replies = Integer.parseInt(PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_REPLIES, "0"));
		} catch(NumberFormatException ex){}
		(new ThreadLoader(true, ignore, max_replies)).execute(url);
	}

	public PhoneHomeFragment getHomeFragment() {
		return (PhoneHomeFragment) getFragmentManager().findFragmentByTag(
				"chanexplorer.HomeViewPager");
	}

	@Override
	public void gotoPost(Board b, int threadId, int postId, boolean allowBoard,
			boolean scrollTo) {
		if (b.equals(getBoard()) && threadId == getThreadId()) {
			if (mPosts == null) {
				mPostId = postId;
				refresh();
				return;
			}
			int post_pos = -1;
			for (int i = 0; i < mPosts.length; i++) {
				if (mPosts[i].getThreadId() == threadId
						&& mPosts[i].getId() == postId) {
					post_pos = i;
				}
			}
			if (post_pos != -1
					&& (getThreadId() != -1 || (getThreadId() == -1 && allowBoard))) {
				if (scrollTo) {
					mListView.setSelection(post_pos);
				} else {
					// TODO : Display the post.
					mOverlay.setVisibility(View.VISIBLE);
					if (mOverlay.getVisibility() != View.VISIBLE)
						mOverlay.setVisibility(View.INVISIBLE);
					mOverlayPost.setVisibility(View.VISIBLE);
					mOverlayPost.setPost(mPosts[post_pos], getThreadId() != -1);
					ScaleAnimation sa = new ScaleAnimation(.75f, 1f, .75f, 1f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
					sa.setDuration(500);
					mOverlayPost.startAnimation(mAnimDialogIn);
				}

				
				return;
			}
			if (getThreadId() == threadId) {
				mPostId = postId;
				refresh();
				return;
			}
		}
		getHomeFragment().selectOrAddThread(b, threadId, postId);
	}

	public int findPostPos(int postId) {
		for (int i = 0; i < mPosts.length; i++) {
			if (mPosts[i].getId() == postId) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void viewImage(int id) {
		int cnt = 0;
		int pos = 0;
		for (int i = 0; i < mPosts.length; i++) {
			if (mPosts[i].getId() == id) {
				pos = cnt;
			}
			if (mPosts[i].hasImage()) {
				cnt++;
			}
		}
		ImageInfo[] imgs = new ImageInfo[cnt];
		cnt = 0;
		for (int i = 0; i < mPosts.length; i++) {
			if (mPosts[i].hasImage() && mPosts[i].getThumbnail() != null) {
				imgs[cnt] = new ImageInfo(getBoard(), getThreadId(),
						mPosts[i].getImage(), mPosts[i].getFilename(), mPosts[i].getThumbnail());
				cnt++;
			}
		}
		Intent intent = new Intent(getActivity(), ImageViewer.class);
		intent.putExtra("POSITION", pos).putExtra("IMAGES", imgs);
		startActivity(intent);
	}

	protected class PostContextMenu implements View.OnCreateContextMenuListener {
		PostView mView;

		public PostContextMenu(PostView v, int mode) {
			mView = v;
			mContextPostView = mView;
			mView.setContextMode(mode);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			if (mView.hasLinks()
					&& mView.getContextMode() == PostView.MODE_LINKS) {
				// mContextPostView = (PostView) v;
				menu.clear();
				menu.setHeaderTitle(String.format(">>%d - Links", mView
						.getPost().getId()));
				int pos = 0;
				for (String quote : mView.getQuotes()) {
					Board b = null;
					int post = -1;
					if (quote.indexOf('#') != -1) {
						post = Integer.parseInt(quote.substring(quote
								.indexOf('#') + 2));
					}
					if (quote.startsWith("/")) {
						b = Board.getBoardById(quote.substring(1,
								quote.indexOf('/', 1)));
					}
					StringBuilder title = new StringBuilder(">>");
					if (b != null) {
						title.append(String.format("/%s/", b.getId()));
					}
					if (post != -1) {
						title.append(post);
					}
					menu.add(0, pos, pos, title.toString());
					pos++;
				}
				int diff = pos;
				for (String link : mView.getHyperLinks()) {
					menu.add(1, pos - diff, pos, link);
					pos++;
				}
			} else if (mView.hasReplies()
					&& mView.getContextMode() == PostView.MODE_REPLY) {
				// mContextPostView = (PostView) v;

				menu.clear();
				menu.setHeaderTitle(String.format(">>%d - Replies", mView
						.getPost().getId()));
				int pos = 0;
				for (Integer reply : mView.getPost().getReplies()) {
					menu.add(2, pos, pos, String.format(">>%d", reply));

					pos++;
				}
			}

		}

	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getGroupId() == MainActivity.MAIN_ACTIVITY_CONTEXT_GROUP) {
			return false;
		}
		if (item.getGroupId() == 0 && mContextPostView != null) {
			Board b = getBoard();
			int thread = getThreadId();
			int post = -1;
			String target = mContextPostView.getQuotes().get(item.getItemId());
			if (target.indexOf('#') != -1) {
				post = Integer
						.parseInt(target.substring(target.indexOf('#') + 2));
			}
			if (target.startsWith("/")) {
				b = Board.getBoardById(target.substring(1,
						target.indexOf('/', 1)));
			}
			Matcher m1 = PostView.num_match.matcher(target);
			if (m1.find()) {
				thread = Integer.parseInt(m1.group(1));
			}
			gotoPost(b, thread, post, true, false);
			mContextPostView.setOnCreateContextMenuListener(null);
			mContextPostView = null;
			return true;

		} else if (item.getGroupId() == 1 && mContextPostView != null) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(item.getTitle().toString()));
			startActivity(browserIntent);
			mContextPostView.setOnCreateContextMenuListener(null);
			mContextPostView = null;
			return true;
		} else if (item.getGroupId() == 2 && mContextPostView != null) {
			gotoPost(getBoard(), getThreadId(), mContextPostView.getPost()
					.getReplies().get(item.getItemId()), true, false);
			mContextPostView.setOnCreateContextMenuListener(null);
			mContextPostView = null;
			return true;
		}

		return false;
	}

	private final class ThreadAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mPosts == null)
				return 0;
			if (mLoading == true)
				return 0;
			return mPosts.length;
		}

		@Override
		public Object getItem(int position) {
			if (mPosts == null)
				return null;
			if (position >= mPosts.length)
				return null;
			return mPosts[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null || !(v instanceof PostView)) {
				LayoutInflater vi = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.post_view, null);
				// ((PostView) v).setTheme(mTheme);
				((PostView) v).setNavigator(ThreadFragment.this);
				((PostView) v).setEnsureView(new PostView.EnsureView() {
					private int mSelection;
					private int mTop;

					@Override
					public void ensureStart(View v) {
						mSelection = mListView.getFirstVisiblePosition();
						mTop = mListView.getChildAt(0).getTop();
					}

					@Override
					public void ensureView(View v, int change) {
						mListView
								.setSelectionFromTop(mSelection, mTop - change);
					}

				});
				((PostView) v).setOnCreateContextMenuListener(null);
				final PostView vx = (PostView) v;
				if (getThreadId() == -1)
					((PostView) v).threadAction();
				((PostView) v).mLinksBtn
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								vx.setOnCreateContextMenuListener(new PostContextMenu(
										vx, PostView.MODE_LINKS));
								getActivity().openContextMenu(vx);
								// unregisterForContextMenu(vx);

							}
						});
				((PostView) v).mRepliesBtn
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								vx.setOnCreateContextMenuListener(new PostContextMenu(
										vx, PostView.MODE_REPLY));
								getActivity().openContextMenu(vx);
								// unregisterForContextMenu(vx);

							}
						});
			}
			((PostView) v).setTheme(mTheme);
			((PostView) v).setPost(mPosts[position], getThreadId() != -1);
			return v;
		}

	}

	private class ThreadLoader extends AsyncTask<String, Integer, Post[]> {
		private boolean mAppend = false;
		private Set<Integer> mIgnore = null;
		private int maxReplies = -1;

		public ThreadLoader(boolean append, Set<Integer> ignore, int max_replies) {
			mAppend = append;
			mIgnore = ignore;
			maxReplies = max_replies;
		}

		@Override
		protected Post[] doInBackground(String... url) {

			try {
				String data = Http.getRequestAsString(url[0], "");
				

				Post[] posts = Parser.parse(data, false, mIgnore, maxReplies, mTheme.green_text);

				if (getNewPost() != null) {
					getNewPost().getCaptchaInfo(data, url[0]);
				}

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
			showError(error[0]);
		}

		@Override
		protected void onPostExecute(Post[] result) {
			if (result != null) {
				if (mAppend) {
					appendPosts(result);
				} else {
					setPosts(result, false, getThreadId() != -1);
					mPageNo = 0;
				}
			}
			finishLoading();
		}

	}

	private class ThreadPoster extends AsyncTask<NewPost, String, String[]> {

		private final static String cap_start = "<textarea rows=\"5\" cols=\"100\">";
		private final static String cap_end = "</textarea";
		private final static String captcha_invalid = "CHANEXPLORER_INVALID_CAPTCHA";
		private final static String captcha_finished = "CHANEXPLORER_CAPTCHA_FINISHED";
		// private final static String upload_success =
		// "CHANEXPLORER_POST_SUCCESS";
		private final static String error_start = "<span id=\"errmsg\" style=\"color: red;\">";
		private final static String error_end = "</span>";
		private final Pattern success = Pattern
				.compile("thread:([0-9]+),no:([0-9]+)");

		private int notifyId = -1;
		private NewPost mNp;

		@Override
		protected String[] doInBackground(NewPost... np) {
			mNp = np[0];
			try {
				String response;
				int start_from;
				boolean usePass = PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_USE_PASS, false);
				boolean rightCaptcha = false;
				if (np[0].getCaptcha().length() > 0 || !usePass) { 
					Map<String, String> captchaData = new HashMap<String, String>();
					captchaData.put("recaptcha_challenge_field",
							np[0].getCaptchaToken());
					captchaData.put("recaptcha_response_field", np[0].getCaptcha());
					captchaData.put("submit", "I&#39;m a human");
					response = Http
							.postRequestAsString(np[0].getCapchaRef(), captchaData,
									np[0].getCapchaRef());
					if (!response.contains("<textarea rows=\"5\" cols=\"100\">")) {
						publishProgress(captcha_invalid);
						return null;
					}
					rightCaptcha = true;
					np[0].setCaptchaChallenge(response.substring(
							(start_from = response.indexOf(cap_start, 0)
									+ cap_start.length()),
							response.indexOf(cap_end, start_from)));
				}
				// android.util.Log.d("POST", response);
				
				
				publishProgress(captcha_finished);
				
				if (usePass && !rightCaptcha) {
					String token = PrefsActivity.getSetting(getActivity(), PrefsActivity.KEY_PASS_TOKEN, "");
					response = Http.newPost(np[0], token, getBoard(), getThreadId());
				} else {
					response = Http.newPost(np[0], null, getBoard(), getThreadId());
				}
				

				if (response
						.contains("<span id=\"errmsg\" style=\"color: red;\">")) {
					String error = response.substring(
							(start_from = response.indexOf(error_start, 0)
									+ error_start.length()),
							response.indexOf(error_end, start_from));
					publishProgress(error);
					return null;
				}
				Matcher m1 = success.matcher(response);
				if (m1.find()) {
					// publishProgress(upload_success);
					return new String[] { m1.group(1), m1.group(2) };
				} else {
					publishProgress("Error uploading post.");
					return null;
				}
				// <b>Post successful!<!-- thread:25328574,no:25328747 --></b>
				// <b>1325809672490.jpg uploaded!<!-- thread:0,no:27367460
				// --></b>
			} catch (MalformedURLException e) {
				e.printStackTrace();
				publishProgress("Error uploading post.");
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("Error uploading post.");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... error) {
			if (mDeleted)
				return;
			if (error[0].equals(captcha_invalid)) {
				((MainActivity) getActivity()).dismissProgressDialog();
				((MainActivity) getActivity()).showDialog("Error",
						"Invalid captcha.");
				((MainActivity) getActivity()).mNewPostView.refreshCaptcha();
			} else if (error[0].equals(captcha_finished)) {
				((MainActivity) getActivity()).dismissProgressDialog();
				((MainActivity) getActivity()).getSlidingMenu().showAbove();
				mNewPost.reset();

				CharSequence tickerText = "Uploading post to " + getTitle(); // ticker-text
				Context context = getActivity(); // application Context
				CharSequence contentTitle = "ChanExplorer"; // message title
				CharSequence contentText = "Uploading post to " + getTitle(); // message
																				// text
				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						context);
				Notification notification = builder
						.setContentIntent(
								PendingIntent.getActivity(getActivity(), 0,
										new Intent(), 0))
						.setSmallIcon(android.R.drawable.stat_sys_upload)
						.setTicker(tickerText)
						.setWhen(System.currentTimeMillis()).setOngoing(true)
						.setContentTitle(contentTitle)
						.setContentText(contentText).getNotification();

				notifyId = notification.hashCode();
				NotificationManager notificationManager = (NotificationManager) getActivity()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(notifyId, notification);
			} else {
				((MainActivity) getActivity()).dismissProgressDialog();
				Intent notificationIntent = new Intent(getActivity(),
						MainActivity.class);
				notificationIntent
						.setAction("co.wakarimasen.chanexplorer.FIX_POST");
				notificationIntent.putExtra("Board", getBoard().getId());
				notificationIntent.putExtra("Thread", getThreadId());
				notificationIntent.putExtra("NewPostInfo", mNp);

				PendingIntent contentIntent = PendingIntent.getActivity(
						getActivity(), notificationIntent.hashCode(),
						notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						getActivity());
				Notification notification = builder
						.setSmallIcon(android.R.drawable.stat_notify_error)
						.setContentIntent(contentIntent)
						.setTicker("Error uploading post to " + getTitle())
						.setWhen(System.currentTimeMillis())
						.setAutoCancel(true).setContentTitle("Post Error")
						.setContentText(error[0]).getNotification();
				NotificationManager notificationManager = (NotificationManager) getActivity()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(notification.hashCode(),
						notification);
			}
		}

		@Override
		protected void onPostExecute(String[] result) {
			if (mDeleted)
				return;
			if (notifyId != -1) {
				NotificationManager notificationManager = (NotificationManager) getActivity()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(notifyId);
			}
			if (result != null) {
				Intent notificationIntent = new Intent(getActivity(),
						MainActivity.class);
				notificationIntent
						.setAction("co.wakarimasen.chanexplorer.ACTION_VIEW");
				if (result[0].equals("0")) {
					notificationIntent.setData(Uri.parse(String.format(
							"chanexplorer://%s/%s", getBoard().getId(),
							result[1])));
					notificationIntent.putExtra("Board", getBoard().getId());
					notificationIntent.putExtra("Thread",
							Integer.parseInt(result[1]));
					notificationIntent.putExtra("Post", -1);
				} else {
					notificationIntent.setData(Uri.parse(String.format(
							"chanexplorer://%s/%s/%s", getBoard().getId(),
							result[0], result[1])));
					notificationIntent.putExtra("Board", getBoard().getId());
					notificationIntent.putExtra("Thread",
							Integer.parseInt(result[0]));
					notificationIntent.putExtra("Post",
							Integer.parseInt(result[1]));
				}
				PendingIntent contentIntent = PendingIntent.getActivity(
						getActivity(), notificationIntent.hashCode(),
						notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						getActivity());
				Notification notification = builder
						.setSmallIcon(android.R.drawable.stat_sys_upload_done)
						.setContentIntent(contentIntent)
						.setTicker("Post complete")
						.setWhen(System.currentTimeMillis())
						.setAutoCancel(true).setContentTitle(">>" + result[1])
						.setContentText(mNp.getComment()).getNotification();
				NotificationManager notificationManager = (NotificationManager) getActivity()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(notification.hashCode(),
						notification);
			}
		}

	}

	@Override
	public boolean isWorksafe() {
		return getBoard().isWorksafe();
	}

	@Override
	public boolean isThread() {
		return getThreadId() != -1;
	}

	@Override
	public void setDeleted() {
		mDeleted = true;
		mListView = null;
	}
	
	@Override
	public boolean isDeleted() {
		return mDeleted;
	}
}
