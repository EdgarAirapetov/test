package com.numplates.nomera3.modules.search.ui.viewmodel.group

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.search.domain.mapper.recent.SearchRecentGroupsMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentGroupsParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentGroupsUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentGroupsParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentGroupsUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.GroupSearchViewEvent
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

class SearchGroupDefaultViewModel : SearchDefaultScreenBaseViewModel() {
    @Inject
    lateinit var searchRecentUseCase: SearchRecentGroupsUseCase

    @Inject
    lateinit var searchCleanRecentUseCase: SearchCleanRecentGroupsUseCase

    private var lastRecentBlock: SearchItem.RecentBlock? = null

    init {
        App.component.inject(this)
    }

    fun showEmpty() {
        publishList(SearchResultViewState.DefaultResult(emptyList()))
    }

    fun showLastRecent() {
        publishList(SearchResultViewState.DefaultResult(listOfNotNull(lastRecentBlock)))
    }

    override fun reload() {
        showLastRecent()
        loadAndSearchRecent()
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
                params = SearchCleanRecentGroupsParams(),
                success = {
                    lastRecentBlock = null
                    showEmpty()
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    Timber.e(exception)
                }
            )
        }
    }

    fun selectRecentItem(item: SearchItem.RecentBlock.RecentBaseItem.RecentGroup) {
        publishEvent(GroupSearchViewEvent.SelectGroup(item.groupId))
    }

    fun selectGroupItem(item: SearchItem.Group) {
        publishEvent(GroupSearchViewEvent.SelectGroup(item.groupId))
    }

    fun loadAndSearchRecent() {
        showLoading()

        viewModelScope.launch(Dispatchers.IO) {
            searchRecentUseCase.execute(
                params = SearchRecentGroupsParams(),
                success = { response ->
                    lastRecentBlock = SearchRecentGroupsMapper().map(response)
                    val newState =
                        SearchResultViewState.DefaultResult(listOfNotNull(lastRecentBlock))

                    publishList(newState)
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
                    params = SearchCleanRecentGroupsParams(),
                    success = {
                        Timber.i("Success delete recents:${it}")
                        cancel()
                    },
                    fail = { exception ->
                        Timber.e(exception)
                        cancel()
                    }
                )
            }
        }
    }
}
