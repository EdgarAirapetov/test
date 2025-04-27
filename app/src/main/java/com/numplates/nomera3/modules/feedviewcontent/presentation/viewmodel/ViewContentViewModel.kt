package com.numplates.nomera3.modules.feedviewcontent.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.photo.AmplitudePhotoAnalytic
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostUseCase
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentGroupUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.mapper.toContentGroupUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewevents.ViewContentEvent
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates.ViewContentMessageState
import com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates.ViewContentState
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ViewContentViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val postsRepository: PostsRepository,
    private val reactionRepository: ReactionRepository,
    private val subscribePostUseCase: SubscribePostUseCase,
    private val unsubscribePostUseCase: UnsubscribePostUseCase,
    private var getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePhoto: AmplitudePhotoAnalytic,
    private val featureTogglesContainer: FeatureTogglesContainer
) : ViewModel() {

    private val _viewContentState: MutableLiveData<ViewContentState> = SingleLiveEvent()
    private val _viewContentMessageState: MutableLiveData<ViewContentMessageState> = SingleLiveEvent()
    private val _liveReactions: MutableLiveData<UIPostUpdate> = MutableLiveData()
    private var currentGroup: ContentGroupUiModel? = null
    private val disposables = CompositeDisposable()


    init {
        initListenReaction()
    }

    fun getUid(): Long {
        return appSettings.readUID()
    }

    fun getViewContentState(): LiveData<ViewContentState> = _viewContentState

    fun getViewContentMessageState(): LiveData<ViewContentMessageState> = _viewContentMessageState

    fun getLiveReactions(): LiveData<UIPostUpdate> = _liveReactions

    fun onTriggerViewEvent(event: ViewContentEvent) {
        when (event) {
            is ViewContentEvent.GetContent -> getContentFromUrl(event.post)
            is ViewContentEvent.SubscribePost -> subscribePost(event.postId)
            is ViewContentEvent.UnsubscribePost -> unsubscribePost(event.postId)
            is ViewContentEvent.AddPostComplaint -> addPostComplaint(event.postId)
            is ViewContentEvent.SendAnalytic -> logAmplitude(event.post, event.where, event.actionType)
        }
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return featureTogglesContainer
    }

    private fun subscribePost(postId: Long?) {
        if (postId == null) return
        viewModelScope.launch {
            subscribePostUseCase.execute(
                params = SubscribePostParams(postId),
                success = { updatePostSubscription(isPostSubscribed = true) },
                fail = {
                    Timber.e("Fail subscribe POST id: $postId")
                    _viewContentMessageState.postValue(ViewContentMessageState.ShowError(R.string.error_try_later))
                }
            )
        }
    }

    private fun unsubscribePost(postId: Long?) {
        if (postId == null) return
        viewModelScope.launch {
            unsubscribePostUseCase.execute(
                params = UnsubscribePostParams(postId),
                success = { updatePostSubscription(isPostSubscribed = false) },
                fail = {
                    Timber.e("Fail unsubscribe POST id: $postId")
                    _viewContentMessageState.postValue(ViewContentMessageState.ShowError(R.string.error_try_later))
                }
            )
        }
    }

    private fun addPostComplaint(postId: Long?) {
        if (postId == null) return
        viewModelScope.launch {
            postsRepository.addPostComplaint(
                postId = postId,
                success = {
                    _viewContentMessageState.postValue(
                        ViewContentMessageState.ShowSuccess(R.string.road_complaint_send_success)
                    )
                },
                fail = {
                    Timber.e("Fail complaint POST id: $postId")
                    _viewContentMessageState.postValue(
                        ViewContentMessageState.ShowError(R.string.error_try_later)
                    )
                }
            )
        }
    }

    private fun updatePostSubscription(isPostSubscribed: Boolean) {
        val newGroup = currentGroup?.copy(isPostSubscribed = isPostSubscribed) ?: return
        currentGroup = newGroup
        val updateSubscriptionEvent = ViewContentState.PostSubscriptionUpdated(newGroup)
        _viewContentState.postValue(updateSubscriptionEvent)
        _viewContentMessageState.postValue(
            ViewContentMessageState.ShowSuccess(
                if (isPostSubscribed) R.string.subscribe_post else R.string.unsubscribe_post
            )
        )
    }

    private fun getContentFromUrl(post: PostUIEntity?) {
        val contentGroup = post?.toContentGroupUiModel() ?: return
        currentGroup = contentGroup
        val updateEvent = ViewContentState.ContentDataReceived(contentGroup)
        _viewContentState.postValue(updateEvent)
    }

    private fun initListenReaction() {
        val disposable = reactionRepository
            .getCommandReactionStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reactionUpdate ->
                proceedReaction(reactionUpdate)
            }

        disposables.add(disposable)
    }

    private fun proceedReaction(reactionUpdate: ReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is ReactionSource.Post -> {
                updatePostReaction(reactionUpdate)
            }
            else -> Unit
        }
    }

    private fun updatePostReaction(reactionUpdate: ReactionUpdate) {
        _liveReactions.postValue(reactionUpdate.toUIPostUpdate())
    }

    private fun logAmplitude(
        post: PostUIEntity?,
        where: AmplitudePropertyWhere,
        actionType: AmplitudePropertyMenuAction?
    ) {
        amplitudePhoto.logPhotoScreenOpen(
            userId = getUserUidUseCase.invoke(),
            authorId = post?.user?.userId ?: 0,
            where = where,
            actionType = actionType
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
