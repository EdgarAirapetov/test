package com.numplates.nomera3.presentation.view.adapter.checkedList

import androidx.paging.PositionalDataSource
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel

class CheckedListDataSource(
        private val storage: AllRegionsStorage
): PositionalDataSource<CheckedListModel>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<CheckedListModel>) {
        storage.loadRange(params.startPosition, params.loadSize, callback)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<CheckedListModel>) {
        storage.loadInitial(0, params.pageSize, callback)
    }
}