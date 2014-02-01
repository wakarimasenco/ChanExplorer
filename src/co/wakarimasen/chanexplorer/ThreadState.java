package co.wakarimasen.chanexplorer;

import android.os.Bundle;
import android.os.Parcelable;

import co.wakarimasen.chanexplorer.imageboard.Board;
import co.wakarimasen.chanexplorer.imageboard.Post;

public class ThreadState {
	protected Board mBoard = null;
	protected int mThreadId = -1;
	protected Post[] mPosts;
	protected int mError = -1;

	public ThreadState(ThreadFragment t) {
		mBoard = t.getBoard();
		mThreadId = t.getThreadId();
		mPosts = t.getPosts();
		mError = t.mStatusError;
	}
	
	public ThreadState(Bundle bundle) {
		bundle.setClassLoader(Post.class.getClassLoader());
		String boardId = bundle.getString("BOARD");
		if (boardId != null) {
			mBoard = Board.getBoardById(boardId);
			mThreadId = bundle.getInt("THREAD_ID");
			Parcelable[] pa = bundle.getParcelableArray("POSTS");
			if (pa != null) {
				mPosts = new Post[pa.length];
				for (int i=0; i<pa.length; ++i) {
					mPosts[i] = (Post)pa[i];
				}
			}
			//mPosts = (Post[]) bundle.getParcelableArray("POSTS");
			//in.readTypedArray(mPosts, Post.CREATOR);
		}
		mError = bundle.getInt("ERROR");
	}

	public void apply(ThreadFragment t) {
		// t.mListView.setSelectionFromTop(mBoardTop, mBoardTop);
		t.mPosts = mPosts;
		t.mBoard = mBoard;
		t.mThreadId = mThreadId;
		t.mStatusError = mError;
	}
	
	public Bundle getBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("BOARD", (mBoard == null) ? null : mBoard.getId());
		if (mBoard != null) {
			bundle.putInt("THREAD_ID", mThreadId);
			//bundle.putInt("numPosts", (mPosts == null) ? 0 : mPosts.length);
			bundle.putParcelableArray("POSTS", mPosts);
		}
		bundle.putInt("ERROR", mError);
		return bundle;
	}
	
}