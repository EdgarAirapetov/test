package com.numplates.nomera3.presentation.view.adapter.checkedList

import androidx.paging.PositionalDataSource
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel
import timber.log.Timber

class AllRegionsStorage {
    fun loadRange(start: Int, loadSize: Int, callback: PositionalDataSource.LoadRangeCallback<CheckedListModel>) {
        Timber.d("AllRegionsStorage loadRange: start = $start, loadSize = $loadSize")
        callback.onResult(provideFakeData())
    }

    fun loadInitial(startPosition: Int, size: Int, callback: PositionalDataSource.LoadInitialCallback<CheckedListModel>) {
        Timber.d("AllRegionsStorage loadInitial: startPosition = $startPosition, size = $size")
        val data = mutableListOf<CheckedListModel>()
        data.add(CheckedListModel(viewType = CheckedPagedAdapter.VIEW_TYPE_CHECKED_LOCATION))
        data.add(CheckedListModel(viewType =CheckedPagedAdapter.VIEW_TYPE_HEADER))
        data.addAll(provideFakeData())
        callback.onResult(data, 0)
    }

    private fun provideFakeData(): MutableList<CheckedListModel> {
        Timber.d("AllRegionsStorage provideFakeData called")
        val res = mutableListOf<CheckedListModel>()
        repeat(10) {
            res.add(CheckedListModel())
        }
        return res
    }

    fun clear() = Unit
}
