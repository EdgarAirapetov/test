package com.numplates.nomera3.presentation.view.widgets.gallery.fullscreen;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.numplates.nomera3.App;
import com.numplates.nomera3.presentation.view.adapter.GalleryPreviewMaxValueRecyclerAdapter;


public class ExtendedRecyclerView extends RecyclerView {

	public int itemHeight;
	public int itemQuantityOnScreen;

	public ExtendedRecyclerView(Context context) {
	    super(context);
	}

	public ExtendedRecyclerView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	

	@Override
	public void scrollToPosition(int position) {
		if(getAdapter() instanceof GalleryPreviewMaxValueRecyclerAdapter) {
			GalleryPreviewMaxValueRecyclerAdapter adapter = (GalleryPreviewMaxValueRecyclerAdapter)getAdapter();
			if(position < App.MAX_ITEMS_INFINITY_RECYCLER / 4 ) {
				position = App.MAX_ITEMS_INFINITY_RECYCLER / 2 - (App.MAX_ITEMS_INFINITY_RECYCLER / 2) % adapter.getRealCount() + position;
			}
		}
		super.scrollToPosition(position);
	}
}
