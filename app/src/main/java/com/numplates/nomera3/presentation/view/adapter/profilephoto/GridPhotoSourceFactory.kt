package com.numplates.nomera3.presentation.view.adapter.profilephoto

import androidx.paging.DataSource

class GridPhotoSourceFactory(
        private val storage: ProfilePhotoStorage
): DataSource.Factory<Int, GridPhotoRecyclerModel>() {
    private lateinit var dataSource: GridPhotoPositionlDataSource

    override fun create(): DataSource<Int, GridPhotoRecyclerModel>{
        storage.refresh()
        dataSource = GridPhotoPositionlDataSource(storage)
        return dataSource
    }

    fun invalidate(){
        dataSource.invalidate()
    }

}
