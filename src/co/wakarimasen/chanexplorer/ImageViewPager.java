package co.wakarimasen.chanexplorer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class ImageViewPager extends ViewPager {

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ImageViewPager(Context context) {
        super(context);
    }
    @Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y) || (checkV && customCanScroll(v));
    }
    protected boolean customCanScroll(View v) {
        if (v instanceof TouchImageView) {
        	TouchImageView hsvChild = ((TouchImageView) v);
            if (hsvChild.getZoom() >= 1.08) {
            	return true;
            }
        }
        return false;
   }
}