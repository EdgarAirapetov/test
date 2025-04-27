package com.numplates.nomera3.modules.search.ui.viewmodel.user

import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.search.domain.mapper.recent.SearchRecentUsersMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentUserParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentUsersUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.UserSearchViewEvent
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

@Deprecated("use SearchUserViewModel")
class SearchUserDefaultViewModel : SearchDefaultScreenBaseViewModel() {

    @Inject
    lateinit var searchRecentUseCase: SearchRecentUsersUseCase

    @Inject
    lateinit var searchCleanRecentUseCase: SearchCleanRecentUsersUseCase

    private var lastRecentBlock: SearchItem.RecentBlock? = null

    init {
        App.component.inject(this)

        showLastRecent()
        loadAndShowRecent()
    }

    override fun reload() {
        showLastRecent()
        loadAndShowRecent()
    }

    override fun undoClearRecent() {
        super.undoClearRecent()

        showLastRecent()
    }

    fun showLastRecent() {
        publishList(SearchResultViewState.DefaultResult(listOfNotNull(lastRecentBlock)))
    }

    override fun clearRecent(force: Boolean) {
        showEmpty()

        publishMessage(SearchMessageViewEvent.ClearRecentMessage(CLEAR_RECENT_DELAY_SEC))

        clearRecentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!force) {
                delay(CLEAR_RECENT_DELAY_MS)
            }

            searchCleanRecentUseCase.execute(
                params = SearchCleanRecentUsersParams(),
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

    fun selectRecentItem(recentUser: SearchItem.RecentBlock.RecentBaseItem.RecentUser) {
        publishEvent(
            UserSearchViewEvent.SelectUser(
                userId = recentUser.uid,
                isRecent = true,
                approved = recentUser.approved,
                topContentMaker = recentUser.topContentMaker
            )
        )
    }

    fun selectUserItem(user: SearchItem.User) {
        publishEvent(
            UserSearchViewEvent.SelectUser(
                userId = user.uid,
                isRecent = false,
                approved = user.approved.toBoolean(),
                topContentMaker = user.topContentMaker.toBoolean()
            )
        )
    }

    fun showEmpty() {
        publishList(SearchResultViewState.DefaultResult(emptyList()))
    }

    fun loadAndShowRecent() {
        showLoading()

        viewModelScope.launch(Dispatchers.IO) {
            searchRecentUseCase.execute(
                params = SearchRecentUserParams(),
                success = { response ->
                    lastRecentBlock = SearchRecentUsersMapper().map(response)

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

    fun clearRecentGlobalIfExists() {
        if (isClearingRecent()) {
            GlobalScope.launch(Dispatchers.IO) {
                searchCleanRecentUseCase.execute(
                    params = SearchCleanRecentUsersParams(),
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
