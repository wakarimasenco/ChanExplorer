package co.wakarimasen.chanexplorer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class PostMainView extends FrameLayout {

	private boolean mRecieveTouch = true;
	private OnClickListener mListener = null;

	public PostMainView(Context context) {
		super(context);
	}

	public PostMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PostMainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void enableTouch() {
		mRecieveTouch = true;
		//enableDisableViewGroup(this, mRecieveTouch);
	}
	
	public void disableTouch(OnClickListener listener) {
		mListener = listener;
		mRecieveTouch = false;
		//enableDisableViewGroup(this, mRecieveTouch);
	}

	@Override
	public boolean onInterceptTouchEvent (MotionEvent event) {
		if (mRecieveTouch) {
			return super.onInterceptTouchEvent(event);
		} else {

			int action = event.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mListener.onClick(this);
				return true;
			}
			return super.onInterceptTouchEvent(event);
		}
	}
	
	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
	    int childCount = viewGroup.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	      View view = viewGroup.getChildAt(i);
	      view.setEnabled(enabled);
	      view.setClickable(enabled);
	      if (view instanceof ViewGroup) {
	        enableDisableViewGroup((ViewGroup) view, enabled);
	      }
	    }
	  }
}