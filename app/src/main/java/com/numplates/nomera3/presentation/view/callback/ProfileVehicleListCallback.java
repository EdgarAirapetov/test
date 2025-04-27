package com.numplates.nomera3.presentation.view.callback;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by abelov
 */
public abstract class ProfileVehicleListCallback extends ProfileListZeroDataCallback{
    abstract public void onClick(RecyclerView.ViewHolder holder);
//    abstract public boolean isOwner();
}
