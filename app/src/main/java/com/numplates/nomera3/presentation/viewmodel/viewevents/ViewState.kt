package com.numplates.nomera3.presentation.viewmodel.viewevents

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.numplates.nomera3.data.network.core.ResponseError

/**
 * Реализация [LiveData] для использования с ViewState ивентами
 * */
class ViewStateLiveData<T> : LiveData<ViewState<T>> {

    constructor() : super()
    constructor(value: ViewState<T>) : super(value)

    fun set(value: ViewState<T>) {
        setValue(value)
    }

    fun post(value: ViewState<T>) {
        postValue(value)
    }

    fun setProgress(isShowProgress: Boolean = true) {
        value = ViewState.Progress(isShowProgress)
    }

    fun postProgress(isShowProgress: Boolean = true) {
        postValue(ViewState.Progress(isShowProgress))
    }

    fun setSuccess(value: T) {
        setValue(ViewState.Success(value))
    }

    fun postSuccess(value: T) {
        postValue(ViewState.Success(value))
    }

    fun setError(isShowError: Boolean = true, error: ResponseError?) {
        postValue(ViewState.Error(isShowError, error))
    }

    fun postError(isShowError: Boolean = true, error: ResponseError?) {
        postValue(ViewState.Error(isShowError, error))
    }

    fun observe(viewLifecycleOwner: LifecycleOwner, block: ViewState<T>.() -> Unit) {
        observe(viewLifecycleOwner, Observer {
            block(it)
        })
    }
}

/**
 * ViewState класс предназначен для разделения трех состояний ]
 * сетевого запроса [Success] [Progress] [Error]
 * */
sealed class ViewState<out T> {
    data class Success<T>(val data: T) : ViewState<T>()

    data class Error<T>(val isShowError: Boolean = true, val error: ResponseError?) : ViewState<T>()

    data class Progress<T>(val isShowProgress: Boolean = true) : ViewState<T>()

    inline fun onSuccess(block: (T) -> Unit) {
        if (this is Success) block(this.data)
    }

    inline fun onProgress(block: (isShowProgress: Boolean) -> Unit) {
        if (this is Progress) block(this.isShowProgress)
    }

    inline fun onError(block: (isShowError: Boolean, error: ResponseError?) -> Unit) {
        if (this is Error) block(this.isShowError, this.error)
    }
}

//Пример использования ViewState обертки с использованием
//kotlin extensions
//ViewModel.ViewStateLiveData.observe(viewLifecycleOwner) {
//    onSuccess { data ->
//
//    }
//    onProgress { isShowProgress ->
//
//    }
//    onError { isShowError, error ->
//
//    }
//}

