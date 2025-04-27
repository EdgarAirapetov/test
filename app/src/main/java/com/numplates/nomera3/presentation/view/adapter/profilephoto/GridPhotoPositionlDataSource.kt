package com.numplates.nomera3.presentation.view.adapter.profilephoto

import androidx.paging.PositionalDataSource

class GridPhotoPositionlDataSource(private var storage:ProfilePhotoStorage): PositionalDataSource<GridPhotoRecyclerModel> (){

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<GridPhotoRecyclerModel>) {
        storage.getDataRange(params.startPosition, 10, callback)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<GridPhotoRecyclerModel>) {
        storage.getDataInitial(0, 10, callback)
    }
}
