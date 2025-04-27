package com.numplates.nomera3.presentation.view.widgets.gallery.fullscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.numplates.nomera3.App;
import com.numplates.nomera3.presentation.view.adapter.InfinityAdapter;


public class ExtendedViewPager extends ViewPager {

	public ExtendedViewPager(Context context) {
	    super(context);
	}
	
	public ExtendedViewPager(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
	    if (v instanceof TouchImageView) {
	    	//
	    	// canScrollHorizontally is not supported for Api < 14. To get around this issue,
	    	// ViewPager is extended and canScrollHorizontallyFroyo, a wrapper around
	    	// canScrollHorizontally supporting Api >= 8, is called.
	    	//
	        return ((TouchImageView) v).canScrollHorizontallyFroyo(-dx);
	        
	    } else {
	        return super.canScroll(v, checkV, dx, x, y);
	    }
	}

	@Override
	public void setCurrentItem(int item) {
		if(getAdapter() instanceof InfinityAdapter) {
			InfinityAdapter adapter = (InfinityAdapter)getAdapter();
			if(/*getCurrentItem() */item < App.MAX_ITEMS_INFINITY_RECYCLER / 4 ) {
				item = App.MAX_ITEMS_INFINITY_RECYCLER / 2 - (App.MAX_ITEMS_INFINITY_RECYCLER / 2) % adapter.getRealCount() + item;
			}
		}
		super.setCurrentItem(item);
	}
}
