package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.tryCatch
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.RxEventsJava
import com.numplates.nomera3.presentation.viewmodel.exception.Failure
import com.numplates.nomera3.presentation.viewmodel.viewevents.Event
import com.numplates.nomera3.presentation.viewmodel.viewevents.ViewState
import com.numplates.nomera3.presentation.viewmodel.viewevents.ViewStateLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@Deprecated("Используйте стандартную ViewModel")
abstract class BaseViewModel : ViewModel() {

    protected val baseCompositeDisposable = CompositeDisposable()

    var failure: MutableLiveData<Failure> = MutableLiveData()

    protected fun handleFailure(failure: Failure) {
        this.failure.postValue(failure)
    }

    @Deprecated("Use requestLiveData with ViewState")
            /**
             * Send signal to RxBus refresh token
             */
    fun requestToRefreshToken() {
        App.bus.send(RxEventsJava.MustRefreshToken())
    }


    @Deprecated("Use viewModelScope.launch()")
    fun <T> requestWithLiveData(liveData: MutableLiveData<Event<T>>, request: suspend () -> ResponseWrapper<T>) {
        liveData.postValue(Event.loading())
        this.viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = request.invoke()
                if (response.data != null) {
                    liveData.postValue(Event.success(response.data))
                } else if (response.err != null) {
                    liveData.postValue(Event.error(response.err))
//                    errorWithLiveData(response.err, liveData) {
//                        requestWithLiveData(liveData, request)
//                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                liveData.postValue(Event.error(null))
            }
        }
    }

    @Deprecated("Use viewModelScope.launch()")
    fun <T> requestWithCallback(request: suspend () -> ResponseWrapper<T>, response: (Event<T>) -> Unit) {
        response(Event.loading())
        this.viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = request.invoke()

                launch(Dispatchers.Main) {
                    if (res.data != null) {
                        response(Event.success(res.data))
                    } else if (res.err != null) {
                        response(Event.error(res.err))
//                        errorWithCallback<T>(res.err, response) {
//                            requestWithCallback(request, response)
//                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                response(Event.error(null))
            }
        }
    }

    @Deprecated("Use viewModelScope.launch()")
    // Здесь можно использовать реализацию Event sealed классом ViewState
    fun <T> requestLiveData(
        viewStateLiveData: ViewStateLiveData<T>,
        isShowProgress: Boolean = true,
        isShowError: Boolean = true,
        request: suspend CoroutineScope.() -> ResponseWrapper<T>
    ) {
        viewStateLiveData.postProgress(isShowProgress)
        viewModelScope.launch(Dispatchers.IO) {
            tryCatch({
                val response = request()
                if (response.data != null) {
                    viewStateLiveData.postSuccess(response.data)
                } else if (response.err != null) {
                    viewStateLiveData.postError(isShowError, response.err)
                }
            }, {
                viewStateLiveData.postError(isShowError, null)
            })
        }
    }

    @Deprecated("Use viewModelScope.launch()")
    fun <T> requestCallback(
        request: suspend CoroutineScope.() -> ResponseWrapper<T>,
        response: ViewState<T>.() -> Unit,
        isShowProgress: Boolean = true,
        isShowError: Boolean = true
    ) {
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

    /**
     * Launcher for coroutines on Dispatchers.IO with callback represented as a one interface
     */
    @Deprecated("Use viewModelScope.launch()")
    fun <T> runWithDispatcherIO(
        coroutine: CoroutineScope.() -> T,
        executionState: CoroutineExecutionState<CoroutineResult<T>>
    ) {
        viewModelScope.launch {
            executionState.onStart()
            try {
                executionState.onSuccess(withContext(Dispatchers.IO) { CoroutineResult.Success(coroutine()) })
            } catch (e: Exception) {
                executionState.onError(CoroutineResult.Error(e))
            }
        }
    }

    /**
     * Launcher for coroutines on Dispatchers.IO with callback represented as functions
     */
    @Deprecated("Use viewModelScope.launch()")
    fun <T> runWithDispatcherIO(
        coroutine: suspend CoroutineScope.() -> T,
        onStart: suspend () -> Unit,
        onSuccess: suspend (result: CoroutineResult<T>) -> Unit,
        onError: suspend (result: CoroutineResult<T>) -> Unit
    ) {
        viewModelScope.launch {
            onStart()
            try {
                onSuccess(withContext(Dispatchers.IO) { CoroutineResult.Success(coroutine()) })
            } catch (e: Exception) {
                onError(CoroutineResult.Error(e))
            }
        }
    }

    /**
     * Launcher for coroutines on Dispatchers.IO with callback represented as functions
     */
    @Deprecated("Use viewModelScope.launch()")
    fun <T> runWithDispatcherIORaw(
        coroutine: suspend CoroutineScope.() -> T,
        onStart: suspend () -> Unit,
        onSuccess: suspend (result: T) -> Unit,
        onError: suspend (e: Exception) -> Unit
    ): Job {
        return viewModelScope.launch {
            onStart()
            try {
                onSuccess(withContext(Dispatchers.IO) { coroutine() })
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Helper interface for runWithDispatcherIO(...)
     */
    interface CoroutineExecutionStep<T> {
        fun accept(result: T) {}
    }

    /**
     * Helper interface for runWithDispatcherIO(...)
     */
    interface CoroutineExecutionState<T> {
        fun onStart() {}
        fun onSuccess(result: T) {}
        fun onError(error: T) {}
    }

    /**
     * Helper class for runWithDispatcherIO(...)
     */
    sealed class CoroutineResult<out T> {
        data class Success<out T>(val data: T) : CoroutineResult<T>()
        data class Error(val exception: Exception) : CoroutineResult<Nothing>()
    }

    /**
     * For common purpose
     */
    sealed class CommonResult<out T> {
        object Loading : CommonResult<Nothing>()
        data class Success<T>(val data: T) : CommonResult<T>()
        data class Error(val throwable: Throwable?) : CommonResult<Nothing>()
    }

    override fun onCleared() {
        super.onCleared()
        baseCompositeDisposable.clear()
    }

    fun Disposable.addDisposable() {
        baseCompositeDisposable.add(this)
    }

}
