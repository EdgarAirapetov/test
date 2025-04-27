package com.numplates.nomera3.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.presentation.viewmodel.viewevents.ViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application){


    fun <T> requestCallback(
            request: suspend CoroutineScope.() -> ResponseWrapper<T>,
            response: ViewState<T>.() -> Unit,
            isShowProgress: Boolean = true,
            isShowError: Boolean = true) {
        response(ViewState.Progress(isShowProgress))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = request()
                launch(Dispatchers.Main) {
                    if (res.data != null) {
                        response(ViewState.Success(res.data))
                    } else if (res.err != null) {
                        response(ViewState.Error(isShowError, res.err))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                response(ViewState.Error(isShowError, null))
            }
        }
    }
}