package co.wakarimasen.chanexplorer;

import java.io.File;

import co.wakarimasen.chanexplorer.ImageCache.ImageWaiter;
import co.wakarimasen.chanexplorer.imageboard.Board;

import co.wakarimasen.chanexplorer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NewPostView extends RelativeLayout {
	
	private TextView mTitle;
	private EditText mName;
	private EditText mEmail;
	private EditText mSubject;
	private EditText mComment;
	private TextView mFileText;
	public Button   mBrowse;
	private ImageView mCaptchaImg;
	private ImageButton mRefreshCaptcha;
	private EditText mCaptcha;
	public Button mSubmit;
	private NewPost mNewPost;
	private String mCaptchaUrl;
	private Boolean mLoadingCaptcha = !isInEditMode();
	private CaptchaWaiter mCapWaiter = new CaptchaWaiter();
	

	public NewPostView(Context context) {
		super(context);
	}

	public NewPostView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewPostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mTitle = (TextView) findViewById(R.id.new_post_title);
		mName = (EditText) findViewById(R.id.post_name);
		mEmail = (EditText) findViewById(R.id.post_email);
		mSubject = (EditText) findViewById(R.id.post_subject);
		mComment = (EditText) findViewById(R.id.post_comment);
		mFileText = (TextView) findViewById(R.id.post_file_text);
		mBrowse = (Button) findViewById(R.id.post_file_button);
		mCaptchaImg = (ImageView) findViewById(R.id.post_captcha_image);
		mRefreshCaptcha = (ImageButton) findViewById(R.id.post_refresh_captcha);
		mCaptcha = (EditText) findViewById(R.id.post_captcha);
		mSubmit = (Button) findViewById(R.id.post_submit);
		
		mCaptchaImg.setImageResource(R.drawable.progress_large_holo);
		
		mRefreshCaptcha.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshCaptcha();
			}});
		
	}
	
	public void refreshCaptcha() {
		setLoadingCaptcha(true);
		(new ReloadCaptcha()).execute(mNewPost);
		mNewPost.setCaptcha("");
		mCaptcha.setText("");
	}
	
	public void isClosed(boolean closed) {
		closed =  !closed;
		mName.setEnabled(closed);
		mEmail.setEnabled(closed);
		mSubject.setEnabled(closed);
		mComment.setEnabled(closed);		
		mBrowse.setEnabled(closed);
		mRefreshCaptcha.setEnabled(closed);
		mCaptcha.setEnabled(closed);
		
		mSubmit.setEnabled(closed);
		
		if (!closed) { 
			mName.setText("");
			mEmail.setText("");
			mSubject.setText("");
			mComment.setText("");
			mCaptcha.setText("");
			mFileText.setText("File: None");
		}
	}
	
	public void setNewPost(Board b, int threadId, NewPost np) {
		mNewPost = np;
		if (mNewPost.isThreadClosed()) {
			mTitle.setText("Thread Closed.");
			isClosed(true);
		} else {
			isClosed(false);
			if (threadId != -1)
				mTitle.setText(getContext().getString(R.string.text_new_post, b.getId(), threadId));
			else 
				mTitle.setText(getContext().getString(R.string.text_new_thread, b.getId()));
			mName.setText(np.getName());
			mEmail.setText(np.getEmail());
			mSubject.setText(np.getSubject());
			mComment.setText(np.getComment());
			mCaptcha.setText(np.getCaptcha());
			if (np == null || np.getFile() == null || np.getFile().length() == 0) {
				mFileText.setText("File: None");
			} else {
				mFileText.setText("File: "+(new File(np.getFile())).getName());
			}
			if (PrefsActivity.getSetting(getContext(), PrefsActivity.KEY_USE_PASS, false)) {
				mCaptcha.setHint(getContext().getString(R.string.form_pass));
			} else {
				mCaptcha.setHint(getContext().getString(R.string.form_verification));
			}
			setCaptcha();
		}
	}
	
	public boolean sameNewPost(NewPost other) {
		return other.equals(mNewPost);
	}
	
	public NewPost saveNewPost(NewPost dest) {
		dest.setName(mName.getText().toString());
		dest.setEmail(mEmail.getText().toString());
		dest.setSubject(mSubject.getText().toString());
		dest.setComment(mComment.getText().toString());
		//dest.setFile()
		dest.setCaptcha(mCaptcha.getText().toString());
		return dest;
	}
	
	public void setImage(String filePath) {
		if (mNewPost != null) {
			mNewPost.setFile(filePath);
			if (mFileText == null) {
				mFileText = (TextView) findViewById(R.id.post_file_text);
			}
			if (mFileText != null) {
				if (filePath == null || filePath.length() == 0) {
					mFileText.setText("File: None");
				} else {
					mFileText.setText("File: "+(new File(filePath)).getName());
				}
			}
		} else {
			mNewPost.setFile(null);
		}
	}
	
	public void setCaptcha() {
		mCaptchaUrl = String.format("http://www.google.com/recaptcha/api/image?c=%s", mNewPost.getCaptchaToken());
		setCaptcha(ImageCache.get(mCaptchaUrl, mCapWaiter));
	}
	
	protected void setCaptcha(Bitmap image) {
		if (ImageCache.empty(image)) {
			setLoadingCaptcha(true);
		} else {
			mCaptchaImg.setImageBitmap(image);
			setLoadingCaptcha(false);
		}
	}
	
	private void setLoadingCaptcha(boolean loading) {
		if (loading)
			mCaptchaImg.setImageResource(R.drawable.progress_large_holo);
		mLoadingCaptcha = loading;
		setWillNotDraw(!loading);
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		if (mLoadingCaptcha && mCaptchaImg.getDrawable() != null) {
			LayerDrawable ld= (LayerDrawable)mCaptchaImg.getDrawable();
			for (int i=0; i<ld.getNumberOfLayers(); i++) {
				ld.getDrawable(i).setLevel((int) (((int) (System.currentTimeMillis() % 4000) / (float)4000) * 10000));
			}
		}
		super.onDraw(canvas);
	}
	
	private class ReloadCaptcha extends AsyncTask<NewPost, Void, Boolean> {
		
		private NewPost np;

		@Override
		protected Boolean doInBackground(NewPost... np) {
			this.np = np[0];
			return np[0].reloadCaptcha();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (np.equals(mNewPost)) {
				setCaptcha();
			}
		}

	}
	
	private class CaptchaWaiter implements ImageWaiter {

		@Override
		public void onLoadedImage(String key, Bitmap image) {
			
			if (key.equals(mCaptchaUrl)) {
				setCaptcha(image);
			}
			
		}
	}
	
}
