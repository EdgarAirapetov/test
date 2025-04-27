package com.numplates.nomera3.presentation.view.ui;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class OverlapDecoration extends RecyclerView.ItemDecoration {

    private /*final static*/ int vertOverlap = -40;

    public OverlapDecoration(int dpOverlap) {
        vertOverlap  = -dpOverlap;
    }

    @Override
    public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == 0) {
            return; }
        outRect.set(vertOverlap, 0, 0, 0);
    }
}
