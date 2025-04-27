package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.numplates.nomera3.App
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.presentation.model.adaptermodel.CheckedListModel
import com.numplates.nomera3.presentation.view.adapter.checkedList.AllRegionsStorage
import com.numplates.nomera3.presentation.view.adapter.checkedList.CheckedListSourceFactory
import java.util.concurrent.Executors
import javax.inject.Inject

class LocationViewModel: ViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    lateinit var pagedListLiveData: LiveData<PagedList<CheckedListModel>>

    fun init(){
        App.component.inject(this)
        initPaging()
    }

    private fun initPaging() {
        val regionStorage = AllRegionsStorage()
        val sourceFactory = CheckedListSourceFactory(regionStorage)
        val pagedConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build()

        pagedListLiveData = LivePagedListBuilder(sourceFactory, pagedConfig)
                .setFetchExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
                .build()
    }
}
