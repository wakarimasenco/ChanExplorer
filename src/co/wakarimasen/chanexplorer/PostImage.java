package co.wakarimasen.chanexplorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PostImage extends ImageView {
	
	private SizeChangedListener mSzChangedListener = null;
	private PostView mPost;
	
	public PostImage(Context context) {
		super(context);
	}

	public PostImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PostImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnSizeChangedListener(SizeChangedListener listener) {
		mSzChangedListener = listener;
	}
	
	public void setPost(PostView post) {
		mPost = post;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (getDrawable() != null && getDrawable() instanceof BitmapDrawable) {
			if (((BitmapDrawable)getDrawable()).getBitmap().isRecycled()) {
				mPost.setImage(ImageCache.get(mPost.mImageKey, mPost));
			}
		}
		super.onDraw(canvas);
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		if (mSzChangedListener != null) {
			mSzChangedListener.onSizeChanged(w,h,oldw,oldh);
		}
	}
	
	public interface SizeChangedListener {
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}
}
