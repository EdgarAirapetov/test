package com.numplates.nomera3.presentation.view.adapter.checkedList

import androidx.paging.DataSource
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel

class CheckedListSourceFactory(
        private val storage: AllRegionsStorage
): DataSource.Factory<Int, CheckedListModel>() {

    private lateinit var dataSource: CheckedListDataSource

    override fun create(): DataSource<Int, CheckedListModel> {
        dataSource = CheckedListDataSource(storage)
        return dataSource
    }

}