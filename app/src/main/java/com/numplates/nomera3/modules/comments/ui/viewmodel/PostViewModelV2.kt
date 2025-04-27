package com.numplates.nomera3.modules.comments.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.meera.core.di.modules.APPLICATION_COROUTINE_SCOPE
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toInt
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.message.UniquenameSpanData
import com.numplates.nomera3.App
import com.numplates.nomera3.COMMENTS_AVAILABILITY_FRIENDS
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.AddPostCommentUseCase
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.GetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.domain.interactornew.SetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.toAmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDeleteWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGetThereWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsWantToGoWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyPostWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.comments.data.repository.SendCommentError
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.comments.domain.usecase.ComplainCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.ComplainCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.DeletePostCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.DeletePostCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.GetLastCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.GetLastCommentsUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.SendCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.SendCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.ToGetCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.ToGetCommentsUseCase
import com.numplates.nomera3.modules.comments.ui.entity.BirthdayInputState
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.PostDetailsMode
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.comments.ui.util.checkHasCommentIntersection
import com.numplates.nomera3.modules.devtools_bridge.domain.GetPostViewCollisionHighlightEnableUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostUpdateAvailability
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.DownloadVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetAllDownloadingMediaEventUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetFeedStateUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetNewPostStreamUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserParams
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.StopDownloadingVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.usecase.JoinEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.LeaveEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.ObserveEventParticipationChangesUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetEventPostUseCase
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.maps.ui.mapper.MapAnalyticsMapperImpl
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadDATA
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkPostCommentParams
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewStatisticRepository
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewsDetectViewModel
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.domain.usecase.ObserveReactionPostUpdatesUseCase
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkParams
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkUseCase
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.volume.domain.GetVolumeStateUseCase
import com.numplates.nomera3.modules.volume.domain.SetVolumeStateUseCase
import com.numplates.nomera3.modules.volume.domain.SubscribeVolumeEventsUseCase
import com.numplates.nomera3.modules.volume.domain.model.VolumeRepositoryEvent
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.exception.Failure
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent.CancelDeleteComment
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent.DeleteComment
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent.ErrorPostComment
import com.numplates.nomera3.presentation.viewmodel.viewevents.PostViewEvent.MarkCommentForDeletion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Named

const val COMMENT_NOT_FOUND = "4921"
const val DELAY_SCROLL_TO_COMMENT = 200
private const val SINGLE_DELETED_COMMENT = 1
private const val EDIT_POST_AVAILABLE = 1

class PostViewModelV2 : BaseViewModel(), IPostViewsDetectViewModel {

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var audioFeedHelper: AudioFeedHelper

    @Inject
    lateinit var repository: PostsRepository

    @Inject
    lateinit var getPost: GetPostUseCase

    @Inject
    lateinit var addPostComment: AddPostCommentUseCase

    @Inject
    lateinit var myTracker: ITrackerActions

    @Inject
    lateinit var markNotificationAsRead: MarkAsReadDATA

    @Inject
    lateinit var blockUser: BlockStatusUseCase

    @Inject
    lateinit var complainComment: ComplainCommentUseCase

    @Inject
    lateinit var toGetCommentUseCase: ToGetCommentsUseCase

    @Inject
    lateinit var sendCommentUseCase: SendCommentUseCase

    @Inject
    lateinit var deletePostCommentUseCase: DeletePostCommentUseCase

    @Inject
    lateinit var deletePostUseCase: DeletePostUseCase

    @Inject
    lateinit var getLastCommentsUseCase: GetLastCommentsUseCase

    @Inject
    lateinit var analyticsInteractor: AnalyticsInteractor

    @Inject
    lateinit var amplitudeReactions: AmplitudeReactions

    @Inject
    lateinit var amplitudeComments: AmplitudeCommentsAnalytics

    @Inject
    lateinit var amplitudeFollowButton: AmplitudeFollowButton

    @Inject
    lateinit var reactionRepository: ReactionRepository

    @Inject
    lateinit var postViewStatisticRepository: PostViewStatisticRepository

    @Inject
    lateinit var getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var getPostViewCollisionHighlightUseCaseUseCase: GetPostViewCollisionHighlightEnableUseCase

    @Inject
    lateinit var forceUpdatePostUseCase: ForceUpdatePostUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var birthdayUtils: UserBirthdayUtils

    @Inject
    lateinit var textBirthdayUtil: BirthdayTextUtil

    @Inject
    lateinit var hideRoadUseCase: HidePostsOfUserUseCase

    @Inject
    lateinit var updateReactiveSubscription: ReactiveUpdateSubscribeUserUseCase

    @Inject
    lateinit var updateReactivePostSubscription: ReactiveUpdateSubscribePostUseCase

    @Inject
    lateinit var getFeedStateUseCase: GetFeedStateUseCase

    @Inject
    lateinit var textProcessorUtil: TextProcessorUtil

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var unsubscribePost: UnsubscribePostUseCase

    @Inject
    lateinit var subscribePostUseCase: SubscribePostUseCase

    @Inject
    lateinit var downloadVideoToGalleryUseCase: DownloadVideoToGalleryUseCase

    @Inject
    lateinit var getAllDownloadingVideoWorkInfosUseCase: GetAllDownloadingMediaEventUseCase

    @Inject
    lateinit var stopDownloadingVideoToGalleryUseCase: StopDownloadingVideoToGalleryUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase

    @Inject
    lateinit var getPostLinkUseCase: GetPostLinkUseCase

    @Inject
    lateinit var checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase

    @Inject
    lateinit var fbAnflytic: FireBaseAnalytics

    @Inject
    @Named(APPLICATION_COROUTINE_SCOPE)
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var _featureTogglesContainer: FeatureTogglesContainer

    @Inject
    lateinit var joinEventUseCase: JoinEventUseCase

    @Inject
    lateinit var leaveEventUseCase: LeaveEventUseCase

    @Inject
    lateinit var getEventPostUseCase: GetEventPostUseCase

    @Inject
    lateinit var observeEventParticipationChangesUseCase: ObserveEventParticipationChangesUseCase

    @Inject
    lateinit var mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor

    @Inject
    lateinit var mapAnalyticsMapperImpl: MapAnalyticsMapperImpl

    @Inject
    lateinit var mapAnalyticsInteractor: MapAnalyticsInteractor

    @Inject
    lateinit var observeReactionPostUpdatesUseCase: ObserveReactionPostUpdatesUseCase

    @Inject
    lateinit var subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase

    @Inject
    lateinit var amplitudeProfile: AmplitudeProfile

    @Inject
    lateinit var eventsCommonUiMapper: EventsCommonUiMapper

    @Inject
    lateinit var eventLabelUiMapper: EventLabelUiMapper

    @Inject
    lateinit var checkPostUpdateAvailability: CheckPostUpdateAvailability

    @Inject
    lateinit var getNewPostStreamUseCase: GetNewPostStreamUseCase

    @Inject
    lateinit var uploadRepository: UploadRepository

    @Inject
    lateinit var getLastPostMediaViewInfoUseCase: GetLastPostMediaViewInfoUseCase

    @Inject
    lateinit var setLastPostMediaViewInfoUseCase: SetLastPostMediaViewInfoUseCase

    @Inject
    lateinit var getVolumeStateUseCase: GetVolumeStateUseCase

    @Inject
    lateinit var setVolumeStateUseCase: SetVolumeStateUseCase

    @Inject
    lateinit var subscribeVolumeEventsUseCase: SubscribeVolumeEventsUseCase

    val isEventsOnMapEnabled: Boolean

    var postDetailsMode: PostDetailsMode = PostDetailsMode.DEFAULT
    private var snippetState: SnippetState? = null
    private var isUserDeletedOwnPost: Boolean = false

    private var settings: Settings? = null

    private var postId: Long = -1

    private var originEnum: DestinationOriginEnum? = null

    private var commentList = mutableListOf<CommentUIType>()

    val commentObserver: (MutableList<CommentUIType>) -> Unit = {
        commentList = it
    }

    val livePostViewEvent: MutableLiveData<PostViewEvent> = MutableLiveData()
    var loadingStatusLive: MutableLiveData<NetworkState.Status> = MutableLiveData()

    private val _livePostState: MutableLiveData<List<PostUIEntity>> = MutableLiveData()
    val livePostState = _livePostState.distinctUntilChanged() as LiveData<List<PostUIEntity>>

    val liveComments = MutableLiveData<CommentChunk>()
    private val _birthdayRangesLiveData = MutableLiveData<BirthdayInputState>()
    val birthdayRangesLiveData: LiveData<BirthdayInputState> = _birthdayRangesLiveData
    var isPostCommentable = true

    private val disposables = CompositeDisposable()

    private var isBlockedMe = false

    val paginationHelper = PaginationHelper()
    private val toBeDeletedComments = CopyOnWriteArraySet<ToBeDeletedCommentEntity>()

    val mapper =
        CommentsEntityResponseMapper(paginationHelper = paginationHelper, toBeDeletedComments = toBeDeletedComments)
    private var post: PostUIEntity? = null

    private var isPostInSnippetSetupDone = false

    override fun detectPostView(postViewDetectModel: PostCollisionDetector.PostViewDetectModel) {
        viewModelScope.launch {
            postViewStatisticRepository.detectPostView(postViewDetectModel)
        }
    }

    override fun uploadPostViews() {
        postViewStatisticRepository.tryUploadPostViews()
    }

    init {
        App.component.inject(this)
        isEventsOnMapEnabled = _featureTogglesContainer.mapEventsFeatureToggle.isEnabled
    }

    fun init(
        postId: Long?,
        post: PostUIEntity? = null,
        scrollCommentId: Long? = null,
        originEnum: DestinationOriginEnum?,
        postDetailsMode: PostDetailsMode = PostDetailsMode.DEFAULT,
        needToUpdate: Boolean = false
    ) {
        this.postDetailsMode = postDetailsMode
        this.postId = postId!!
        this.post = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            post?.setFollowButtonStateOnFirstLoad()
        } else {
            post?.copy(needToShowFollowButton = false)
        }
        this.originEnum = originEnum
        settings = getAppInfoAsyncUseCase.executeBlocking()
        initListenReaction()
        observeFeedState()
        observeFriendStatusChanged()
        observeDownloadMediaEvent()
        observeVolumeEvents()
        observeEditPostEvents()
        observeEventParticipationChanges()
        observeMomentsEvents()
        this.post?.let {
            renderPost(listOf(it, PostUIEntity(feedType = FeedType.PROGRESS)))
        } ?: kotlin.run {
            renderPost(listOf(PostUIEntity(feedType = FeedType.SHIMMER_PLACEHOLDER)))
        }
        if (postDetailsMode != PostDetailsMode.EVENT_SNIPPET || needToUpdate) {
            requestPostFromNetwork(scrollCommentId)
        }
    }

    fun getLastPostMediaViewInfo(): PostMediaViewInfo? = getLastPostMediaViewInfoUseCase.invoke()

    private fun observeEditPostEvents() {
        getNewPostStreamUseCase.invoke().subscribe({
            when (it) {
                is PostActionModel.PostEditingStartModel -> postEditingStart(it.postId)
                is PostActionModel.PostEditingCompleteModel -> postEditingEnd(it)
                is PostActionModel.PostEditingAbortModel -> postEditingAbort(it.postId)
                else -> Unit
            }
        }, {
            Timber.e(it)
        })?.addDisposable()
    }

    private fun postEditingStart(postId: Long) {
        val changingPost = post?: return
        if(changingPost.postId != postId) return

        val loadingInfo = LoadingPostVideoInfoUIModel(
            loadingState = MediaLoadingState.LOADING_NO_CANCEL_BUTTON,
            loadingTime = System.currentTimeMillis()
        )
        updatePost(changingPost.copy(postUpdatingLoadingInfo = loadingInfo))
    }

    private fun postEditingEnd(model: PostActionModel.PostEditingCompleteModel) {
        val changingPost = model.post.toUiEntity(
            textProcessorUtil = textProcessorUtil,
            isInSnippet = false
        )
        livePostViewEvent.postValue(PostViewEvent.PostEditedEvent(changingPost))
        updatePost(changingPost)
    }

    private fun postEditingAbort(postId: Long) {
        val changingPost = post?: return
        if (changingPost.postId != postId) return
        updatePost(changingPost.copy(postUpdatingLoadingInfo = LoadingPostVideoInfoUIModel()))
    }

    fun updatePost(post: PostUIEntity) {
        val processedPost = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            post.setFollowButtonStateOnFirstLoad()
        } else {
            post.copy(needToShowFollowButton = false)
        }
        processedPost.tagSpan?.showFullText = snippetState == null
            || snippetState == SnippetState.Expanded
            || processedPost.tagSpan?.shortText == null
        this.post = processedPost
        renderPost(listOf(processedPost))
    }

    fun isUserDeletedOwnPost(): Boolean = isUserDeletedOwnPost

    fun updatePostSelectedMediaPosition(selectedMediaPosition: Int) {
        val posts = getActualPosts()
        if (posts.isEmpty()) return
        post?.let { post ->
            posts[0] = post.copy(selectedMediaPosition = selectedMediaPosition)
        }
        post = posts[0]
    }

    fun setVolumeState(volumeState: VolumeState) {
        setVolumeStateUseCase.invoke(volumeState)
    }

    fun getVolumeState() = getVolumeStateUseCase.invoke()

    fun logScreenForFragment(isFromGroup: Boolean) {
        fbAnflytic.logScreenForFragment(
            if (isFromGroup) "GroupPostV2"
            else "PostFragmentV2"
        )
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun logPostMenuAction(
        action: AmplitudePropertyMenuAction,
        authorId: Long?,
        saveType: AmplitudePropertySaveType = AmplitudePropertySaveType.NONE
    ) {
        authorId ?: return
        val whosePost = if (getUserUidUseCase.invoke() == authorId) {
            AmplitudePropertyWhosePost.MY
        } else {
            AmplitudePropertyWhosePost.USER
        }
        val where = if (post?.isEvent().isTrue()) {
            AmplitudePropertyWhere.MAP_EVENT
        } else {
            AmplitudePropertyWhere.POST
        }
        val whence = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            AmplitudePropertyWhence.MAP
        } else {
            originEnum.toAmplitudePropertyWhence()
        }
        analyticsInteractor.logPostMenuAction(
            actionType = action,
            authorId = authorId,
            where = where,
            whosePost = whosePost,
            whence = whence,
            saveType = saveType,
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun logCommentMenuAction(action: AmplitudePropertyCommentMenuAction) {
        amplitudeComments.logCommentMenuAction(
            action = action,
            where = AmplitudePropertyCommentWhere.POST,
            whence = originEnum.toAmplitudePropertyWhence()
        )
    }

    fun logStatisticReactionsTap(where: AmplitudePropertyReactionWhere) {
        amplitudeReactions.statisticReactionsTap(
            where = where,
            whence = originEnum.toAmplitudePropertyWhence(),
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun getWhereOrigin(post: PostUIEntity): AmplitudePropertyWhere {
      return when {
            post.isEvent() && postDetailsMode == PostDetailsMode.EVENT_SNIPPET -> AmplitudePropertyWhere.EVENT_SNIPPET
            post.hasPostVideo() -> AmplitudePropertyWhere.VIDEO_POST
            else -> AmplitudePropertyWhere.OTHER
        }
    }

    fun logPressHashTag(
        post: PostUIEntity?,
    ) {
        val postId = post?.postId ?: return
        val authorId = post.getUserId() ?: return
        analyticsInteractor.logHashTagPress(
            originEnum.toAmplitudePropertyWhere(),
            postId,
            authorId
        )
    }

    fun setMapEventSnippetCloseMethod(closeMethod: MapSnippetCloseMethod) {
        mapAnalyticsInteractor.setMapSnippetCloseMethod(closeMethod)
    }

    fun setSnippetState(snippetState: SnippetState) {
        this.snippetState = snippetState
        post?.let { postUIEntity ->
            renderPost(listOf(postUIEntity))
        }
        if (snippetState == SnippetState.Expanded && isPostInSnippetSetupDone.not()) {
            isPostInSnippetSetupDone = true
            livePostViewEvent.postValue(PostViewEvent.FinishSnippetSetupEvent)
            if (postDetailsMode != PostDetailsMode.EVENTS_LIST) {
                requestPostFromNetwork(null)
            }
        }
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return _featureTogglesContainer
    }

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event -> handleMomentsEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleMomentsEvents(event: MomentRepositoryEvent) {
        when (event) {
            is MomentRepositoryEvent.UserMomentsStateUpdated -> {
                updateUserMomentsState(event.userMomentsStateUpdate)
            }

            else -> Unit
        }
    }

    private fun updateUserMomentsState(userMomentsStateUpdate: UserMomentsStateUpdateModel) {
        if (post?.user?.userId != userMomentsStateUpdate.userId) return

        val moments = post?.user?.moments?.copy(
            hasMoments = userMomentsStateUpdate.hasMoments,
            hasNewMoments = userMomentsStateUpdate.hasNewMoments
        )
        val user = post?.user?.copy(moments = moments)
        post = post?.copy(user = user)

        post?.let { livePostViewEvent.postValue(PostViewEvent.UpdateUserMomentsState(it)) }
    }

    private fun renderPost(postItems: List<PostUIEntity>) {
        val updatedPostData = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            postItems.getOrNull(0)?.let { postUIEntity ->
                val updatedPost = getUpdatedEventPost(postUIEntity)
                val updatedPostItems = postItems.mapIndexed { index, post -> if (index == 0) updatedPost else post }
                val noEmptyPlaceholder = updatedPostItems.all { it.feedType != FeedType.EMPTY_PLACEHOLDER }
                if (noEmptyPlaceholder && needAddCommentsEmptyPlaceholder(postUIEntity)) {
                    updatedPostItems.plus(PostUIEntity(feedType = FeedType.EMPTY_PLACEHOLDER))
                } else {
                    updatedPostItems
                }
            } ?: postItems
        } else {
            postItems
        }

        _livePostState.postValue(updatedPostData)
    }

    private fun getUpdatedEventPost(postUIEntity: PostUIEntity): PostUIEntity = postUIEntity.copy(
        tagSpan = postUIEntity.tagSpan?.copy(
            showFullText = snippetState == SnippetState.Expanded || postUIEntity.tagSpan.shortText == null
        ),
        isNotExpandedSnippetState = snippetState != SnippetState.Expanded
    )

    private fun requestPostFromNetwork(scrollCommentId: Long? = null) {
        paginationHelper.isLoadingAfter = true
        paginationHelper.isLoadingBefore = true
        viewModelScope.launch(Dispatchers.IO) {
            getPost.execute(
                params = GetPostParams(postId),
                success = {
                    viewModelScope.launch {
                        runCatching {
                            handleSuccessPostFromNetwork(it, scrollCommentId)
                        }.onFailure(Timber::e)
                    }
                },
                fail = { e ->
                    post?.let { renderPost(listOf(it.copy())) }
                    Timber.e(e)
                }
            )
        }
    }

    fun stopDownloadingPostVideo(postId: Long) {
        stopDownloadingVideoToGalleryUseCase.invoke(postId)
    }

    fun downloadPostVideo(postId: Long, assetId: String?) {
        downloadVideoToGalleryUseCase.invoke(DownloadMediaHelper.PostMediaDownloadType.PostDetailDownload(postId, assetId))
    }

    fun onJoinAnimationFinished(postUIEntity: PostUIEntity) {
        livePostViewEvent.postValue(PostViewEvent.ShowEventSharingSuggestion(postUIEntity))
    }

    fun joinEvent(postUIEntity: PostUIEntity) {
        logMapEventWantToGo(postUIEntity)
        val eventId = postUIEntity.event?.id ?: return
        viewModelScope.launch {
            runCatching {
                joinEventUseCase.invoke(eventId)
                livePostViewEvent.postValue(PostViewEvent.ShowEventSharingSuggestion(postUIEntity))
            }.onFailure {
                Timber.e(it)
                livePostViewEvent.postValue(PostViewEvent.NoInternet)
            }
        }
    }

    fun leaveEvent(postUIEntity: PostUIEntity) {
        val eventId = postUIEntity.event?.id ?: return
        viewModelScope.launch {
            runCatching {
                leaveEventUseCase.invoke(eventId)
                logMapEventMemberDeleteYouself(postUIEntity)
            }.onFailure {
                Timber.e(it)
                livePostViewEvent.postValue(PostViewEvent.NoInternet)
            }
        }
    }

    private fun checkUpdateAvailability(post: PostUIEntity, currentMedia: MediaAssetEntity?) {
        if (_featureTogglesContainer.editPostFeatureToggle.isEnabled) {
            viewModelScope.launch(Dispatchers.IO) {
                checkPostUpdateAvailability.execute(
                    params = CheckPostPostParams(postId = post.postId),
                    success = { response ->
                        livePostViewEvent.postValue(
                            PostViewEvent.PostEditAvailableEvent(
                                post = post,
                                isEditAvailable = response.isAvailable == EDIT_POST_AVAILABLE && post.isEditable(),
                                currentMedia = currentMedia
                            )
                        )
                    },
                    fail = { error ->
                        Timber.e(error)
                        livePostViewEvent.postValue(
                            PostViewEvent.PostEditAvailableEvent(
                                post = post,
                                isEditAvailable = false,
                                currentMedia = currentMedia
                            )
                        )
                    }
                )
            }
        } else {
            livePostViewEvent.postValue(PostViewEvent.PostEditAvailableEvent(
                post = post,
                isEditAvailable = false,
                currentMedia = currentMedia)
            )
        }
    }

    private fun updatePostEvent(postUIEntity: PostUIEntity) {
        val event = postUIEntity.event ?: return
        val posts = getActualPosts()
        if (posts.isNotEmpty()) {
            val post = getUpdatedEventPost(posts[0].copy(event = event))
            this.post = post
            livePostViewEvent.postValue(PostViewEvent.UpdateEventParticipationEvent(post))
        }
    }

    fun logMapEventGetTherePress(post: PostUIEntity) {
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = post.event?.id ?: return,
            authorId = post.user?.userId ?: return
        )
        val where = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            AmplitudePropertyMapEventsGetThereWhere.MAP
        } else {
            AmplitudePropertyMapEventsGetThereWhere.FEED
        }
        mapEventsAnalyticsInteractor.logMapEventGetTherePress(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = where
        )
    }

    private fun observeVolumeEvents() {
        subscribeVolumeEventsUseCase.invoke().onEach { event -> handleVolumeEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleVolumeEvents(event: VolumeRepositoryEvent) {
        when (event) {
            is VolumeRepositoryEvent.VolumeStateUpdated -> {
                livePostViewEvent.postValue(PostViewEvent.UpdateVolumeState(event.volumeState))
            }
        }
    }

    private fun observeDownloadMediaEvent() {
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

    private fun observeEventParticipationChanges() {
        viewModelScope.launch {
            observeEventParticipationChangesUseCase.invoke()
                .filter { it.id == postId }
                .onEach { post ->
                    val updatedPost = withContext(Dispatchers.Default) {
                        post.toUiEntity(textProcessorUtil = textProcessorUtil, isInSnippet = false)
                    }
                    updatePostEvent(updatedPost)
                }
                .catch { Timber.e(it) }
                .launchIn(viewModelScope)
        }
    }

    private fun handleVideoLoading(postId: Long, loadingInfo: LoadingPostVideoInfoUIModel) {
        if (isValidPostId(postId).not()) return
        val post = getActualPosts().firstOrNull()?.copy(loadingInfo = loadingInfo) ?: return
        this.post = post
        livePostViewEvent.postValue(PostViewEvent.UpdateLoadingState(loadingInfo, post))
    }

    private suspend fun handleSuccessPostFromNetwork(
        networkPost: PostModelEntity,
        scrollCommentId: Long?
    ) {
        isBlockedMe = networkPost.user?.blacklistedMe == 1
        isPostCommentable = networkPost.isAllowedToComment == true
        val isInSnippet = postDetailsMode == PostDetailsMode.EVENT_SNIPPET
        val isPreviewSnippetState = isInSnippet && (snippetState == null || snippetState == SnippetState.Preview)
        var uiPost = withContext(Dispatchers.Default) {
            networkPost
                .toUiEntity(
                    textProcessorUtil = textProcessorUtil,
                    isInSnippet = isInSnippet,
                    isNotExpandedSnippetState = isPreviewSnippetState
                )
                .setFollowButtonStateOnFirstLoad()
                .setLoadingStateOnFirstLoad()
        }
        uiPost.tagSpan?.showFullText = snippetState == null
            || snippetState == SnippetState.Expanded
            || uiPost.tagSpan?.shortText == null
        uiPost = uiPost.copy(selectedMediaPosition = post?.selectedMediaPosition ?: 0)
        renderPost(listOf(uiPost))
        this.post = uiPost
        if (scrollCommentId != null && scrollCommentId != 0L) {
            initScrollToComment(scrollCommentId)
        } else {
            initialComments()
        }
        logOpenPost()
    }

    private fun logOpenPost() {
        val post = post ?: return
        val postType = if (post.parentPost == null) AmplitudePropertyPostType.POST else AmplitudePropertyPostType.REPOST
        val commentCount = post.commentCount
        val where = AmplitudePropertyPostWhere.POST
        val whence = when(originEnum) {
            DestinationOriginEnum.MAIN_ROAD -> AmplitudePropertyPostWhence.MAIN_FEED
            DestinationOriginEnum.CUSTOM_ROAD -> AmplitudePropertyPostWhence.SELF_FEED
            DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> AmplitudePropertyPostWhence.FOLLOW_FEED
            DestinationOriginEnum.OWN_PROFILE -> AmplitudePropertyPostWhence.PROFILE
            DestinationOriginEnum.OTHER_PROFILE -> AmplitudePropertyPostWhence.USER_PROFILE
            DestinationOriginEnum.CHAT -> AmplitudePropertyPostWhence.CHAT
            DestinationOriginEnum.HASHTAG -> AmplitudePropertyPostWhence.HASHTAG
            DestinationOriginEnum.COMMUNITY -> AmplitudePropertyPostWhence.COMMUNITY
            DestinationOriginEnum.NOTIFICATIONS -> AmplitudePropertyPostWhence.NOTIFICATION
            DestinationOriginEnum.NOTIFICATIONS_REACTIONS -> AmplitudePropertyPostWhence.NOTIFICATION
            DestinationOriginEnum.DEEPLINK -> AmplitudePropertyPostWhence.DEEPLINK
            DestinationOriginEnum.PUSH -> AmplitudePropertyPostWhence.NOTIFICATION
            DestinationOriginEnum.ANNOUNCEMENT -> AmplitudePropertyPostWhence.OTHER
            null -> AmplitudePropertyPostWhence.OTHER
        }
        amplitudeComments.logOpenPost(
            postId = post.postId,
            authorId = post.user?.userId ?: NO_USER_ID,
            momentId = null,
            postType = postType,
            postContentType = post.propertyContentType(),
            where = where,
            commentCount = commentCount,
            haveText = post.postText.isEmpty(),
            havePic = !post.getImageUrl().isNullOrBlank(),
            haveVideo = post.hasPostVideo(),
            haveGif = post.hasPostGif(),
            haveMusic = post.media != null,
            recFeed = checkMainFilterRecommendedUseCase.invoke(),
            whence = whence
        )
    }

    /**
     * Реактивные события, прилетают из репозитория
     **/
    private fun observeFeedState() {
        getFeedStateUseCase
            .execute(DefParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleFeedUpdate) { Timber.e(it) }
            .addDisposable()
    }

    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach { effect ->
                if (effect is UserSettingsEffect.UserFriendStatusChanged) {
                    handleFriendStatusChanged(
                        userId = effect.userId,
                        subscribed = effect.isSubscribe
                    )
                }
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(
        userId: Long,
        subscribed: Boolean
    ) {
        if (post?.user?.userId != userId) return
        val posts = getActualPosts()
        if (posts.isNotEmpty()) {
            if (posts[0].commentAvailability == COMMENTS_AVAILABILITY_FRIENDS && posts[0].isCommunityPost().not()) {
                isPostCommentable = subscribed
                post = posts[0].copy(isAllowedToComment = subscribed)
                post?.let { posts[0] = it }
                if (needAddCommentsEmptyPlaceholder(post)) addCommentsEmptyPlaceholder() else renderPost(posts)

                paginationHelper.needToShowReplyBtn = subscribed

                livePostViewEvent.postValue(
                    PostViewEvent.UpdateCommentsReplyAvailability(needToShowReplyBtn = subscribed)
                )
            }
        }
    }

    private fun handleFeedUpdate(event: FeedUpdateEvent) {
        when {
            event is FeedUpdateEvent.FeedUpdatePayload && isValidPostId(event.postId) ->
                handleFeedUpdatePayload(event)

            event is FeedUpdateEvent.FeedPostSubscriptionChanged && isValidPostId(event.postId) ->
                handleSubscriptionChanged(event.isSubscribed)

            event is FeedUpdateEvent.FeedUserSubscriptionChanged ->
                handleUserSubscriptionChanged(
                    userId = event.userId,
                    subscribed = event.isSubscribed,
                    needToHideFollowButton = event.needToHideFollowButton
                )

            event is FeedUpdateEvent.FeedPostRemoved -> {
                if (event.postId == postId) {
                    requestPostFromNetwork(null)
                }
            }

            event is FeedUpdateEvent.FeedUpdatePostComments -> handleFeedPostCommentsUpdate(event)
        }
    }

    private fun handleFeedPostCommentsUpdate(event: FeedUpdateEvent.FeedUpdatePostComments) {
        if (event.postId != postId) return
        val comments = event.comments
        val commentChunk = liveComments.value ?: return
        liveComments.value = commentChunk.copy(items = comments, order = OrderType.INITIALIZE, countAfter = 0, countBefore = 0)
        paginationHelper.apply {
            clear()
            isLoadingAfter = false
            isLoadingBefore = false
            isTopPage = true
            if (comments.isNotEmpty()) {
                firstCommentId = event.comments.first().id
                lastCommentId = event.comments.last().id
            }
        }
    }

    private fun handleFeedUpdatePayload(event: FeedUpdateEvent.FeedUpdatePayload) {
        val posts = getActualPosts()
        if (posts.isEmpty()) return
        post?.let { post ->
            posts[0] = post.copy(
                commentCount = event.commentCount ?: post.commentCount,
                repostCount = event.repostCount ?: post.repostCount,
                reactions = event.reactions ?: post.reactions
            )

            val postUpdate = UIPostUpdate(
                posts[0].postId,
                commentCount = posts[0].commentCount,
                repostCount = posts[0].repostCount,
                reactions = posts[0].reactions
            )
            livePostViewEvent.postValue(PostViewEvent.UpdatePostValues(postUpdate))
        }
        post = posts[0]
    }

    private fun handleUserSubscriptionChanged(
        userId: Long,
        subscribed: Boolean,
        needToHideFollowButton: Boolean
    ) {
        if (post?.user?.userId != userId) return
        val posts = getActualPosts()
        if (posts.isNotEmpty()) {
            post = posts[0].copy(user = posts[0].user?.copy(subscriptionOn = subscribed.toInt()))
            post = post?.setFollowButtonState(needToHideFollowButton)
            post?.let { posts[0] = it }
            if (needAddCommentsEmptyPlaceholder(post)) addCommentsEmptyPlaceholder() else renderPost(posts)
        }
    }

    private fun handleSubscriptionChanged(isSubscribed: Boolean) {
        val posts = getActualPosts()
        if (posts.isNotEmpty()) {
            post = posts[0].copy(isPostSubscribed = isSubscribed)
            post?.let { posts[0] = it }

            if (needAddCommentsEmptyPlaceholder(post)) addCommentsEmptyPlaceholder() else renderPost(posts)
        }
    }

    private fun needAddCommentsEmptyPlaceholder(post: PostUIEntity?): Boolean {
        if (post == null) return false
        return !post.isMeBlocked()
            && post.isAllowedToComment
            && !post.hasComments()
            && toBeDeletedComments.size != SINGLE_DELETED_COMMENT
    }

    private fun getActualPosts(): MutableList<PostUIEntity> {
        val newList = mutableListOf<PostUIEntity>()
        post?.let { newList.add(it) }
        return newList
    }

    private fun isValidPostId(postId: Long?) =
        postId == post?.postId


    fun getPostViewHighlightLiveData(): LiveData<Boolean> {
        return getPostViewCollisionHighlightUseCaseUseCase.execute()
    }

    fun getSettings(): Settings? = settings

    private fun initialComments() {
        paginationHelper.clear()
        paginationHelper.needToShowReplyBtn = isBlockedMe == false && isPostCommentable == true
        paginationHelper.isLoadingAfter = true
        paginationHelper.isLoadingBefore = true

        viewModelScope.launch {
            val params = ToGetCommentParams(postId)

            toGetCommentUseCase.execute(params,
                {
                    liveComments.value = mapper.map(it, OrderType.INITIALIZE)
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                    //при инициализации не нужно запрашивать пагинацию наверх
                    paginationHelper.isTopPage = true
                },
                {
                    paginationHelper.isLastPage = true
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                }
            )
        }
    }

    private fun initListenReaction() {
        val disposable = reactionRepository
            .getCommandReactionStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { reactionUpdate ->
                proceedReaction(reactionUpdate)
            }

        disposables.add(disposable)

        if (postDetailsMode == PostDetailsMode.EVENT_SNIPPET) {
            observeReactionPostUpdatesUseCase.invoke()
                .onEach { post ->
                    if (post.id == postId) requestPostFromNetwork(null)
                }
                .launchIn(viewModelScope)
        }
    }

    private fun proceedReaction(reactionUpdate: ReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is ReactionSource.PostComment -> {
                val commentId = reactionUpdate.reactionSource.commentId
                val commentPosition = commentList.indexOfFirst { it.id == commentId }
                if (commentPosition == -1) return
                val comment = commentList[commentPosition] as? CommentEntity ?: return

                comment.comment.reactions = reactionUpdate.reactionList

                livePostViewEvent.postValue(PostViewEvent.UpdateCommentReaction(commentPosition, reactionUpdate))
            }

            is ReactionSource.Post -> {
                updatePostReaction(reactionUpdate)
            }

            else -> Unit
        }
    }

    private fun updatePostReaction(reactionUpdate: ReactionUpdate) {
        val postId = reactionUpdate.toUIPostUpdate().postId
        if (isValidPostId(postId).not()) return
        val post = getActualPosts().firstOrNull()?.copy(reactions = reactionUpdate.reactionList) ?: return
        this.post = post
        livePostViewEvent.postValue(PostViewEvent.UpdatePostReaction(reactionUpdate, post))
    }

    fun addCommentsBefore() {
        if (postDetailsMode == PostDetailsMode.EVENT_SNIPPET && snippetState != SnippetState.Expanded) return
        viewModelScope.launch {
            paginationHelper.isLoadingBefore = true
            paginationHelper.isLoadingBeforeCallback(true)
            val params = ToGetCommentParams(
                postId, startId = paginationHelper.firstCommentId,
                order = OrderType.BEFORE
            )
            toGetCommentUseCase.execute(params,
                {
                    paginationHelper.isLoadingBeforeCallback(false)
                    liveComments.value = mapper.map(it, OrderType.BEFORE)
                    paginationHelper.isLoadingBefore = false
                },
                {
                    paginationHelper.isTopPage = true
                    paginationHelper.isLoadingBefore = false
                    paginationHelper.isLoadingBeforeCallback(false)
                }
            )
        }
    }

    fun addCommentsAfter() {
        if (postDetailsMode == PostDetailsMode.EVENT_SNIPPET && snippetState != SnippetState.Expanded) return
        Timber.d("addCommentsAfter called")
        viewModelScope.launch {
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingAfterCallback(true)
            val params = ToGetCommentParams(
                postId, startId = paginationHelper.lastCommentId,
                order = OrderType.AFTER
            )

            toGetCommentUseCase.execute(params,
                {
                    paginationHelper.isLoadingAfterCallback(false)
                    liveComments.value = mapper.map(it, OrderType.AFTER)
                    paginationHelper.isLoadingAfter = false
                },
                {
                    paginationHelper.isLoadingAfterCallback(false)
                    paginationHelper.isLastPage = true
                    paginationHelper.isLoadingAfter = false
                }
            )
        }
    }

    fun addInnerComment(d0: CommentSeparatorEntity) {
        Timber.d("addInnerComment")
        viewModelScope.launch {
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingBefore = true

            val startId = d0.data.targetCommentId

            val params = ToGetCommentParams(
                postId, startId = startId, parentId = d0.data.parentId,
                order = d0.data.orderType
            )

            toGetCommentUseCase.execute(params,
                {
                    val preparedData = mapper.mapInnerCommentsToChunk(it, d0.data.orderType, d0)
                    liveComments.value = preparedData
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                },
                {
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                    livePostViewEvent.postValue(PostViewEvent.ErrorInnerPagination(d0))
                }
            )
        }
    }

    private fun initScrollToComment(scrollCommentId: Long) {
        paginationHelper.clear()
        paginationHelper.needToShowReplyBtn = isBlockedMe == false && isPostCommentable == true
        paginationHelper.isLoadingAfter = true
        paginationHelper.isLoadingBefore = true

        //при инициализации не нужно запрашивать пагинацию наверх
        paginationHelper.isTopPage = true

        viewModelScope.launch {
            val params = ToGetCommentParams(postId = postId, commentId = scrollCommentId)
            delay(DELAY_SCROLL_TO_COMMENT.toLong())
            toGetCommentUseCase.execute(params,
                {
                    val prepData = mapper.map(it, OrderType.INITIALIZE)
                        .copy(scrollCommentId = scrollCommentId)
                    liveComments.value = prepData
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                },
                {
                    paginationHelper.isLastPage = true
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                    if (it is IllegalArgumentException) {
                        if (it.message == COMMENT_NOT_FOUND) {
                            initialComments()
                        }
                    }
                }
            )
        }
    }

    fun getLastComments(postId: Long?) {
        postId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                getLastCommentsUseCase.execute(
                    params = GetLastCommentParams(postId = postId),
                    success = {
                        val beforeMyComment = it?.comments
                        if (!beforeMyComment.isNullOrEmpty()) {
                            handleCommentResponse(
                                beforeMyComment = beforeMyComment,
                                afterMyComment = mutableListOf(),
                                myComment = beforeMyComment.last(),
                                needSmoothScroll = false
                            )
                        }

                    },
                    fail = {
                        livePostViewEvent.postValue(PostViewEvent.OnScrollToBottom)
                    }
                )
            }
        }
    }

    fun sendCommentToServer(
        post: PostUIEntity? = null, //used for analytics
        postId: Long?,
        message: String,
        parentCommentId: Long
    ) {
        Timber.d("Send comment to server> postId: $postId  MSG: $message commentId: $parentCommentId")
        postId?.let { id ->
            loadingStatusLive.value = NetworkState.Status.RUNNING
            viewModelScope.launch(Dispatchers.IO) {
                val res = sendCommentUseCase.execute(SendCommentParams(
                    postId = postId,
                    text = message,
                    commentId = parentCommentId,
                    errorTypeListener = { handleError(it) }
                ))
                logCommentSent(post, parentCommentId)
                post?.let { commentSuccess(it) }
                launch(Dispatchers.Main) {
                    livePostViewEvent.value = PostViewEvent.EnableComments
                    if (res?.myComment != null) {
                        if (post?.isPostSubscribed == false) {
                            triggerAction(PostDetailsActions.SubscribePost(postId, false))
                        }
                        myTracker.trackWriteComments()
                        val beforeMyComment = res.lastComments?.comments ?: listOf()
                        val myComment = res.myComment
                        if (res.myComment.parentId == null || res.myComment.parentId == 0L) {
                            //просто новый комментарий
                            handleCommentResponse(
                                beforeMyComment = beforeMyComment,
                                afterMyComment = mutableListOf(),
                                myComment = myComment,
                                isSendingComment = true
                            )
                        } else {
                            //ответ на комментарий
                            handleInnerCommentResponse(res, res.myComment.parentId)
                        }


                        post?.apply {
                            val postType =
                                if (parentPost != null) AmplitudePropertyPostType.REPOST
                                else AmplitudePropertyPostType.POST

                            if (postType == AmplitudePropertyPostType.REPOST) {
                                if (postText.isNotEmpty()) AmplitudePropertyContentType.SINGLE
                                else AmplitudePropertyContentType.NONE
                            } else {
                                var i = 0
                                if (postText.isNotEmpty()) i += 1
                                if (!getImageUrl().isNullOrEmpty()) i += 1
                                if (getVideoUrl() != null) i += 1

                                when {
                                    i == 1 -> AmplitudePropertyContentType.SINGLE
                                    i > 1 -> AmplitudePropertyContentType.MULTIPLE
                                    else -> AmplitudePropertyContentType.NONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logCommentSent(post: PostUIEntity?, parentCommentId: Long) {
        if (post == null) return
        val commentType = if (parentCommentId == 0L) {
            AmplitudePropertyCommentType.COMMENT
        } else {
            AmplitudePropertyCommentType.REPLAY
        }
        val postId = post.postId

        val postType = if (post.parentPost == null) {
            AmplitudePropertyPostType.POST
        } else {
            AmplitudePropertyPostType.REPOST
        }

        val postContentType = if (postType == AmplitudePropertyPostType.POST) {
            var contentCount = 0
            val hasText = post.postText.isNotEmpty()
            val hasImage = !post.getImageUrl().isNullOrEmpty()
            val hasVideo = !post.getVideoUrl().isNullOrEmpty()
            val hasMusic = post.media != null
            if (hasText) contentCount++
            if (hasImage) contentCount++
            if (hasVideo) contentCount++
            if (hasMusic) contentCount++
            if (contentCount > 1) AmplitudePropertyContentType.MULTIPLE
            else AmplitudePropertyContentType.SINGLE
        } else {
            AmplitudePropertyContentType.NONE
        }
        val where = if (post.isEvent()) {
            AmplitudePropertyWhere.MAP_EVENT
        } else {
            AmplitudePropertyWhere.POST
        }
        amplitudeComments.logSentComment(
            postId = postId,
            authorId = post.user?.userId ?: NO_USER_ID,
            commentorId = getUserUid(),
            momentId = 0,
            commentType = commentType,
            postType = postType,
            postContentType = postContentType,
            whence = originEnum.toAmplitudePropertyWhence(),
            where = where,
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    private fun handleError(commentError: SendCommentError) {
        when (commentError) {
            is SendCommentError.UnknownHost ->
                livePostViewEvent.postValue(PostViewEvent.NoInternetAction)

            is SendCommentError.SendFail ->
                livePostViewEvent.postValue(ErrorPostComment)

            is SendCommentError.UserDeletedPostComment ->
                livePostViewEvent.postValue(PostViewEvent.ShowTextError(commentError.messageError))
        }
    }

    private fun getLastValidCommentId(): Long? {
        for (i in commentList.size - 1 downTo 0) {
            val comment = commentList[i]
            val hasParent = comment.parentId != null
            val isDeleted = comment is DeletedCommentEntity
            val hasChild = isCommentHasChild(comment.id)
            if ((!hasParent && !isDeleted) ||
                (isDeleted && (hasChild || toBeDeletedComments.any { it.id == comment.id }))
            ) {
                return comment.id
            }
        }
        return paginationHelper.lastCommentId
    }

    private fun isCommentHasChild(commentId: Long): Boolean {
        val child = commentList.findLast { it.parentId == commentId }
        return child != null
    }

    private fun handleCommentResponse(
        beforeMyComment: List<CommentEntityResponse>,
        afterMyComment: List<CommentEntityResponse>,
        myComment: CommentEntityResponse,
        needSmoothScroll: Boolean = true,
        isSendingComment: Boolean = false
    ) {

        getLastValidCommentId().let {
            val hasIntersections = it?.checkHasCommentIntersection(beforeMyComment) ?: false
            val beforeNew = mutableListOf<CommentEntityResponse>()
            if (!hasIntersections) paginationHelper.clear()
            beforeNew.addAll(beforeMyComment.filter { item ->
                item.id > it ?: Long.MIN_VALUE
            })

            livePostViewEvent.value =
                PostViewEvent.NewCommentSuccess(
                    myCommentId = myComment.id,
                    beforeMyComment = mapper.mapCommentsForNewMessage(
                        beforeNew,
                        OrderType.BEFORE,
                        hasIntersections
                    ),
                    afterMyComment = mapper.mapCommentsForNewMessage(
                        afterMyComment,
                        OrderType.AFTER,
                        hasIntersections
                    ),
                    hasIntersection = hasIntersections,
                    needSmoothScroll = needSmoothScroll,
                    needToShowLastFullComment = isSendingComment
                )


            paginationHelper.isTopPage = false
            paginationHelper.isLastPage = false
        }
    }

    private fun handleInnerCommentResponse(res: SendCommentResponse, parentId: Long) {
        res.lastComments ?: return

        val preparedData = mapper.mapInnerCommentsToChunk(res.lastComments, order = OrderType.BEFORE, parentId)

        livePostViewEvent.value = PostViewEvent.NewInnerCommentSuccess(
                chunk = preparedData,
                parentId = parentId
            )

    }

    private fun decrementPostCommentCounter() {
        post?.let { nonNullPost ->
            if (nonNullPost.commentCount == 0) return@let
            forceUpdatePostUseCase.execute(
                UpdatePostParams(
                    FeedUpdateEvent.FeedUpdatePayload(
                        postId = nonNullPost.postId,
                        commentCount = nonNullPost.commentCount - 1
                    )
                )
            )
        }
    }

    private fun deletePost(postId: Long?) {
        postId?.let { id ->
            viewModelScope.launch {
                deletePostUseCase.execute(DeletePostParams(id),
                    {
                        Timber.e("Successfully removed Post")
                        isUserDeletedOwnPost = true
                        livePostViewEvent.postValue(PostViewEvent.DeletePost())
                        if (post?.event != null) {
                            logMapEventDelete(post)
                        }
                    },
                    { Timber.e("Fail removed Post") }
                )
            }
        }
    }

    private fun subscribePost(postId: Long?, notifyUser: Boolean) {
        postId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                subscribePostUseCase.execute(
                    params = SubscribePostParams(id),
                    success = {
                        if (notifyUser) {
                            livePostViewEvent.postValue(PostViewEvent.SubscribePost)
                        }
                        handleSubscriptionChanged(true)
                    },
                    fail = {
                        Timber.e("Fail subscribe POST id:$id")
                    }
                )
            }
        }
    }

    private fun unsubscribePost(postId: Long?) {
        postId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                unsubscribePost.execute(
                    params = UnsubscribePostParams(id),
                    success = {
                        livePostViewEvent.postValue(PostViewEvent.UnsubscribePost)
                        handleSubscriptionChanged(false)
                    },
                    fail = {
                        Timber.e("Fail UnSubscribe POST id:$id")
                    }
                )
            }
        }
    }

    private fun addComplaint(postId: Long?) {
        postId?.let { id ->
            viewModelScope.launch {
                repository.addPostComplaint(id,
                    {
                        Timber.e("Success post complaint")
                        livePostViewEvent.postValue(PostViewEvent.AddComplaint)
                    },
                    { Timber.e("Fail post complaint") })
            }
        }
    }

    /**
     * Добавить жалобу на комментарий
     * https://nomera.atlassian.net/browse/BR-6018
     * */
    fun addPostCommentComplaint(commentId: Long) {
        viewModelScope.launch {
            repository.addPostCommentComplaint(
                commentId = commentId,
                success = {
                    livePostViewEvent.postValue(PostViewEvent.AddPostCommentComplaint)
                },
                fail = {
                    Timber.e("Fail post complaint")
                }
            )
        }
    }

    private fun hideUserRoad(userId: Long?) {
        userId?.let { id ->
            viewModelScope.launch {
                hideRoadUseCase.execute(
                    params = HidePostsOfUserParams(id),
                    success = {
                        Timber.e("Success hide user road")
                        livePostViewEvent.postValue(PostViewEvent.HideUserRoad(id))
                    },
                    fail = {
                        Timber.e("Fail hide user road")
                    }
                )
            }
        }

    }

    private fun unsubscribeFromUser(
        postId: Long?,
        userId: Long?,
        postOrigin: DestinationOriginEnum?,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean
    ) {
        if (postId != null && userId != null) {
            viewModelScope.launch {
                repository.unsubscribeFromUser(userId,
                    {
                        Timber.e("Success unsubscribe from user:$userId")
                        logUnsubscribePost(
                            userId = userId,
                            postOrigin = postOrigin,
                            fromFollowButton = fromFollowButton,
                            approved = approved,
                            topContentMaker = topContentMaker
                        )
                        updateUserSubscription(
                            postId = postId,
                            userId = userId,
                            isSubscribed = false,
                            needToHideFollowButton = false
                        )
                    },
                    { Timber.e("Fail unsubscribe from user:$userId") })
            }
        }
    }

    private fun handleGetLinkAndCopy(postId: Long?) {
        if (postId == null) return
        viewModelScope.launch {
            getPostLinkUseCase.execute(
                params = GetPostLinkParams(postId),
                success = { response ->
                    livePostViewEvent.postValue(PostViewEvent.CopyLinkEvent(response.deeplinkUrl))
                },
                fail = { exception ->
                    Timber.e(exception)
                }
            )
        }
    }

    private fun getPostDataForScreenshotPopup(postId: Long?, event: EventUiModel?) {
        if (postId == null) return
        viewModelScope.launch {
            getPostLinkUseCase.execute(
                params = GetPostLinkParams(postId),
                success = { response ->
                    val eventIconRes = if (event?.eventType == null) null else eventsCommonUiMapper.mapEventTypeImgResId(event.eventType)
                    val eventLabelModel = if (event == null) null else eventLabelUiMapper.mapEventLabelUiModel(event, false)
                    val eventDateAndTime = if (eventLabelModel == null) null else "${eventLabelModel.date} ${eventLabelModel.time}"
                    livePostViewEvent.postValue(PostViewEvent.ShowScreenshotPopup(response.deeplinkUrl, eventIconRes, eventDateAndTime))
                },
                fail = { exception ->
                    Timber.e(exception)
                }
            )
        }
    }

    private fun saveLastPostMediaViewInfo(lastPostMediaViewInfo: PostMediaViewInfo?) {
        setLastPostMediaViewInfoUseCase.invoke(lastPostMediaViewInfo)
    }

    private fun updateUserSubscription(
        postId: Long,
        userId: Long,
        isSubscribed: Boolean,
        needToHideFollowButton: Boolean
    ) {
        viewModelScope.launch {
            updateReactiveSubscription.execute(
                params = UpdateSubscriptionUserParams(
                    postId = postId,
                    userId = userId,
                    isSubscribed = isSubscribed,
                    needToHideFollowButton = needToHideFollowButton,
                    isBlocked = false
                ),
                success = {},
                fail = {}
            )
        }
    }

    private fun updatePostSubscription(
        postId: Long,
        isSubscribed: Boolean,
        onSuccess: (Boolean) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            updateReactivePostSubscription.execute(
                params = UpdateSubscriptionPostParams(postId, isSubscribed),
                success = onSuccess,
                fail = onError
            )
        }
    }

    private fun subscribeToUser(
        postId: Long?,
        userId: Long?,
        postOrigin: DestinationOriginEnum?,
        needToHideFollowButton: Boolean,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean
    ) {
        if (postId != null && userId != null) {
            viewModelScope.launch {
                repository.subscribeToUser(userId,
                    {
                        logSubscribePost(
                            userId = userId,
                            postOrigin = postOrigin,
                            fromFollowButton = fromFollowButton,
                            approved = approved,
                            topContentMaker = topContentMaker
                        )
                        updateUserSubscription(
                            postId = postId,
                            userId = userId,
                            isSubscribed = true,
                            needToHideFollowButton = needToHideFollowButton
                        )
                    },
                    { Timber.e("Fail subscribe from user:$userId") })
            }
        }
    }

    private fun logSubscribePost(
        userId: Long,
        postOrigin: DestinationOriginEnum?,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean
    ) {
        val propertyType = if (fromFollowButton) AmplitudePropertyType.POST else AmplitudePropertyType.POST_MENU
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = approved
        )
        val where = if (post?.isEvent().isTrue()) {
            AmplitudeFollowButtonPropertyWhere.EVENT_SNIPPET
        } else {
            postOrigin.toAmplitudeFollowButtonPropertyWhere()
        }
        amplitudeFollowButton.followAction(
            fromId = appSettings.readUID(),
            toId = userId,
            where = where,
            type = propertyType,
            amplitudeInfluencerProperty = influencerProperty
        )
    }

    private fun logUnsubscribePost(
        userId: Long,
        postOrigin: DestinationOriginEnum?,
        fromFollowButton: Boolean,
        approved: Boolean,
        topContentMaker: Boolean
    ) {
        val propertyType = if (fromFollowButton) AmplitudePropertyType.POST else AmplitudePropertyType.POST_MENU
        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = approved
        )
        val where = if (post?.isEvent().isTrue()) {
            AmplitudeFollowButtonPropertyWhere.EVENT_SNIPPET
        } else {
            postOrigin.toAmplitudeFollowButtonPropertyWhere()
        }
        amplitudeFollowButton.logUnfollowAction(
            fromId = getUserUid(),
            toId = userId,
            where = where,
            type = propertyType,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty
        )
    }

    private fun blockUser(userId: Long?, remoteUserId: Long?) {
        if (userId == null || remoteUserId == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val params = DefBlockParams(
                userId = userId,
                remoteUserId = remoteUserId,
                isBlocked = true
            )

            kotlin.runCatching {
                blockUser.invoke(
                    params = params
                )
            }.onSuccess {
                livePostViewEvent.postValue(PostViewEvent.UserBlocked(remoteUserId))
            }.onFailure {
                livePostViewEvent.postValue(PostViewEvent.NoInternet)
            }
        }
    }

    fun getSensitiveContentManager(): ISensitiveContentManager = repository

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    private fun refreshPost(postId: Long?) {
        postId?.let { id ->
            viewModelScope.launch {
                forceUpdatePostUseCase.execute(UpdatePostParams(FeedUpdateEvent.FeedUpdateAll(postId = id)))
            }
        }
    }

    private var isMarkedPostCommentAsSeen = false

    fun onItemSeen(postCommentsModel: PostUIEntity) {
        if (isMarkedPostCommentAsSeen) return
        postCommentsModel.postId.let {
            markPost(it)
            markComments(it)
            isMarkedPostCommentAsSeen = true
        }
    }

    fun markComments(postId: Long) {
        markNotificationAsRead
            .execute(MarkPostCommentParams(postId, false))
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
            .addDisposable()
    }

    private fun markPost(postId: Long) {
        markNotificationAsRead
            .execute(MarkPostCommentParams(postId, true))
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
            .addDisposable()
    }

    private fun complainComment(commentId: Long?) {
        commentId?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val success = complainComment.execute(ComplainCommentParams(it))
                if (success) livePostViewEvent.postValue(PostViewEvent.ComplainSuccess)
                else livePostViewEvent.postValue(PostViewEvent.NoInternet)
            }
        }
    }

    fun markAsDeletePostComment(
        originalComment: CommentUIType,
        whoDeleteComment: WhoDeleteComment,
    ) {
        val toBeDeleted = ToBeDeletedCommentEntity(
            id = originalComment.id,
            whoDeleteComment = whoDeleteComment,
            originalComment = originalComment
        )
        toBeDeletedComments.add(toBeDeleted)
        livePostViewEvent.postValue(
            MarkCommentForDeletion(
                commentID = originalComment.id,
                whoDeleteComment = whoDeleteComment
            )
        )
    }

    fun cancelDeletePostComment(
        originalComment: CommentUIType
    ) {
        val toBeDeleted = toBeDeletedComments.find { it.id == originalComment.id }
        toBeDeletedComments.remove(toBeDeleted)
        livePostViewEvent.postValue(CancelDeleteComment(toBeDeleted?.originalComment ?: originalComment))
    }

    fun deletePostComment(
        commentId: Long,
        whoDeleteComment: WhoDeleteComment,
        comment: CommentUIType
    ) {
        applicationScope.launch {
            decrementPostCommentCounter()
            runCatching {
                deletePostCommentUseCase.invoke(DeletePostCommentParams((commentId)))
            }.onSuccess {
                val toBeDeleted = toBeDeletedComments.find { deleted -> deleted.id == commentId }
                toBeDeletedComments.remove(toBeDeleted)
                livePostViewEvent.value = DeleteComment(commentId, whoDeleteComment)
            }.onFailure {
                val toBeDeleted = toBeDeletedComments.find { deleted -> deleted.id == commentId }
                toBeDeletedComments.remove(toBeDeleted)
                livePostViewEvent.postValue(PostViewEvent.ErrorDeleteComment(toBeDeleted?.originalComment ?: comment))
                handleFailure(Failure.ServerError(""))
                Timber.e(it)
            }
        }
    }

    fun repostSuccess(post: PostUIEntity, repostTargetCount: Int = 1) {
        forceUpdatePostUseCase.execute(
            UpdatePostParams(
                FeedUpdateEvent.FeedUpdatePayload(
                    postId = post.postId,
                    repostCount = post.repostCount + repostTargetCount
                )
            )
        )
    }

    private fun commentSuccess(post: PostUIEntity) {
        updatePostSubscription(
            postId = post.postId,
            isSubscribed = true,
            onSuccess = {
                forceUpdatePostUseCase.execute(
                    UpdatePostParams(
                        FeedUpdateEvent.FeedUpdatePayload(
                            postId = post.postId,
                            commentCount = post.commentCount + 1
                        )
                    )
                )
            }
        )
    }

    private fun addCommentsEmptyPlaceholder() {
        post?.let {
            renderPost(listOf(it.copy(), PostUIEntity(feedType = FeedType.EMPTY_PLACEHOLDER)))
        }
    }

    private fun removeCommentsEmptyPlaceHolder() {
        post?.let {
            renderPost(listOf(it))
        }
    }

    private fun showError(@StringRes resId: Int) =
        livePostViewEvent.postValue(PostViewEvent.ShowTextError(resourceManager.getString(resId)))

    fun triggerAction(action: PostDetailsActions) {
        when (action) {
            is PostDetailsActions.RepostClick -> handleRepostClick(action.post)
            is PostDetailsActions.OnPostClick -> handlePostClick()
            PostDetailsActions.Refresh -> handleRefreshPostComments()
            is PostDetailsActions.OnBadWordClicked -> handleBadWordClick(action)
            PostDetailsActions.AddEmptyCommentsPlaceHolder -> addCommentsEmptyPlaceholder()
            PostDetailsActions.RemoveEmptyCommentsPlaceHolder -> removeCommentsEmptyPlaceHolder()
            is PostDetailsActions.RefreshPost -> refreshPost(action.postId)
            is PostDetailsActions.DeletePost -> deletePost(action.postId)
            is PostDetailsActions.AddComplaintPost -> addComplaint(action.postId)
            is PostDetailsActions.AddComplaintPostComment -> complainComment(action.commentId)
            is PostDetailsActions.BlockUser -> blockUser(action.userId, action.remoteUserId)
            is PostDetailsActions.HideUserRoad -> hideUserRoad(action.userId)
            is PostDetailsActions.SubscribePost -> subscribePost(action.postId, action.notifyUser)
            is PostDetailsActions.SubscribeUser -> subscribeToUser(
                postId = action.postId,
                userId = action.userId,
                postOrigin = action.postOrigin,
                needToHideFollowButton = action.needToHideFollowButton,
                fromFollowButton = action.fromFollowButton,
                approved = action.approved,
                topContentMaker = action.topContentMaker
            )

            is PostDetailsActions.UnsubscribePost -> unsubscribePost(action.postId)
            is PostDetailsActions.UnsubscribeUser -> unsubscribeFromUser(
                postId = action.postId,
                userId = action.userId,
                postOrigin = action.postOrigin,
                fromFollowButton = action.fromFollowButton,
                approved = action.approved,
                topContentMaker = action.topContentMaker
            )

            is PostDetailsActions.CopyPostLink -> handleGetLinkAndCopy(postId = action.postId)
            is PostDetailsActions.CheckUpdateAvailability -> checkUpdateAvailability(action.post, action.currentMedia)
            is PostDetailsActions.EditPost -> openEditPost(action.post)
            is PostDetailsActions.GetPostDataForScreenshotPopup -> getPostDataForScreenshotPopup(action.postId, action.event)
            is PostDetailsActions.SaveLastPostMediaViewInfo -> saveLastPostMediaViewInfo(action.lastPostMediaViewInfo)
        }
    }

    private fun handleBadWordClick(action: PostDetailsActions.OnBadWordClicked) {
        val originalText = when (action.tagOrigin) {
            TagOrigin.POST_TEXT -> action.post.tagSpan?.text ?: ""
            TagOrigin.POST_TITLE -> action.post.event?.tagSpan?.text ?: ""
        }
        val badWord = action.click.badWord ?: ""
        val updatedText = originalText.replaceRange(action.click.startIndex, action.click.endIndex, badWord)
        val newSpan = mutableListOf<UniquenameSpanData>()
        val postSpanData = when (action.tagOrigin) {
            TagOrigin.POST_TEXT -> action.post.tagSpan?.spanData
            TagOrigin.POST_TITLE -> action.post.event?.tagSpan?.spanData
        }
        postSpanData?.let { newSpan.addAll(it.filter { data -> action.click.tagSpanId != data.id }) }
        val spanData = UniquenameSpanData(
            id = null,
            tag = null,
            type = UniquenameType.FONT_STYLE_ITALIC.value,
            startSpanPos = action.click.startIndex,
            endSpanPos = action.click.startIndex + badWord.length,
            userId = null,
            groupId = null,
            symbol = badWord
        )
        newSpan.add(spanData)
        val newItem = when (action.tagOrigin) {
            TagOrigin.POST_TEXT -> action.post.copy(
                postText = updatedText,
                tagSpan = action.post.tagSpan?.copy(
                    text = updatedText,
                    spanData = newSpan
                )
            )

            TagOrigin.POST_TITLE -> action.post.copy(
                event = action.post.event?.copy(
                    title = updatedText,
                    tagSpan = action.post.event.tagSpan?.copy(
                        text = updatedText,
                        spanData = newSpan
                    )
                )
            )
        }
        val posts = getActualPosts()
        if (posts.isNotEmpty()) {
            posts[0] = newItem
            this.post = newItem
            val postUpdate = UIPostUpdate.UpdateTagSpan(newItem.postId, newItem)
            livePostViewEvent.postValue(PostViewEvent.UpdateTagSpan(postUpdate))
        }
    }

    private fun handleRefreshPostComments() = requestPostFromNetwork()

    private fun handlePostClick() = Unit

    private fun handleRepostClick(post: PostUIEntity) {
        val blacklistedMe = post.user?.blackListedMe ?: false
        val blacklistedByMe = post.user?.blackListedByMe ?: false
        if (blacklistedMe || blacklistedByMe) {
            showError(R.string.post_hide_error_message)
        } else {
            livePostViewEvent.postValue(PostViewEvent.OpenRepostMenu(post))
        }
    }

    private fun logMapEventWantToGo(post: PostUIEntity) {
        val eventId = post.event?.id ?: return
        val authorId = post.user?.userId ?: return
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = eventId,
            authorId = authorId
        )
        val where = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            AmplitudePropertyMapEventsWantToGoWhere.MAP
        } else {
            AmplitudePropertyMapEventsWantToGoWhere.FEED
        }
        val mapEventInvolvementParamsAnalyticsModel = MapEventInvolvementParamsAnalyticsModel(
            membersCount = post.event.participation.participantsCount,
            reactionCount = post.reactions?.size ?: 0,
            commentCount = post.commentCount
        )
        mapEventsAnalyticsInteractor.logMapEventWantToGo(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = where,
            mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel
        )
    }

    private fun logMapEventDelete(post: PostUIEntity?) {
        post?.event ?: return
        val mapEventDeletedEventParamsAnalyticsModel = mapAnalyticsMapperImpl
            .mapMapEventDeletedEventParamsAnalyticsModel(post) ?: return
        val where = if (
            postDetailsMode == PostDetailsMode.EVENT_SNIPPET
            || postDetailsMode == PostDetailsMode.EVENTS_LIST
        ) {
            AmplitudePropertyMapEventsDeleteWhere.MAP
        } else {
            AmplitudePropertyMapEventsDeleteWhere.POST
        }
        mapEventsAnalyticsInteractor.logMapEventDelete(
            mapEventDeletedEventParamsAnalyticsModel = mapEventDeletedEventParamsAnalyticsModel,
            where = where
        )
    }

    private fun logMapEventMemberDeleteYouself(post: PostUIEntity) {
        val eventId = post.event?.id ?: return
        val authorId = post.user?.userId ?: return
        mapEventsAnalyticsInteractor.logMapEventMemberDeleteYouself(
            MapEventIdParamsAnalyticsModel(
                eventId = eventId,
                authorId = authorId
            )
        )
    }

    private fun openEditPost(post: PostUIEntity) {
        livePostViewEvent.postValue(PostViewEvent.OpenEditPostEvent(post = post))
    }

    private fun PostUIEntity.setFollowButtonStateOnFirstLoad(): PostUIEntity {
        val isMyPost = isMyPost(appSettings.readUID())
        val isSubscribed = isSubscribedToPostUser()
        return this.copy(needToShowFollowButton = !isMyPost && !isSubscribed)
    }

    private fun PostUIEntity.setLoadingStateOnFirstLoad(): PostUIEntity {
        (livePostViewEvent.value as? PostViewEvent.UpdateLoadingState)?.let {
            if (it.post.postId == postId) {
                return this.copy(loadingInfo = it.loadingInfo)
            }
        }

        return this
    }

    private fun PostUIEntity.setFollowButtonState(needToHideFollowButton: Boolean): PostUIEntity {
        val isMyPost = isMyPost(appSettings.readUID())
        return this.copy(
            needToShowFollowButton = !needToHideFollowButton && !isMyPost
        )
    }
}
