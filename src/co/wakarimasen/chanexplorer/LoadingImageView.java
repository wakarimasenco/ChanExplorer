package co.wakarimasen.chanexplorer;

import co.wakarimasen.chanexplorer.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class LoadingImageView extends TouchImageView {
	private boolean mLoading = false;
	private int mPrevMax = 0;

	public LoadingImageView(Context context) {
		super(context);
	}

	public LoadingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//public LoadingImageView(Context context, AttributeSet attrs, int defStyle) {
	//	super(context, attrs, defStyle);
	//}

	@SuppressWarnings("deprecation")
	public void setLoading(int current, int max) {
		if (!mLoading) {
			mLoading = true;
			mPrevMax = 0;
		}
		if (max != mPrevMax) {
			setScaleType(ScaleType.CENTER);
			Resources r = getContext().getResources();
			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());
			//android.util.Log.d("CHAN", "Margins: "+px);
			//setPadding((int)px, 0, 0, 0);
			//measure(0,0);
			((ViewGroup.MarginLayoutParams)getLayoutParams()).setMargins((int)px, 0, (int)px, 0);
			((FrameLayout.LayoutParams)getLayoutParams()).gravity = Gravity.CENTER;
			if (max == -1) {
				setImageResource(R.drawable.progress_image_holo_ind);
				setBackgroundDrawable(null);
			} else {
				setImageDrawable(null);
				setBackgroundResource(R.drawable.progress_image_holo);
			}
			mPrevMax = max;
		}
		final Drawable d = (mPrevMax == -1) ? getDrawable() : getBackground();
		if (d instanceof LayerDrawable) {
			LayerDrawable ld = (LayerDrawable) d;
			for (int i = 1; i < ld.getNumberOfLayers(); i++) {
				ld.getDrawable(i).setLevel(
						(int) (((current) / (float) max) * 10000));
			}
		} else if (d instanceof AnimationDrawable) {
			if (!((AnimationDrawable) getDrawable()).isRunning()) {
				post(new Runnable() {
					@Override
					public void run() {
						if (getDrawable() instanceof AnimationDrawable)  {
							((AnimationDrawable) getDrawable()).setOneShot(false);
							((AnimationDrawable) getDrawable()).start();
						}
					}
				});
			}
		}
	}

	public void finishLoading() {
		mLoading = false;
		//setPadding(6,6,6,6);
		//measure(0,0);
		//setBackgroundDrawable(null);
		((ViewGroup.MarginLayoutParams)getLayoutParams()).setMargins(0,0,0,0);
	}
	
	public boolean isLoading() {
		return mLoading;
	}
}
