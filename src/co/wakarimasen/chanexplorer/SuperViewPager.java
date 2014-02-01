package co.wakarimasen.chanexplorer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class SuperViewPager extends ViewPager {

    public SuperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SuperViewPager(Context context) {
        super(context);
    }
    @Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y) || (checkV && customCanScroll(v));
    }
    protected boolean customCanScroll(View v) {
        if (v instanceof HorizontalScrollView) {
            View hsvChild = ((HorizontalScrollView) v).getChildAt(0);
            if (hsvChild.getWidth() > v.getWidth())
                return true;
        }
        return false;
   }
}