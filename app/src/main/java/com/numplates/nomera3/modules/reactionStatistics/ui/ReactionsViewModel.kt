package com.numplates.nomera3.modules.reactionStatistics.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.reactionStatistics.domain.GetCommentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentCommentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentViewersUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetPostReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabsUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionsUiEvent
import com.numplates.nomera3.modules.reactionStatistics.ui.mapper.ReactionsUIMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ReactionsViewModel @Inject constructor(
    private val getPostReactionsUseCase: GetPostReactionsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val getMomentReactionsUseCase: GetMomentReactionsUseCase,
    private val getMomentViewersUseCase: GetMomentViewersUseCase,
    private val getMomentCommentReactionsUseCase: GetMomentCommentReactionsUseCase,
    private val reactionsUIMapper: ReactionsUIMapper
) : ViewModel() {

    private val _reactionsEvent = MutableSharedFlow<ReactionsUiEvent>()
    val reactionsEvent: SharedFlow<ReactionsUiEvent> = _reactionsEvent

    private val _liveReactionTabs = MutableLiveData<ReactionTabsUiEntity>()
    val liveReactionTabsState: LiveData<ReactionTabsUiEntity>
        get() = _liveReactionTabs

    private val _liveViewsCountTabs = MutableLiveData<Long>()
    val liveViewsCountState: LiveData<Long>
        get() = _liveViewsCountTabs

    private var currentTabPosition: Int? = null

    private var isFirstSettingTabPosition: Boolean = true

    fun init(entityId: Long, entityType: ReactionsEntityType) {
        when (entityType) {
            ReactionsEntityType.POST -> getPostReactions(entityId)
            ReactionsEntityType.COMMENT -> getCommentReactions(entityId)
            ReactionsEntityType.MOMENT -> getMomentReactions(entityId)
            ReactionsEntityType.MOMENT_WITH_VIEWS-> getMomentReactionsAndViews(entityId)
            ReactionsEntityType.MOMENT_COMMENT -> getMomentCommentReactions(entityId)
        }
    }

    fun saveTabPosition(position: Int) {
        if (isFirstSettingTabPosition) {
            isFirstSettingTabPosition = false
            return
        }
        this.currentTabPosition = position
    }

    fun resetTabSetting() {
        isFirstSettingTabPosition = true
    }

    private fun getPostReactions(postId: Long) {
        viewModelScope.launch {
            runCatching {
                getPostReactionsUseCase.invoke(postId = postId, reaction = ALL, limit = 1, offset = 0)
            }.onSuccess {
                val items = reactionsUIMapper.mapToReactionTabUi(it)
                if (items.isEmpty()) handleEmpty() else _liveReactionTabs.postValue(getReactionTabsUiEntity(items))
            }.onFailure {
                handleError()
                Timber.e(it)
            }
        }
    }

    private fun getCommentReactions(commentId: Long) {
        viewModelScope.launch {
            runCatching {
                getCommentReactionsUseCase.invoke(commentId = commentId, reaction = ALL, limit = 1, offset = 0)
            }.onSuccess {
                val items = reactionsUIMapper.mapToReactionTabUi(it)
                if (items.isEmpty()) handleEmpty() else _liveReactionTabs.postValue(getReactionTabsUiEntity(items))
            }.onFailure {
                handleError()
                Timber.e(it)
            }
        }
    }

    private fun getMomentReactions(momentId: Long) {
        viewModelScope.launch {
            runCatching {
                getMomentReactionsUseCase.invoke(momentId = momentId, reaction = ALL, limit = 1, offset = 0)
            }.onSuccess {
                val items = reactionsUIMapper.mapToReactionTabUi(it)
                if (items.isEmpty()) handleEmpty() else _liveReactionTabs.postValue(getReactionTabsUiEntity(items))
            }.onFailure {
                handleError()
                Timber.e(it)
            }
        }
    }

    private fun getMomentReactionsAndViews(momentId: Long) {
        viewModelScope.launch {
            runCatching {
                val views = getMomentViewersUseCase.invoke(momentId = momentId, limit = 1, offset = 0)
                val reactions =
                    getMomentReactionsUseCase.invoke(momentId = momentId, reaction = ALL, limit = 1, offset = 0)
                Pair(views, reactions)
            }.onSuccess {
                val viewsCount = it.first.count
                _liveViewsCountTabs.postValue(viewsCount)
                val items = reactionsUIMapper.mapToReactionWithViewsTabUi(viewsCount, it.second)
                if (items.isEmpty()) handleEmpty() else _liveReactionTabs.postValue(getReactionTabsUiEntity(items))
            }.onFailure {
                handleError()
                Timber.e(it)
            }
        }
    }

    private fun getMomentCommentReactions(commentId: Long) {
        viewModelScope.launch {
            runCatching {
                getMomentCommentReactionsUseCase.invoke(commentId = commentId, reaction = ALL, limit = 1, offset = 0)
            }.onSuccess {
                val items = reactionsUIMapper.mapToReactionTabUi(it)
                if (items.isEmpty()) handleEmpty() else _liveReactionTabs.postValue(getReactionTabsUiEntity(items))
            }.onFailure {
                handleError()
                Timber.e(it)
            }
        }
    }

    private fun getReactionTabsUiEntity(items: List<ReactionTabUiEntity>): ReactionTabsUiEntity {
        currentTabPosition?.let { tabPosition ->
            if (items.size <= tabPosition) {
                currentTabPosition = null
            }
        }

        return ReactionTabsUiEntity(items = items, selectedTab = currentTabPosition)
    }

    private fun emitViewEvent(event: ReactionsUiEvent) {
        viewModelScope.launch {
            _reactionsEvent.emit(event)
        }
    }

    private fun handleError() {
        emitViewEvent(ReactionsUiEvent.ShowErrorToast(R.string.error_message_went_wrong))
    }

    private fun handleEmpty() {
        emitViewEvent(ReactionsUiEvent.ShowReactionsIsEmpty)
    }

    companion object {
        const val ALL = "all"
    }
}
