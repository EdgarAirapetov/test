package com.numplates.nomera3.modules.viewvideo.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ViewVideoViewModel @Inject constructor() : ViewModel() {

    private val _viewVideoListState: MutableLiveData<Any> = MutableLiveData()
    val viewVideoScreenState: LiveData<Any> = _viewVideoListState

}

