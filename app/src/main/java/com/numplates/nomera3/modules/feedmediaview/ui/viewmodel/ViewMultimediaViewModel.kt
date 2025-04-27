package com.numplates.nomera3.modules.feedmediaview.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.numplates.nomera3.App
import com.numplates.nomera3.COMMENTS_AVAILABILITY_NOBODY
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.UnsubscribeUserUseCaseNew
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.photo.AmplitudePhotoAnalytic
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.DownloadVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetAllDownloadingMediaEventUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetFeedStateUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserParams
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.StopDownloadingVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.feedmediaview.ui.ViewMultimediaMenuItems
import com.numplates.nomera3.modules.feedmediaview.ui.content.action.ViewMultimediaAction
import com.numplates.nomera3.modules.feedmediaview.ui.content.effect.ViewMultimediaUiEffect
import com.numplates.nomera3.modules.feedmediaview.ui.content.state.ViewMultimediaScreenState
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.share.domain.usecase.CopyPostLinkUseCase
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ViewMultimediaViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    private val reactionRepository: ReactionRepository,
    private val subscribePostUseCase: SubscribePostUseCase,
    private val unsubscribePostUseCase: UnsubscribePostUseCase,
    private var getUserUidUseCase: GetUserUidUseCase,
    private val getPostUseCase: GetPostUseCase,
    private val forceUpdatePostUseCase: ForceUpdatePostUseCase,
    private val feedStateUseCase: GetFeedStateUseCase,
    private val downloadVideoToGalleryUseCase: DownloadVideoToGalleryUseCase,
    private val getAllDownloadingVideoWorkInfosUseCase: GetAllDownloadingMediaEventUseCase,
    private val cancelDownloadVideoUseCase: StopDownloadingVideoToGalleryUseCase,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val unsubscribeUserUseCase: UnsubscribeUserUseCaseNew,
    private val reactiveNotifyUpdateSubscription: ReactiveUpdateSubscribeUserUseCase,
    private val hideRoadUseCase: HidePostsOfUserUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val copyPostLinkUseCase: CopyPostLinkUseCase,
    private val setLastPostMediaViewInfoUseCase: SetLastPostMediaViewInfoUseCase,
    private val textProcessorUtil: TextProcessorUtil,
    private val application: App,
    private val amplitudePhoto: AmplitudePhotoAnalytic,
    private val featureTogglesContainer: FeatureTogglesContainer
) : ViewModel() {

    private val _viewMultimediaScreenState = MutableSharedFlow<ViewMultimediaScreenState>()
    val viewMultimediaScreenState: SharedFlow<ViewMultimediaScreenState> = _viewMultimediaScreenState

    private val _viewMultimediaUiEffect = MutableSharedFlow<ViewMultimediaUiEffect>()
    val viewMultimediaUiEffect: SharedFlow<ViewMultimediaUiEffect> = _viewMultimediaUiEffect

    private var postId: Long = -1L
    private var post: PostUIEntity? = null
    private var isVolumeEnabled: Boolean = true
    private var isVideoNeedToPlay: Boolean = true
    private var isShareOutsideOpened: Boolean = false
    private var postOrigin: DestinationOriginEnum? = null

    private var observerDisposables: CompositeDisposable = CompositeDisposable()

    init {
        observeFeedState()
        observeReactionUpdate()
        observeDownloadingVideoWorkInfos()
    }

    override fun onCleared() {
        super.onCleared()
        observerDisposables.dispose()
    }

    fun handleViewAction(action: ViewMultimediaAction) {
        when (action) {
            is ViewMultimediaAction.SetPostData -> setPostData(action.postId, action.post, action.postOrigin, action.isVolumeEnabled)
            is ViewMultimediaAction.OnSubscribeToPost -> onSubscribeToPost(action.postId)
            is ViewMultimediaAction.OnUnsubscribeFromPost -> onUnsubscribeFromPost(action.postId)
            is ViewMultimediaAction.SendAnalytic -> logAmplitude(action.post, action.where, action.actionType)
            is ViewMultimediaAction.OnRepostClick -> onRepostClicked()
            is ViewMultimediaAction.OnRepostSuccess -> onRepostSuccess(action)
            is ViewMultimediaAction.OnShareOutsideOpened -> setOnShareOutsideOpened(action.isOpened)
            is ViewMultimediaAction.OnOpenMenuClicked -> handleMenuOpen(action.mediaAsset)
            is ViewMultimediaAction.OnDownloadVideoClicked -> downloadVideo(action.postId, action.assetId)
            is ViewMultimediaAction.OnCancelDownloadClicked -> cancelDownloadVideo()
            is ViewMultimediaAction.OnSubscribeToUserClicked -> subscribeToUser(action)
            is ViewMultimediaAction.OnUnsubscribeFromUserClicked -> unsubscribeFromUser(action.userId)
            is ViewMultimediaAction.OnHideUserRoad -> hideUserRoad(action.userId)
            is ViewMultimediaAction.AddComplaintPost -> addComplaintToPost(action.postId)
            is ViewMultimediaAction.OnDeletePost -> deletePost(action.postId)
            is ViewMultimediaAction.OnCopyPostLink -> onCopyPostLinkClicked(action)
            is ViewMultimediaAction.SaveLastMediaViewInfo -> saveLastMediaViewInfo(action.mediaViewInfo)
        }
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return featureTogglesContainer
    }

    fun setPostData(postId: Long, post: PostUIEntity?, postOrigin: DestinationOriginEnum?, isVolumeEnabled: Boolean) {
        if (this.postId == -1L) {
            this.postId = postId
            this.post = post?.setFollowButtonState()
            this.isVolumeEnabled = isVolumeEnabled
            this.postOrigin = postOrigin
            viewModelScope.launch { emitFreshViewState() }
            requestPost(postId, post?.assets)
        }
    }

    fun clearPostId() {
        postId = -1L
    }

    private fun requestPost(postId: Long, assets: List<MediaAssetEntity>?, ) {
        viewModelScope.launch {
            runCatching {
                getPostUseCase.execute(
                    params = GetPostParams(postId),
                    success = { response ->
                        post = response
                            .toUiEntity(textProcessorUtil)
                            .setFollowButtonState()
                            .setLoadingStateOnFirstLoad()
                        checkUnavailableMedia(assets)
                        viewModelScope.launch { emitFreshViewState() }
                    },
                    fail = {
                        Timber.e(it)
                    }
                )
            }
        }
    }

    private fun checkUnavailableMedia(assets: List<MediaAssetEntity>?) {
        assets ?: return
        val mediaList = arrayListOf<MediaAssetEntity>()
        for (item in assets) {
            val containsItem = post?.assets?.contains(item).isTrue()
            mediaList.add(item.copy(isAvailable = containsItem))
        }
        post = post?.copy(assets = mediaList)
    }

    private fun deletePost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                deletePostUseCase.execute(
                    params = DeletePostParams(postId),
                    success = { handlePostDeleted() },
                    fail = {
                        sendErrorTryLater()
                        Timber.e("Failed to remove Video Post with id=$postId")
                    }
                )
            }.onFailure {
                sendErrorTryLater()
                Timber.e("Failed to remove Video Post with id=$postId")
            }
        }
    }

    private fun handlePostDeleted() {
        viewModelScope.launch {
            _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.OnPostDeleted)
        }
    }

    private fun subscribeToUser(event: ViewMultimediaAction.OnSubscribeToUserClicked) {
        viewModelScope.launch {
            runCatching {
                subscribeUserUseCase.invoke(event.userId)
            }.onSuccess {
                reactiveNotifyUpdateSubscription.execute(
                    UpdateSubscriptionUserParams(
                        postId = postId,
                        userId = event.userId,
                        isSubscribed = true,
                        needToHideFollowButton = true,
                        isBlocked = false
                    ),
                    {}, { error ->
                        sendErrorTryLater()
                        Timber.e(error)
                    }
                )
            }.onFailure {
                sendErrorTryLater()
            }
        }
    }

    private fun unsubscribeFromUser(userId: Long) {
        viewModelScope.launch {
            runCatching {
                unsubscribeUserUseCase.invoke(userId)
            }.onSuccess {
                reactiveNotifyUpdateSubscription.execute(
                    UpdateSubscriptionUserParams(
                        postId = postId,
                        userId = userId,
                        isSubscribed = false,
                        needToHideFollowButton = true,
                        isBlocked = false
                    ),
                    {}, { error ->
                        Timber.e(error)
                    }
                )
            }
        }
    }

    private fun hideUserRoad(userId: Long) {
        viewModelScope.launch {
            runCatching {
                hideRoadUseCase.execute(
                    params = HidePostsOfUserParams(userId),
                    success = {
                        viewModelScope.launch { _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.HiddenUserRoad) }
                    },
                    fail = {
                        sendErrorTryLater()
                        Timber.e("Fail hide user road")
                    }
                )
            }.onFailure {
                sendErrorTryLater()
            }
        }
    }

    private fun addComplaintToPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                postsRepository.addPostComplaint(
                    postId = postId,
                    success = { handleAddedPostComplaint() },
                    fail = {
                        sendErrorTryLater()
                        Timber.e("Fail add complaint POST id:$postId")
                    }
                )
            }.onFailure {
                sendErrorTryLater()
                Timber.e("Fail add complaint POST id:$postId")
            }
        }
    }

    private fun handleAddedPostComplaint() {
        viewModelScope.launch {
            _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.AddedPostComplaint)
        }
    }

    private fun downloadVideo(postId: Long, assetId: String?) {
        downloadVideoToGalleryUseCase.invoke(DownloadMediaHelper.PostMediaDownloadType.PostDetailDownload(postId, assetId))
    }

    private fun observeDownloadingVideoWorkInfos() {
        viewModelScope.launch {
            getAllDownloadingVideoWorkInfosUseCase.invoke().collect { downloadEvent ->
                val downloadStateEvent = downloadEvent as? DownloadMediaEvent.PostDownloadState ?: return@collect
                val loadingInfo = LoadingPostVideoInfoUIModel(
                    loadingState = if (downloadStateEvent.postMediaDownloadType.canShowOnPostDetail()) {
                        downloadStateEvent.state
                    } else {
                        MediaLoadingState.NONE
                    },
                    loadingTime = System.currentTimeMillis(),
                    isShowLoadingProgress = true
                )
                handleVideoLoading(downloadStateEvent.postMediaDownloadType.postId, loadingInfo)
            }
        }
    }

    private suspend fun handleVideoLoading(postId: Long, loadingInfo: LoadingPostVideoInfoUIModel) {
        if (this.postId != postId) return
        val post = this.post ?: return
        this.post = post.copy(loadingInfo = loadingInfo)
        _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.UpdateLoadingState(loadingInfo, post))
    }

    private fun onCopyPostLinkClicked(event: ViewMultimediaAction.OnCopyPostLink) {
        viewModelScope.launch {
            runCatching {
                val isSuccess = copyPostLinkUseCase.invoke(event.postId)
                if (isSuccess) {
                    _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.PostLinkCopied)
                } else {
                    sendErrorTryLater()
                }
            }.onFailure {
                sendErrorTryLater()
            }
        }
    }

    private fun saveLastMediaViewInfo(lastMediaViewInfo: PostMediaViewInfo) {
        setLastPostMediaViewInfoUseCase.invoke(lastMediaViewInfo)
    }

    private fun observeReactionUpdate() {
        reactionRepository.getCommandReactionStreamMeera()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleReactionUpdate) { Timber.e(it) }
            .addTo(observerDisposables)
    }

    private fun handleReactionUpdate(reactionUpdate: MeeraReactionUpdate) {
        viewModelScope.launch {
            when (reactionUpdate.reactionSource) {
                is MeeraReactionSource.Post -> {
                    val currentPost = this@ViewMultimediaViewModel.post ?: return@launch
                    val postUpdate = reactionUpdate.toUIPostUpdate() as UIPostUpdate.MeeraUpdateReaction
                    if (postUpdate.postId != postId) return@launch

                    val updatedPost = currentPost.copy(reactions = reactionUpdate.reactionList)
                    this@ViewMultimediaViewModel.post = updatedPost

                    _viewMultimediaUiEffect.emit(
                        ViewMultimediaUiEffect.UpdatePostReaction(
                            reactionUpdate = reactionUpdate,
                            post = updatedPost
                        )
                    )
                }
                else -> Unit
            }
        }
    }

    private fun observeFeedState() {
        feedStateUseCase
            .execute(DefParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleFeedUpdate) { Timber.e(it) }
            .addTo(observerDisposables)
    }

    private fun handleFeedUpdate(event: FeedUpdateEvent) {
        when (event) {
            is FeedUpdateEvent.FeedUserSubscriptionChanged -> {
                viewModelScope.launch {
                    val post = this@ViewMultimediaViewModel.post ?: return@launch
                    if (post.user?.userId != event.userId) return@launch
                    if (post.user.subscriptionOn.toBoolean() == event.isSubscribed) return@launch
                    post.user.subscriptionOn = event.isSubscribed.toInt()
                    if (event.isSubscribed.not()) this@ViewMultimediaViewModel.post = post.copy(needToShowFollowButton = true)
                    emitFreshViewState()
                }
            }
            is FeedUpdateEvent.FeedPostSubscriptionChanged -> {
                viewModelScope.launch {
                    val post = this@ViewMultimediaViewModel.post ?: return@launch
                    if (post.postId != event.postId) return@launch
                    if (post.isPostSubscribed == event.isSubscribed) return@launch
                    this@ViewMultimediaViewModel.post = post.copy(isPostSubscribed = event.isSubscribed)
                }
            }
            is FeedUpdateEvent.FeedUpdatePayload -> {
                viewModelScope.launch {
                    val post = this@ViewMultimediaViewModel.post ?: return@launch
                    if (post.postId != event.postId) return@launch
                    this@ViewMultimediaViewModel.post = post.copy(
                        commentCount = event.commentCount ?: post.commentCount,
                        repostCount = event.repostCount ?: post.repostCount,
                        reactions = event.reactions ?: post.reactions
                    )
                    emitFreshViewState()
                }
            }
            else -> Unit
        }
    }

    private fun onSubscribeToPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                subscribePostUseCase.execute(
                    params = SubscribePostParams(postId),
                    success = { updatePostSubscription(isPostSubscribed = true) },
                    fail = {
                        sendErrorTryLater()
                        Timber.e("Fail subscribe POST id:$postId")
                    }
                )
            }.onFailure {
                sendErrorTryLater()
                Timber.e("Fail subscribe POST id:$postId")
            }
        }
    }

    private fun onUnsubscribeFromPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                unsubscribePostUseCase.execute(
                    params = UnsubscribePostParams(postId),
                    success = { updatePostSubscription(isPostSubscribed = false) },
                    fail = {
                        sendErrorTryLater()
                        Timber.e("Fail unsubscribe POST id:$postId")
                    }
                )
            }.onFailure {
                sendErrorTryLater()
                Timber.e("Fail unsubscribe POST id:$postId")
            }
        }
    }

    private fun cancelDownloadVideo() {
        cancelDownloadVideoUseCase.invoke(postId)
    }

    private fun onRepostClicked() {
        viewModelScope.launch {
            val post = this@ViewMultimediaViewModel.post ?: return@launch
            val blacklistedMe = post.user?.blackListedMe ?: false
            val blacklistedByMe = post.user?.blackListedByMe ?: false
            _viewMultimediaUiEffect.emit(
                if (blacklistedMe || blacklistedByMe) {
                    ViewMultimediaUiEffect.ShowErrorMessage(R.string.post_hide_error_message)
                } else {
                    ViewMultimediaUiEffect.OpenShareMenu(post)
                }
            )
        }
    }

    private fun onRepostSuccess(repostAction: ViewMultimediaAction.OnRepostSuccess) {
        forceUpdatePostUseCase.execute(
            UpdatePostParams(
                FeedUpdateEvent.FeedUpdatePayload(
                    postId = repostAction.post.postId,
                    repostCount = repostAction.post.repostCount + repostAction.repostTargetCount
                )
            )
        )
    }

    private fun handleMenuOpen(mediaAsset: MediaAssetEntity) {
        val post = post ?: return
        val postUser = post.user ?: return
        val isAuthorMenu = postUser.userId == getUserUidUseCase.invoke()

        val menuEvent = if (isAuthorMenu) {
            ViewMultimediaUiEffect.OpenMenu(buildAuthorMenu(mediaAsset))
        } else {
            ViewMultimediaUiEffect.OpenMenu(buildReaderMenu(mediaAsset))
        }

        viewModelScope.launch { _viewMultimediaUiEffect.emit(menuEvent) }
    }

    private fun buildAuthorMenu(mediaAsset: MediaAssetEntity): List<ViewMultimediaMenuItems> {
        val menuList = mutableListOf<ViewMultimediaMenuItems>()
        val post = post ?: return menuList

        val isEnableVideoSaving = !mediaAsset.video.isNullOrEmpty() && application.remoteConfigs.postVideoSaving
        val isPrivateGroupPost = post.isPrivateGroupPost

        when (mediaAsset.type) {
            MEDIA_VIDEO -> {
                if (isEnableVideoSaving) menuList.add(ViewMultimediaMenuItems.DownloadVideo(post.postId, mediaAsset.id))
            }
            else -> menuList.add(ViewMultimediaMenuItems.DownloadImage(post.postId, mediaAsset.id))
        }

        if (isPrivateGroupPost.not()) {
            menuList.add(ViewMultimediaMenuItems.SharePost(post))
            menuList.add(ViewMultimediaMenuItems.CopyPostLink(post.postId))
        }

        return menuList
    }

    private fun buildReaderMenu(mediaAsset: MediaAssetEntity): List<ViewMultimediaMenuItems> {
        val menuList = mutableListOf<ViewMultimediaMenuItems>()
        val post = post ?: return menuList
        val postUser = post.user ?: return menuList

        val isEnableVideoSaving = !mediaAsset.video.isNullOrEmpty() && application.remoteConfigs.postVideoSaving
        val isSubscribedToUser = postUser.subscriptionOn.toBoolean()
        val isSubscribedToPost = post.isPostSubscribed
        val isPrivateGroupPost = post.isPrivateGroupPost

        when (mediaAsset.type) {
            MEDIA_VIDEO -> {
                if (isEnableVideoSaving) menuList.add(ViewMultimediaMenuItems.DownloadVideo(post.postId, mediaAsset.id))
            }

            else -> menuList.add(ViewMultimediaMenuItems.DownloadImage(post.postId, mediaAsset.id))
        }

        if (isSubscribedToPost) {
            menuList.add(ViewMultimediaMenuItems.UnsubscribeFromPost(post.postId))
        } else {
            menuList.add(ViewMultimediaMenuItems.SubscribeToPost(post.postId))
        }

        if (isSubscribedToUser.not()) {
            menuList.add(ViewMultimediaMenuItems.SubscribeToUser(postUser.userId))
        }

        if (isPrivateGroupPost.not()) {
            menuList.add(ViewMultimediaMenuItems.SharePost(post))
            menuList.add(ViewMultimediaMenuItems.CopyPostLink(post.postId))
        }

        menuList.add(ViewMultimediaMenuItems.AddComplaintPost(post.postId))

        return menuList
    }

    private fun setOnShareOutsideOpened(isOpened: Boolean) {
        isShareOutsideOpened = isOpened
    }

    private fun updatePostSubscription(isPostSubscribed: Boolean) {
        viewModelScope.launch {
            _viewMultimediaUiEffect.emit(
                if (isPostSubscribed) {
                    ViewMultimediaUiEffect.SubscribedToPost
                } else {
                    ViewMultimediaUiEffect.UnsubscribedFromPost
                }
            )
        }
    }

    private suspend fun emitFreshViewState() {
        _viewMultimediaScreenState.emit(getCurrentViewState())
    }

    private fun getCurrentViewState(): ViewMultimediaScreenState {
        val post = post
        val user = post?.user
        val assets = post?.assets
        val isPostUnavailable = (post?.isMultimediaPostUnavailable() ?: true).not()
        return when {
            isPostUnavailable -> ViewMultimediaScreenState.Unavailable(post)
            postId != -1L && post != null && user != null && !assets.isNullOrEmpty() -> {
                ViewMultimediaScreenState.MultimediaPostInfo(
                    post = post,
                    isVolumeEnabled = isVolumeEnabled,
                    isVideoNeedToPlay = isVideoNeedToPlay,
                    isCommentsShow = isCommentsShow(post)
                )
            }
            else -> ViewMultimediaScreenState.Loading
        }
    }

    private fun isCommentsShow(post: PostUIEntity): Boolean {
        return post.commentAvailability != COMMENTS_AVAILABILITY_NOBODY
    }

    private fun PostUIEntity.setFollowButtonState(): PostUIEntity {
        val isMyPost = isMyPost(getUserUidUseCase.invoke())
        val isSubscribed = isSubscribedToPostUser()
        val isBlockedByMe = user?.blackListedByMe ?: false
        val showFollowButton = !isMyPost && !isSubscribed && !isBlockedByMe
        return this.copy(needToShowFollowButton = showFollowButton)
    }

    private fun PostUIEntity.setLoadingStateOnFirstLoad(): PostUIEntity {
        val originalPost = post ?: return this
        return this.copy(loadingInfo = originalPost.loadingInfo)
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

    private fun sendErrorTryLater() {
        viewModelScope.launch {
            _viewMultimediaUiEffect.emit(ViewMultimediaUiEffect.ShowErrorMessage(R.string.error_try_later))
        }
    }
}
