package com.numplates.nomera3.modules.viewvideo.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.message.UniquenameSpanData
import com.numplates.nomera3.App
import com.numplates.nomera3.COMMENTS_AVAILABILITY_FRIENDS
import com.numplates.nomera3.COMMENTS_AVAILABILITY_NOBODY
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.UnsubscribeUserUseCaseNew
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost
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
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.share.domain.usecase.CopyPostLinkUseCase
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.viewvideo.presentation.events.ViewVideoItemUIEvent
import com.numplates.nomera3.modules.viewvideo.presentation.events.ViewVideoItemUserEvent
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ViewVideoItemViewModel @Inject constructor(
    private val reactionRepository: ReactionRepository,
    private val postsRepository: PostsRepository,
    private val getPostUseCase: GetPostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val unsubscribeUserUseCase: UnsubscribeUserUseCaseNew,
    private val subscribePostUseCase: SubscribePostUseCase,
    private val unsubscribePostUseCase: UnsubscribePostUseCase,
    private val hideRoadUseCase: HidePostsOfUserUseCase,
    private val copyPostLinkUseCase: CopyPostLinkUseCase,
    private val checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val reactiveNotifyUpdateSubscription: ReactiveUpdateSubscribeUserUseCase,
    private val forceUpdatePostUseCase: ForceUpdatePostUseCase,
    private val downloadVideoToGalleryUseCase: DownloadVideoToGalleryUseCase,
    private val cancelDownloadVideoUseCase: StopDownloadingVideoToGalleryUseCase,
    private val getAllDownloadingVideoWorkInfosUseCase: GetAllDownloadingMediaEventUseCase,
    private val feedStateUseCase: GetFeedStateUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val application: App,
    private val textProcessorUtil: TextProcessorUtil,
    private val analyticsInteractor: AnalyticsInteractor,
    private var setLastPostMediaViewInfoUseCase: SetLastPostMediaViewInfoUseCase
) : ViewModel() {

    private val _videoScreenState: MutableLiveData<VideoScreenState> = MutableLiveData()
    val videoScreenState: LiveData<VideoScreenState> = _videoScreenState

    private val _videoScreenUIEvents: SingleLiveEvent<ViewVideoItemUIEvent> = SingleLiveEvent()
    val videoScreenUIEvents: LiveData<ViewVideoItemUIEvent> = _videoScreenUIEvents

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
        observeFriendStatusChanged()
    }

    override fun onCleared() {
        super.onCleared()
        observerDisposables.dispose()
    }

    fun saveLastVideoPlaybackPosition(lastVideoPlaybackPosition: Long?) {
        setLastPostMediaViewInfoUseCase.invoke(PostMediaViewInfo(lastVideoPlaybackPosition = lastVideoPlaybackPosition))
    }

    fun toggleIsVideoStatePlaying() {
        isVideoNeedToPlay = !isVideoNeedToPlay
        emitFreshViewState()
    }

    fun setVideoPostData(postId: Long, post: PostUIEntity?, postOrigin: DestinationOriginEnum?, isVolumeEnabled: Boolean) {
        if (this.postId == -1L) {
            this.postId = postId
            this.post = post?.setFollowButtonState()
            this.isVolumeEnabled = isVolumeEnabled
            this.postOrigin = postOrigin
            emitFreshViewState()
            requestVideoPost(postId)
        }
    }

    fun onUserEvent(event: ViewVideoItemUserEvent) {
        when (event) {
            is ViewVideoItemUserEvent.OnSubscribeToUserClicked -> subscribeToUser(event)
            is ViewVideoItemUserEvent.OnUnsubscribeFromUserClicked -> unsubscribeFromUser(event.userId)
            is ViewVideoItemUserEvent.OnBadWordClicked -> handleBadWordClicked(event.click)
            is ViewVideoItemUserEvent.OnOpenMenuClicked -> handleMenuOpen()
            is ViewVideoItemUserEvent.OnDownloadVideoClicked -> downloadVideo(event.postId, event.assetId)
            is ViewVideoItemUserEvent.OnCancelDownloadClicked -> cancelDownloadVideo()
            is ViewVideoItemUserEvent.OnDeletePost -> deletePost(event.postId)
            is ViewVideoItemUserEvent.OnRepostClick -> onRepostClicked()
            is ViewVideoItemUserEvent.OnCopyPostLink -> onCopyPostLinkClicked(event)
            is ViewVideoItemUserEvent.OnRepostSuccess -> onRepostSuccess(event)
            is ViewVideoItemUserEvent.OnSubscribeToPost -> onSubscribeToPost(event.postId)
            is ViewVideoItemUserEvent.OnUnsubscribeFromPost -> onUnsubscribeFromPost(event.postId)
            is ViewVideoItemUserEvent.AddComplaintPost -> addComplaintToPost(event.postId)
            is ViewVideoItemUserEvent.OnHideUserRoad -> hideUserRoad(event.userId)
            is ViewVideoItemUserEvent.OnShareOutsideOpened -> setOnShareOutsideOpened(event.isOpened)
            is ViewVideoItemUserEvent.OnPauseFragment -> handlePauseFragment()
        }
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer = featureTogglesContainer

    fun clearPostId() {
        postId = -1L
    }

    private fun requestVideoPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                getPostUseCase.execute(
                    params = GetPostParams(postId),
                    success = { response ->
                        post = response
                            .toUiEntity(textProcessorUtil)
                            .setFollowButtonState()
                            .setLoadingStateOnFirstLoad()
                        emitFreshViewState()
                    },
                    fail = {
                        Timber.e(it)
                    }
                )
            }
        }
    }

    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach(::handleFriendStatusChanged)
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(effect: UserSettingsEffect) {
        when (effect) {
            is UserSettingsEffect.UserFriendStatusChanged -> {
                updateUserFriendStatus(effect)
            }
            is UserSettingsEffect.UserBlockStatusChanged -> {
                updateUserBlockStatus(effect)
            }
            else -> Unit
        }
    }

    private fun updateUserFriendStatus(effect: UserSettingsEffect.UserFriendStatusChanged) {
        var post = post ?: return
        if (effect.userId != post.getUserId()) return
        val user = post.user?.copy(subscriptionOn = effect.isSubscribe.toInt())
        post = post.copy(user = user)
        if (post.commentAvailability == COMMENTS_AVAILABILITY_FRIENDS && post.isCommunityPost().not()) {
            post = post.copy(isAllowedToComment = effect.isSubscribe)
        }

        _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.UpdatePostInfo(post))

        this.post = post
    }

    private fun updateUserBlockStatus(effect: UserSettingsEffect.UserBlockStatusChanged) {
        var post = post ?: return
        if (effect.userId != post.getUserId()) return
        val user = post.user?.copy(blackListedByMe = effect.isUserBlocked)
        post = post.copy(user = user)

        _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.UpdatePostInfo(post))

        this.post = post
    }

    private fun subscribeToUser(event: ViewVideoItemUserEvent.OnSubscribeToUserClicked) {
        viewModelScope.launch {
            runCatching {
                if (event.fromMenu) logPostMenuAction(AmplitudePropertyMenuAction.USER_FOLLOW)
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

    private fun deletePost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                logPostMenuAction(AmplitudePropertyMenuAction.DELETE)
                deletePostUseCase.execute(
                    params = DeletePostParams(postId),
                    success = { _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.OnPostDeleted) },
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

    private fun onRepostClicked() {
        val post = this.post ?: return
        val blacklistedMe = post.user?.blackListedMe ?: false
        val blacklistedByMe = post.user?.blackListedByMe ?: false

        if (blacklistedMe || blacklistedByMe) {
            _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.ShowErrorMessage(R.string.post_hide_error_message))
        } else {
            _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.OpenShareMenu(post))
        }
    }

    private fun onCopyPostLinkClicked(event: ViewVideoItemUserEvent.OnCopyPostLink) {
        viewModelScope.launch {
            runCatching {
                val isSuccess = copyPostLinkUseCase.invoke(event.postId)
                if (isSuccess) {
                    _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.PostLinkCopied)
                } else {
                    sendErrorTryLater()
                }
            }.onFailure {
                sendErrorTryLater()
            }
        }
    }

    private fun setOnShareOutsideOpened(isOpened: Boolean) {
        isShareOutsideOpened = isOpened
    }

    private fun handlePauseFragment() {
        if (!isShareOutsideOpened) _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.OnFragmentPaused)
    }

    private fun onRepostSuccess(repostEvent: ViewVideoItemUserEvent.OnRepostSuccess) {
        forceUpdatePostUseCase.execute(
            UpdatePostParams(
                FeedUpdateEvent.FeedUpdatePayload(
                    postId = repostEvent.post.postId,
                    repostCount = repostEvent.post.repostCount + repostEvent.repostTargetCount
                )
            )
        )
    }

    private fun onSubscribeToPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                logPostMenuAction(AmplitudePropertyMenuAction.POST_FOLLOW)
                subscribePostUseCase.execute(
                    params = SubscribePostParams(postId),
                    success = { _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.SubscribedToPost) },
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
                logPostMenuAction(AmplitudePropertyMenuAction.POST_UNFOLLOW)
                unsubscribePostUseCase.execute(
                    params = UnsubscribePostParams(postId),
                    success = { _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.UnsubscribedFromPost) },
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

    private fun addComplaintToPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                logPostMenuAction(AmplitudePropertyMenuAction.POST_REPORT)
                postsRepository.addPostComplaint(
                    postId = postId,
                    success = { _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.AddedPostComplaint) },
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

    private fun hideUserRoad(userId: Long) {
        viewModelScope.launch {
            runCatching {
                logPostMenuAction(AmplitudePropertyMenuAction.HIDE_USER_POSTS)
                hideRoadUseCase.execute(
                    params = HidePostsOfUserParams(userId),
                    success = {
                        Timber.e("Success hide user road")
                        _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.HiddenUserRoad)
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

    private fun cancelDownloadVideo() {
        cancelDownloadVideoUseCase.invoke(postId)
    }

    private fun downloadVideo(postId: Long, assetId: String?) {
        logPostMenuAction(AmplitudePropertyMenuAction.SAVE)
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

    private fun handleVideoLoading(postId: Long, loadingInfo: LoadingPostVideoInfoUIModel) {
        if (this.postId != postId) return
        val post = this.post ?: return
        this.post = post.copy(loadingInfo = loadingInfo)
        _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.UpdateLoadingState(loadingInfo, post))
    }

    private fun handleBadWordClicked(click: SpanDataClickType.ClickBadWord) {
        val originalText = post?.tagSpan?.text ?: ""
        val badWord = click.badWord ?: ""
        val postText = originalText.replaceRange(click.startIndex, click.endIndex, badWord)
        val newSpan = mutableListOf<UniquenameSpanData>()
        post?.tagSpan?.spanData?.let { newSpan.addAll(it.filter { data -> click.tagSpanId != data.id }) }
        val spanData = UniquenameSpanData(
            id = null,
            tag = null,
            type = UniquenameType.FONT_STYLE_ITALIC.value,
            startSpanPos = click.startIndex,
            endSpanPos = click.startIndex + badWord.length,
            userId = null,
            groupId = null,
            symbol = badWord
        )
        newSpan.add(spanData)
        val updatedPost = post?.copy(
            postText = postText,
            tagSpan = post?.tagSpan?.copy(
                text = postText,
                spanData = newSpan
            )
        )
        this.post = updatedPost
        emitFreshViewState()
    }

    private fun logPostMenuAction(action: AmplitudePropertyMenuAction) {
        val post = post ?: return
        val postUser = post.user ?: return
        val isPostAuthor = postUser.userId == getUserUidUseCase.invoke()

        val whosePost = if (isPostAuthor) {
            AmplitudePropertyWhosePost.MY
        } else {
            AmplitudePropertyWhosePost.USER
        }
        analyticsInteractor.logPostMenuAction(
            actionType = action,
            authorId = postUser.userId,
            where = AmplitudePropertyWhere.POST,
            whosePost = whosePost,
            whence = postOrigin.toAmplitudePropertyWhence(),
            recFeed = checkMainFilterRecommendedUseCase.invoke(),
            saveType = AmplitudePropertySaveType.VIDEO
        )
    }


    private fun handleMenuOpen() {
        val post = post ?: return
        val postUser = post.user ?: return
        val isAuthorMenu = postUser.userId == getUserUidUseCase.invoke()

        val menuEvent = if (isAuthorMenu) {
            ViewVideoItemUIEvent.OpenMenu(buildAuthorMenu())
        } else {
            ViewVideoItemUIEvent.OpenMenu(buildReaderMenu())
        }

        _videoScreenUIEvents.postValue(menuEvent)
    }

    private fun buildAuthorMenu(): List<ViewVideoMenuItems> {
        val menuList = mutableListOf<ViewVideoMenuItems>()
        val post = post ?: return menuList

        val isEnableVideoSaving = post.getVideoUrl() != null && application.remoteConfigs.postVideoSaving
        val isPrivateGroupPost = post.isPrivateGroupPost

        if (isEnableVideoSaving) menuList.add(ViewVideoMenuItems.DownloadVideo(post.postId))

        if (isPrivateGroupPost.not()) {
            menuList.add(ViewVideoMenuItems.SharePost(post))
            menuList.add(ViewVideoMenuItems.CopyPostLink(post.postId))
        }

        return menuList
    }

    private fun buildReaderMenu(): List<ViewVideoMenuItems> {
        val menuList = mutableListOf<ViewVideoMenuItems>()
        val post = post ?: return menuList
        val postUser = post.user ?: return menuList

        val isEnableVideoSaving = post.getVideoUrl() != null && application.remoteConfigs.postVideoSaving
        val isSubscribedToUser = postUser.subscriptionOn.toBoolean()
        val isSubscribedToPost = post.isPostSubscribed
        val isPrivateGroupPost = post.isPrivateGroupPost

        if (isEnableVideoSaving) menuList.add(ViewVideoMenuItems.DownloadVideo(post.postId))

        if (isSubscribedToPost) {
            menuList.add(ViewVideoMenuItems.UnsubscribeFromPost(post.postId))
        } else {
            menuList.add(ViewVideoMenuItems.SubscribeToPost(post.postId))
        }

        if (isSubscribedToUser.not()) {
            menuList.add(ViewVideoMenuItems.SubscribeToUser(postUser.userId))
        }

        if (isPrivateGroupPost.not()) {
            menuList.add(ViewVideoMenuItems.SharePost(post))
            menuList.add(ViewVideoMenuItems.CopyPostLink(post.postId))
        }

        menuList.add(ViewVideoMenuItems.AddComplaintPost(post.postId))

        return menuList
    }

    private fun emitFreshViewState() {
        _videoScreenState.postValue(getCurrentViewState())
    }

    private fun getCurrentViewState(): VideoScreenState {
        val post = post
        val videoUrl = post?.getVideoUrl()
        val user = post?.user
        val isVideoUnavailable = (post?.isPostVideoAvailable() ?: true).not()
        return when {
            isVideoUnavailable -> VideoScreenState.Unavailable(post)
            postId != -1L && post != null && videoUrl != null && user != null -> {
                VideoScreenState.VideoInfo(
                    postId = postId,
                    post = post,
                    videoUrl = videoUrl,
                    videoAspect = post.getSingleAspect(),
                    isVolumeEnabled = isVolumeEnabled,
                    isVideoNeedToPlay = isVideoNeedToPlay,
                    isCommentsShow = isCommentsShow(post)
                )
            }
            else -> VideoScreenState.Loading
        }
    }

    private fun isCommentsShow(post: PostUIEntity): Boolean {
        return post.commentAvailability != COMMENTS_AVAILABILITY_NOBODY
    }

    private fun observeFeedState() {
        feedStateUseCase
            .execute(DefParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleFeedUpdate) { Timber.e(it) }
            .addTo(observerDisposables)
    }

    private fun observeReactionUpdate() {
        reactionRepository.getCommandReactionStreamMeera()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleReactionUpdate) { Timber.e(it) }
            .addTo(observerDisposables)
    }

    private fun handleReactionUpdate(reactionUpdate: MeeraReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is MeeraReactionSource.Post -> {
                val currentPost = this.post ?: return
                val postUpdate = reactionUpdate.toUIPostUpdate() as UIPostUpdate.MeeraUpdateReaction
                if (postUpdate.postId != postId) return

                val updatedPost = currentPost.copy(reactions = reactionUpdate.reactionList)
                this.post = updatedPost

                _videoScreenUIEvents.postValue(
                    ViewVideoItemUIEvent.UpdateVideoReaction(
                        reactionUpdate = reactionUpdate,
                        post = updatedPost
                    )
                )
            }
            else -> Unit
        }
    }

    private fun handleFeedUpdate(event: FeedUpdateEvent) {
        when (event) {
            is FeedUpdateEvent.FeedUserSubscriptionChanged -> {
                val post = this.post ?: return
                if (post.user?.userId != event.userId) return
                if (post.user.subscriptionOn.toBoolean() == event.isSubscribed) return
                post.user.subscriptionOn = event.isSubscribed.toInt()
                if (event.isSubscribed.not()) this.post = post.copy(needToShowFollowButton = true)
                emitFreshViewState()
            }
            is FeedUpdateEvent.FeedPostSubscriptionChanged -> {
                val post = this.post ?: return
                if (post.postId != event.postId) return
                if (post.isPostSubscribed == event.isSubscribed) return
                this.post = post.copy(isPostSubscribed = event.isSubscribed)
            }
            is FeedUpdateEvent.FeedUpdatePayload -> {
                val post = this.post ?: return
                if (post.postId != event.postId) return
                this.post = post.copy(
                    commentCount = event.commentCount ?: post.commentCount,
                    repostCount = event.repostCount ?: post.repostCount,
                    reactions = event.reactions ?: post.reactions
                )
                emitFreshViewState()
            }
            else -> Unit
        }
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

    private fun sendErrorTryLater() {
        _videoScreenUIEvents.postValue(ViewVideoItemUIEvent.ShowErrorMessage(R.string.error_try_later))
    }

}

sealed class VideoScreenState {
    object Loading : VideoScreenState()
    data class Unavailable(val post: PostUIEntity?) : VideoScreenState()
    data class VideoInfo(
        val postId: Long,
        val post: PostUIEntity,
        val videoUrl: String,
        val videoAspect: Double?,
        val isVolumeEnabled: Boolean,
        val isVideoNeedToPlay: Boolean,
        val isCommentsShow: Boolean
    ) : VideoScreenState()
}
