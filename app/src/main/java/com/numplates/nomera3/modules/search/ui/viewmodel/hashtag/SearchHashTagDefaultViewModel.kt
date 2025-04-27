package com.numplates.nomera3.modules.search.ui.viewmodel.hashtag

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.domain.mapper.recent.SearchRecentHashTagMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentHashtagsParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentHashtagsUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentHashTagParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentHashTagUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.HashTagSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.base.CLEAR_RECENT_DELAY_MS
import com.numplates.nomera3.modules.search.ui.viewmodel.base.CLEAR_RECENT_DELAY_SEC
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchDefaultScreenBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SearchHashTagDefaultViewModel : SearchDefaultScreenBaseViewModel() {

    @Inject
    lateinit var searchRecentUseCase: SearchRecentHashTagUseCase

    @Inject
    lateinit var searchCleanRecentUseCase: SearchCleanRecentHashtagsUseCase

    private var lastRecentItems: List<SearchItem> = emptyList()
    private val recentTitle = SearchItem.Title(
        R.string.search_recent_list_title,
        R.string.search_recent_list_clear_button
    )

    init {
        App.component.inject(this)
    }

    override fun reload() {
        showLastRecent()
        loadAndShowRecent()
    }

    override fun undoClearRecent() {
        super.undoClearRecent()

        showLastRecent()
    }

    override fun clearRecent(force: Boolean) {
        showEmpty()

        publishMessage(SearchMessageViewEvent.ClearRecentMessage(CLEAR_RECENT_DELAY_SEC))

        clearRecentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!force) {
                delay(CLEAR_RECENT_DELAY_MS)
            }

            searchCleanRecentUseCase.execute(
                params = SearchCleanRecentHashtagsParams(),
                success = {
                    lastRecentItems = emptyList()
                    showEmpty()
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    Timber.e(exception)
                }
            )
        }
    }

    fun selectHashTagItem(item: SearchItem.HashTag) {
        publishEvent(HashTagSearchViewEvent.OpenHashTag(item))
    }

    fun showLastRecent() {
        publishList(SearchResultViewState.DefaultResult(lastRecentItems))
    }

    fun showEmpty() {
        publishList(SearchResultViewState.DefaultResult(emptyList()))
    }

    fun loadAndShowRecent() {
        showLoading()

        viewModelScope.launch(Dispatchers.IO) {
            searchRecentUseCase.execute(
                params = SearchRecentHashTagParams(),
                success = { response ->
                    lastRecentItems = SearchRecentHashTagMapper(recentTitle).map(response)
                    publishList(SearchResultViewState.DefaultResult(lastRecentItems))
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }
    }

    // При закрытии экрана,
    // viewModelScope сбрасывается и searchCleanRecentUseCase не может до конца выполняться,
    // Что бы выполнять запорс полностью нужно его обработать в долго живущим скопе(GlobalScope)

    fun clearRecentGlobalIfExists(){
        if (isClearingRecent()) {
            GlobalScope.launch(Dispatchers.IO) {
                searchCleanRecentUseCase.execute(
                    params = SearchCleanRecentHashtagsParams(),
                    success = {
                        Timber.i("Success delete recents:${it}")
                        cancel()
                    },
                    fail = { exception ->
                        Timber.i(exception)
                        cancel()
                    }
                )
            }
        }
    }
}
