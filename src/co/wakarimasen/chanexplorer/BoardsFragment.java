package co.wakarimasen.chanexplorer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import co.wakarimasen.chanexplorer.PinnedHeaderListView.PinnedHeaderAdapter;
import co.wakarimasen.chanexplorer.imageboard.Board;

import co.wakarimasen.chanexplorer.R;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class BoardsFragment extends ListFragment implements ChanPage {

	private int mPinnedHeaderBackgroundColor;
	private int mPinnedHeaderTextColor;
	private List<Board> mFavorites;
	private List<Board> mBoards;
	private String mHidden = "";
	private final static Joiner mJoiner = Joiner.on(",");
	private final static Splitter mSplitter = Splitter.on(",").trimResults()
			.omitEmptyStrings();
	private Theme mTheme = Theme.Holo;
	private View mView = null;
	private ListView mListView;
	private boolean mDeleted;
	
	public BoardsFragment() {
		super();
		mDeleted = false;
		mFavorites = new ArrayList<Board>();
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.boards_fragment, container, false);
		return mView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFavorites.clear();
		mTheme = PrefsActivity.getTheme(getActivity(), false);
		mPinnedHeaderBackgroundColor = mTheme.pinned_header_bg;
		mPinnedHeaderTextColor = mTheme.pinned_header_text;
		// SharedPreferences settings =
		// getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
		String favs = PrefsActivity.getSetting(getActivity(),
				PrefsActivity.KEY_FAVORITE_BOARDS, "");
		for (String t : mSplitter.split(favs)) {
			Board b = Board.getBoardById(t);
			if (b != null)
				mFavorites.add(b.asFavorite());
		}
		setListAdapter(new BoardsAdapter());
		// PinnedHeaderListView listView = (PinnedHeaderListView)
		// v.findViewById(android.R.id.list);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		((PinnedHeaderListView) mListView)
				.setPinnedHeaderView(LayoutInflater.from(
						mListView.getContext()).inflate(
						R.layout.board_header_text, mListView, false));
		// Theme
		mListView.setSelector(R.drawable.board_list_selector_holo);
		mListView.setDividerHeight(0);
		mListView.setFastScrollEnabled(true);
		if (savedInstanceState != null
				&& savedInstanceState.getInt("BOARD_LIST_TOP",
						Integer.MIN_VALUE) != Integer.MIN_VALUE) {
			mListView.setSelectionFromTop(
					savedInstanceState.getInt("BOARD_LIST_TOP"),
					savedInstanceState.getInt("BOARD_LIST_POS"));
		}
		mListView.setOnScrollListener((BoardsAdapter) getListAdapter());
		updateTheme(PrefsActivity.getTheme(getActivity(), false));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("BOARD_LIST_TOP", mListView
				.getFirstVisiblePosition());
		if (mListView.getChildAt(0) != null)
			outState.putInt("BOARD_LIST_POS", mListView.getChildAt(0)
					.getTop());
		else
			outState.putInt("BOARD_LIST_POS", 0);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((MainActivity) getActivity()).getHomeFragment().selectOrAddThread(
				((BoardsAdapter) getListAdapter()).get(position), -1, -1);
	}

	public void saveFavorites() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefsActivity.KEY_FAVORITE_BOARDS,
				mJoiner.join(mFavorites));
		editor.commit();
	}

	public boolean inFavorites(Board b) {
		for (Board f : mFavorites) {
			if (f.getId().equals(b.getId())) {
				return true;
			}
		}
		return false;
	}

	public void addFavorite(Board b) {
		if (!inFavorites(b)) {
			if (b.getCategory().equals(Board.categories.get(0))) {
				mFavorites.add(b);
			} else {
				mFavorites.add(b.asFavorite());
			}
		}
	}

	public void removeFavorite(Board b) {
		for (Board f : mFavorites) {
			if (f.getId().equals(b.getId())) {
				mFavorites.remove(f);
				break;
			}
		}
	}

	public List<Board> getBoards() {
		String hidden = PrefsActivity.getSetting(getActivity(),
				PrefsActivity.KEY_HIDDEN_BOARDS,
				getString(R.string.default_hidden));
		if (!hidden.equals(mHidden) || mBoards == null || mBoards.size() == 0) {
			if (mBoards == null) {
				mBoards = new ArrayList<Board>(Board.boards);
			} else {
				mBoards.clear();
				mBoards.addAll(Board.boards);
			}
			for (String boardId : mSplitter.split(hidden)) {
				mBoards.remove(Board.getBoardById(boardId));
			}
			mHidden = hidden;
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		}
		return mBoards;
	}

	@Override
	public Board getBoard() {
		return null;
	}

	@Override
	public int getThreadId() {
		return -1;
	}

	@Override
	public Theme getTheme() {
		return mTheme;
	}

	@Override
	public void updateTheme(Theme t) {
		//if (mTheme.equals(t))
		//	return;
		if (mListView == null)
			return;
		mTheme = t;
		mPinnedHeaderBackgroundColor = mTheme.pinned_header_bg;
		mPinnedHeaderTextColor = mTheme.pinned_header_text;
		if (mView != null) {
			mListView.setBackgroundColor(mTheme.bg_color);
			mListView.setCacheColorHint(mTheme.bg_color);
			for (int i = 0; i<mListView.getChildCount(); i++) {
				View v = mListView.getChildAt(i);
				setItemTheme(v, mTheme);
			}
		}
	}

	@Override
	public Fragment getFragment() {
		return this;
	}

	@Override
	public void refresh() {
	}
	
	public void setItemTheme(View view, Theme theme) {
		view.setBackgroundResource(theme.list_style);
		
		if (inFavorites((Board) (view.findViewById(R.id.btn_favorite)
				.getTag()))) {
			((ImageView) view.findViewById(R.id.btn_favorite))
					.setImageResource(theme.fav_on);
		} else {
			((ImageView) view.findViewById(R.id.btn_favorite))
					.setImageResource(theme.fav_off);
		}
		((TextView) view.findViewById(R.id.board_header_text))
				.setTextColor(theme.pinned_header_text);
		((TextView) view.findViewById(R.id.board_header_text))
				.setBackgroundColor(theme.pinned_header_bg);
	}

	private final class BoardsAdapter extends BaseAdapter implements
			SectionIndexer, OnScrollListener, PinnedHeaderAdapter,
			BoardCategoryIndexer.BoardDataSource<Board> {

		private BoardCategoryIndexer mIndexer;

		public BoardsAdapter() {
			super();
			this.mIndexer = new BoardCategoryIndexer(this);
		}

		@Override
		public int getCount() {
			return mFavorites.size() + getBoards().size();
		}

		@Override
		public Object getItem(int position) {
			return get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get our object for this position
			Board board = get(position);

			// Try to reuse convertView if it's not null, otherwise inflate it
			// from our item layout
			// This gives us some performance gains by not always inflating a
			// new view
			// This will sound familiar to MonoTouch developers with
			// UITableViewCell.DequeueReusableCell()
			View view = convertView;
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.board_item, null);
				ImageView btn = ((ImageView) view
						.findViewById(R.id.btn_favorite));
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int direction;
						int first = mListView.getFirstVisiblePosition();
						int top = mListView.getChildAt(0).getTop();
						if (inFavorites((Board) v.getTag())) {
							removeFavorite((Board) v.getTag());
							((ImageView) v)
									.setImageResource(mTheme.fav_off);
							direction = -1;

						} else {
							if (first < mFavorites.size()) {
								top += ((PinnedHeaderListView) mListView)
										.getPinnedHeaderView().getHeight();
							}
							addFavorite((Board) v.getTag());
							((ImageView) v)
									.setImageResource(mTheme.fav_on);
							direction = 1;

						}
						saveFavorites();
						notifyDataSetChanged();
						mListView.setSelectionFromTop(
								Math.max(0, first + direction), top);
					}
				});
			}
			((ImageView) view.findViewById(R.id.btn_favorite)).setTag(board);
			setItemTheme(view, mTheme);
			
			TextView boardName = (TextView) view
					.findViewById(R.id.board_item_name);

			boardName.setText(String.format("/%s/ - %s", board.getId(),
					board.getName()));
			boardName.setTextColor(mTheme.title_color);
			// Finally return the view

			bindSectionHeader(view, position);

			return view;
		}

		private void bindSectionHeader(View itemView, int position) {
			final TextView headerView = (TextView) itemView
					.findViewById(R.id.board_header_text);
			final View dividerView = itemView.findViewById(R.id.list_divider);

			final int section = getSectionForPosition(position);
			if (getPositionForSection(section) == position) {
				String title = (String) mIndexer.getSections()[section];
				headerView.setText(title);
				headerView.setVisibility(View.VISIBLE);
				dividerView.setVisibility(View.GONE);
			} else {
				headerView.setVisibility(View.GONE);
				dividerView.setVisibility(View.VISIBLE);
			}

			// move the divider for the last item in a section
			if (getPositionForSection(section + 1) - 1 == position) {
				dividerView.setVisibility(View.GONE);
			} else {
				dividerView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			mIndexer.resetCache();
		}

		@Override
		public int getPinnedHeaderState(int position) {
			if (mIndexer == null || getCount() == 0) {
				return PINNED_HEADER_GONE;
			}

			if (position < 0) {
				return PINNED_HEADER_GONE;
			}

			// The header should get pushed up if the top item shown
			// is the last item in a section for a particular letter.
			int section = getSectionForPosition(position);
			int nextSectionPosition = getPositionForSection(section + 1);
			if (nextSectionPosition != -1
					&& position == nextSectionPosition - 1) {
				return PINNED_HEADER_PUSHED_UP;
			}

			return PINNED_HEADER_VISIBLE;
		}

		@Override
		public void configurePinnedHeader(View v, int position, int alpha) {
			TextView header = (TextView) v;

			final int section = getSectionForPosition(position);
			final String title = (String) getSections()[section];

			header.setText(title);

			if (alpha == 255) {
				header.setBackgroundColor(mPinnedHeaderBackgroundColor);
				header.setTextColor(mPinnedHeaderTextColor);
			} else {
				header.setBackgroundColor(Color.argb(alpha,
						Color.red(mPinnedHeaderBackgroundColor),
						Color.green(mPinnedHeaderBackgroundColor),
						Color.blue(mPinnedHeaderBackgroundColor)));
				header.setTextColor(Color.argb(alpha,
						Color.red(mPinnedHeaderTextColor),
						Color.green(mPinnedHeaderTextColor),
						Color.blue(mPinnedHeaderTextColor)));
			}

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (view instanceof PinnedHeaderListView) {
				((PinnedHeaderListView) view)
						.configureHeaderView(firstVisibleItem);
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public int getPositionForSection(int section) {
			return mIndexer.getPositionForSection(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			return mIndexer.getSectionForPosition(position);
		}

		@Override
		public Object[] getSections() {
			return mIndexer.getSections();
		}

		@Override
		public Board get(int position) {
			if (position < mFavorites.size()) {
				return mFavorites.get(position);
			} else {
				return getBoards().get(position - mFavorites.size());
			}
		}

		@Override
		public int size() {
			return getCount();
		}

	}

	@Override
	public NewPost getNewPost() {
		return null;
	}

	@Override
	public Fragment setNewPost(NewPost np) {
		return this;
	}

	@Override
	public void uploadPost(NewPostView v) {
		return;

	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public boolean isWorksafe() {
		return false;
	}

	@Override
	public void setDeleted() {
		mDeleted = true;
	}

	@Override
	public boolean isDeleted() {
		return mDeleted;
	}

}