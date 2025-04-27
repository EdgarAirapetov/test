package com.numplates.nomera3.modules.reactionStatistics.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.reactionStatistics.domain.GetCommentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentCommentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetMomentViewersUseCase
import com.numplates.nomera3.modules.reactionStatistics.domain.GetPostReactionsUseCase
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.mapper.ReactionsUIMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ReactionsPageViewModel @Inject constructor(
    private val getPostReactionsUseCase: GetPostReactionsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val getMomentReactionsUseCase: GetMomentReactionsUseCase,
    private val getMomentViewersUseCase: GetMomentViewersUseCase,
    private val getMomentCommentReactionsUseCase: GetMomentCommentReactionsUseCase,
    private val reactionsUIMapper: ReactionsUIMapper
) : ViewModel() {

    private val _liveReactionUsers = MutableLiveData<List<ReactionUserUiEntity>>()
    val liveReactionUsersState: LiveData<List<ReactionUserUiEntity>>
        get() = _liveReactionUsers

    var entityId: Long? = null
    var entityType: ReactionsEntityType? = null
    var reaction: String? = null
    var isViewersPage: Boolean = false

    private val limit: Int = 30

    private var moreItems: Boolean = true

    fun initReactionsPage(entityId: Long, entityType: ReactionsEntityType, reaction: String? = null) {
        this.entityId = entityId
        this.entityType = entityType
        this.reaction = reaction

        when (entityType) {
            ReactionsEntityType.POST -> getPostReactions()
            ReactionsEntityType.COMMENT -> getCommentReactions()
            ReactionsEntityType.MOMENT,
            ReactionsEntityType.MOMENT_WITH_VIEWS -> getMomentReactions()
            ReactionsEntityType.MOMENT_COMMENT -> getMomentCommentReactions()
        }
    }

    fun initViewersPage(entityId: Long, entityType: ReactionsEntityType) {
        this.entityId = entityId
        this.entityType = entityType
        this.isViewersPage = true

        getMomentViewers()
    }

    var loading: Job? = null

    private fun getPostReactions() {
        loading = viewModelScope.launch {
            val loadedItems = _liveReactionUsers.value ?: emptyList()
            val offset = loadedItems.size
            runCatching {
                getPostReactionsUseCase.invoke(
                    postId = entityId ?: -1, reaction = reaction ?: "", limit = limit, offset = offset
                ).first()
            }.onSuccess {
                moreItems = it.more.toBoolean()
                _liveReactionUsers.postValue(loadedItems + reactionsUIMapper.mapToReactionUserUi(it.reactions))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun getCommentReactions() {
        loading = viewModelScope.launch {
            val loadedItems = _liveReactionUsers.value ?: emptyList()
            val offset = loadedItems.size
            runCatching {
                getCommentReactionsUseCase.invoke(
                    commentId = entityId ?: -1, reaction = reaction ?: "", limit = limit, offset = offset
                ).first()
            }.onSuccess {
                moreItems = it.more.toBoolean()
                _liveReactionUsers.postValue(loadedItems + reactionsUIMapper.mapToReactionUserUi(it.reactions))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun getMomentReactions() {
        loading = viewModelScope.launch {
            val loadedItems = _liveReactionUsers.value ?: emptyList()
            val offset = loadedItems.size
            runCatching {
                getMomentReactionsUseCase.invoke(
                    momentId = entityId ?: -1, reaction = reaction ?: "", limit = limit, offset = offset
                ).first()
            }.onSuccess {
                moreItems = it.more.toBoolean()
                _liveReactionUsers.postValue(loadedItems + reactionsUIMapper.mapToReactionUserUi(it.reactions))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun getMomentViewers() {
        loading = viewModelScope.launch {
            val loadedItems = _liveReactionUsers.value ?: emptyList()
            val offset = loadedItems.size
            runCatching {
                getMomentViewersUseCase.invoke(
                    momentId = entityId ?: -1, limit = limit, offset = offset
                )
            }.onSuccess {
                moreItems = it.more.toBoolean()
                _liveReactionUsers.postValue(loadedItems + reactionsUIMapper.mapToViewerUserUi(it))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun getMomentCommentReactions() {
        loading = viewModelScope.launch {
            val loadedItems = _liveReactionUsers.value ?: emptyList()
            val offset = loadedItems.size
            runCatching {
                getMomentCommentReactionsUseCase.invoke(
                    commentId = entityId ?: -1, reaction = reaction ?: "", limit = limit, offset = offset
                ).first()
            }.onSuccess {
                moreItems = it.more.toBoolean()
                _liveReactionUsers.postValue(loadedItems + reactionsUIMapper.mapToReactionUserUi(it.reactions))
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun loadMore() {
        when (entityType) {
            ReactionsEntityType.POST -> getPostReactions()
            ReactionsEntityType.COMMENT -> getCommentReactions()
            ReactionsEntityType.MOMENT -> getMomentReactions()
            ReactionsEntityType.MOMENT_WITH_VIEWS ->
                if (isViewersPage) {
                    getMomentViewers()
                } else {
                    getMomentReactions()
                }
            ReactionsEntityType.MOMENT_COMMENT -> getMomentCommentReactions()
            null -> Unit
        }
    }

    fun isLastPage(): Boolean {
        return moreItems.not()
    }

    fun isLoading(): Boolean {
        return loading?.isActive == true
    }
}
