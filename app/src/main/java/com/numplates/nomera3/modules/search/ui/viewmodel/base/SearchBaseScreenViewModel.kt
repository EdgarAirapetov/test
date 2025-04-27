package com.numplates.nomera3.modules.search.ui.viewmodel.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.SearchBaseViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class SearchBaseScreenViewModel : ViewModel() {

    protected val disposables = CompositeDisposable()

    protected val resultState = MutableLiveData<SearchResultViewState>()
    protected val messageStream = PublishSubject.create<SearchMessageViewEvent>()
    protected val eventStream = PublishSubject.create<SearchBaseViewEvent>()

    fun getResultState(): LiveData<SearchResultViewState> {
        return resultState
    }

    fun getMessageStream(): Subject<SearchMessageViewEvent> {
        return messageStream
    }

    fun getEventStream(): Subject<SearchBaseViewEvent> {
        return eventStream
    }

    fun publishMessage(messageViewEvent: SearchMessageViewEvent) {
        messageStream.onNext(messageViewEvent)
    }

    fun publishEvent(event: SearchBaseViewEvent) {
        eventStream.onNext(event)
    }

    fun publishList(state: SearchResultViewState.Data) {
        resultState.postValue(state)
    }

    fun showLoading() {
        publishEvent(SearchBaseViewEvent.ShowLoading)
    }

    protected fun showShimmerLoading() {
        publishEvent(SearchBaseViewEvent.ShowShimmerLoading)
    }

    fun publishEmptyData() {
        publishList(SearchResultViewState.Data(emptyList()))
    }

    protected open fun getAllSearchItems(): List<SearchItem> {
        return getCurrentRenderData().value
    }

    protected open fun getCurrentRenderData(): SearchResultViewState.Data {
        return resultState.value as SearchResultViewState.Data
    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }
}
