package com.numplates.nomera3.presentation.view.widgets.gallery.fullscreen;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class GalleryPreviewLayoutManager extends LinearLayoutManager {
    ExtendedRecyclerView recyclerView;

    public GalleryPreviewLayoutManager(Context context, int orientation, boolean reverseLayout, ExtendedRecyclerView recyclerView) {
        super(context, orientation, reverseLayout);
        this.recyclerView = recyclerView;
    }

    public GalleryPreviewLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void scrollToPosition(int position) {
        recyclerView.post(() -> GalleryPreviewLayoutManager.this.scrollToPositionWithOffset(position, recyclerView.getWidth()/2 - recyclerView.itemHeight/2));
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {


        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return GalleryPreviewLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }

        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

}
