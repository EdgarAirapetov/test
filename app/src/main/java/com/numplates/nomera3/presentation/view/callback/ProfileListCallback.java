package com.numplates.nomera3.presentation.view.callback;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by abelov
 */
public abstract class ProfileListCallback {

    abstract public void onClick(RecyclerView.ViewHolder holder);
    public void onRemove(RecyclerView.ViewHolder holder) {

    }
    abstract public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

}
