package co.wakarimasen.chanexplorer;

import co.wakarimasen.chanexplorer.imageboard.Board;

import android.support.v4.app.Fragment;

public interface ChanPage {

	public Theme getTheme();
	public void updateTheme(Theme t);
	public void refresh();
	public Fragment getFragment();
	
	public Board getBoard();
	public int getThreadId();
	
	public void uploadPost(NewPostView v);
	public NewPost getNewPost();
	public Fragment setNewPost(NewPost np);
	
	public boolean onBackPressed();
	public boolean isWorksafe();
	
	public void setDeleted();
	public boolean isDeleted();
	
	//public void setPosition(int position);
	//public int getPosition();
	
	//public void markDeleted();
	//public boolean isDeleted();
}
