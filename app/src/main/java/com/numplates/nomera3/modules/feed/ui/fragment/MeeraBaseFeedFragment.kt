package com.numplates.nomera3.modules.feed.ui.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.addAnimationTransitionByDefault
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.gone
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.string
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.UploadType
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.ACTION_AFTER_SUBMIT_LIST_DELAY
import com.numplates.nomera3.App
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAnalyticPost
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.UserComplainViewModel
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.ExtraLinearLayoutManager
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraFeedAdapter
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.feed.ui.util.divider.PostDividerDecoration
import com.numplates.nomera3.modules.feed.ui.viewholder.BasePostHolder
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewEventPost
import com.numplates.nomera3.modules.feed.ui.viewmodel.MeeraFeedViewModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.PostSubscribeTitle
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_TYPE
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_POST_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_VIDEO_DATA
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.fragments.MeeraBaseRoadsFragment
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventNavigationBottomsheetDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.main.map.participant.MeeraEventParticipantsListFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.setHiddenState
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.MeeraAdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.MeeraComplaintDialog
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_DATA
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST_ID
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_DEFAULT_VOLUME_ENABLED
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_POSITION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_ROAD_TYPE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_FROM_USER_ROAD
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_SHOW_HIDE_POSTS_BTN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TIME_MILLS
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.MeeraFeedRecyclerView
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy.ZoomyProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.math.max

private const val DELAY_DELETE_NOTIFICATION_SEC = 4L



private const val REFRESH_ALPHA_INVISIBLE = 0f
private const val REFRESH_ALPHA_VISIBLE = 1f
private const val REFRESH_VISIBLE_ANIMATION_DURATION = 200L

/**
 * Базовый класс для всех постов, загружаемых только по сети
 * без кэштрования в БД
 */
abstract class MeeraBaseFeedFragment(
    layout: Int = R.layout.meera_fragment_show_community,
    behaviourConfigState: ScreenBehaviourState = ScreenBehaviourState.Full
) :
    MeeraBaseDialogFragment(
        layout = layout,
        behaviourConfigState = behaviourConfigState
    ),
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    MeeraPostCallback,
    VolumeStateCallback,
    MomentCallback {

    private var movedToEvent: Boolean = false
    private var wasHideable: Boolean = false

    private var postDisposable: Disposable? = null

    private var blurHelper: BlurHelper? = null

    private var zoomyProvider: ZoomyProvider? = null

    private var formatterProvider: AllRemoteStyleFormatter? = null

    private var sensitiveContentManager : ISensitiveContentManager? = null

    private var cacheUtil: CacheUtil? = null

    private var featureTogglesContainer: FeatureTogglesContainer? = null

    private var scrollFirstPageJob: Job? = null

    protected val feedAdapter: MeeraFeedAdapter by lazy {
        MeeraFeedAdapter(needToShowCommunityLabel = isNotCommunityScreen())
    }

    private val deleteNotificationState = linkedMapOf<String, NotificationCellUiModel>()
    private var pendingDeleteSnackbar: UiKitSnackBar? = null

    companion object {
        // Road types
        const val REQUEST_ROAD_TYPE_ALL = 0
        const val REQUEST_ROAD_TYPE_USER = 1
        const val REQUEST_ROAD_TYPE_GROUP = 2
        const val REQUEST_ROAD_TYPE_SUBSCRIPTIONS = 5
    }

    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) {
        ComplainsNavigator(
            requireActivity()
        )
    }

    private var undoSnackbar: UiKitSnackBar? = null

    protected val feedViewModel by viewModels<MeeraFeedViewModel> { App.component.getViewModelFactory() }
    private val userComplainViewModel by viewModels<UserComplainViewModel> { App.component.getViewModelFactory() }

    private var _layoutManager: ExtraLinearLayoutManager? = null

    // scroll refresh button
    private var isVisibleScrollRefresh = false
    private var isPushScrollRefresh = false
    private var selectedUserId: Long? = 0
    protected var isSavingFeedPhoto = false

    private var needToShowRepostBtn = true

    private var loadScrollListener: RecyclerPaginationListener? = null
    private var infoTooltip: NSnackbar? = null

   private lateinit var roadType: NetworkRoadType

    protected var feedRecycler: MeeraFeedRecyclerView? = null

    private var scrollToTopView: View? = null

    protected var communityUserRole = CommunityUserRole.REGULAR

    private var isRefreshButtonShown = false

    private var recyclerDecoration: PostDividerDecoration? = null

    abstract fun onClickScrollUpButton()

    abstract fun isNotCommunityScreen(): Boolean

    // нужно ли открывать профиль при нажатии на аватар и на ник в посте
    abstract val needToShowProfile: Boolean

    private val _audioFeedHelper: AudioFeedHelper
        get() = feedViewModel.getAudioHelper()

    private val priorityListener: () -> Unit = {
        feedRecycler?.turnOffAudioOfVideo()
    }

    private var postCollisionDetector: PostCollisionDetector? = null

    private var dotsMenuPost: PostUIEntity? = null

    private val act by lazy { (requireActivity() as MeeraAct) }

    private var reactionAnimationHelper: ReactionAnimationHelper? = null

    open fun scrollToTopWithoutRefresh() {}

    open fun getHashtag(): String? = null

    open fun setTotalPostsCount(count: Long) {}

    abstract fun getAnalyticPostOriginEnum(): DestinationOriginEnum?

    fun getAdapterPosts() = feedAdapter

    open fun getCommunityId() = -1L

    open fun getWhereCommunityOpened() = AmplitudePropertyWhereCommunityOpen.FEED

    open fun getWhereFromHashTagPressed(): AmplitudePropertyWhere? = AmplitudePropertyWhere.FEED

    open fun getAmplitudeWhereFromOpened(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereProfileFromOpened(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereMomentOpened(): AmplitudePropertyMomentScreenOpenWhere =
        AmplitudePropertyMomentScreenOpenWhere.OTHER

    abstract fun getFormatter(): AllRemoteStyleFormatter

    abstract fun getPostViewRoadSource(): PostViewRoadSource

    abstract fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String? = null)

    abstract fun getParentContainer(): ViewGroup?

    protected open fun getRefreshTopButtonView(): View? {
        return null
    }

    open fun scrollUpCommunityPosts() = Unit

    fun resetAllZoomViews() {
        feedRecycler?.apply {
            val viewHolders = ArrayList<RecyclerView.ViewHolder>()
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                viewHolders.add(getChildViewHolder(view))
            }

            viewHolders.forEach { viewHolder ->
                when (viewHolder) {
                    is BasePostHolder -> viewHolder.endZoom()
                }
            }
        }
    }

    fun resetLastVideoPlaybackPosition() {
        triggerPostsAction(FeedViewActions.SaveLastPostMediaViewInfo(null))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapterCallbacks()
        initReactionAnimationHelper()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initStatusToastCallback()
        initСomplainEventsObserver()
    }

    private fun initAdapterCallbacks() {
        feedAdapter.setPostCallback(this)
        feedAdapter.setMomentCallback(this)
        feedAdapter.setVolumeStateCallback(this)

        blurHelper =
            BlurHelper(context = requireContext(), lifecycle = viewLifecycleOwner.lifecycle).also { blurHelper ->
                feedAdapter.setBlurHelper(blurHelper)
            }

        sensitiveContentManager = object : ISensitiveContentManager {
            override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
                return feedViewModel.isMarkedAsSensitivePost(postId)
            }

            override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
                feedViewModel.markPostAsNotSensitiveForUser(postId, parentPostId)
                feedViewModel.refreshPost(postId)
                feedViewModel.refreshPost(parentPostId)
            }
        }.also { sensitiveContentManager ->
            feedAdapter.setContentManager(sensitiveContentManager)
        }

        zoomyProvider = ZoomyProvider { Zoomy.Builder(act) }.also { zoomyProvider ->
            feedAdapter.setZoomyProvider(zoomyProvider)
        }

        cacheUtil = CacheUtil(requireContext()).also { cacheUtil ->
            feedAdapter.setCacheUtils(cacheUtil)
        }

        feedAdapter.setAudioFeedHelper(_audioFeedHelper)

        featureTogglesContainer = feedViewModel.getFeatureTogglesContainer().also { featureTogglesContainer ->
            feedAdapter.setFeatureTogglesContainer(featureTogglesContainer)
        }

        formatterProvider = AllRemoteStyleFormatter(feedViewModel.getSettings()).also { formatterProvider->
            feedAdapter.setRemoteStyleFormatter(formatterProvider)
        }
    }

    private fun initReactionAnimationHelper() {
        reactionAnimationHelper = ReactionAnimationHelper()
    }

    private fun initStatusToastCallback() {
        act.getMeeraStatusToastViewController().apply {
            setOnToastControllerRepeatListener { type, postStringEntity ->
                when (type) {
                    UploadType.Post, UploadType.EditPost -> {
                        navigateEditPostFragment(post = null, postStringEntity = postStringEntity)
                    }

                    UploadType.EventPost -> {
                        //todo ROAD_FIX
                    }

                    else -> Unit
                }
            }
            initAdditionalMargin(0)
        }
    }

    override fun onCancelByUser(menuTag: String?) {
        feedViewModel.logPostMenuAction(
            post = dotsMenuPost ?: return,
            action = AmplitudePropertyMenuAction.CANCEL,
            authorId = selectedUserId,
        )
        dotsMenuPost = null
    }

    private fun initViewModel(roadType: NetworkRoadType) {
        val type = when (roadType) {
            NetworkRoadType.ALL -> RoadTypesEnum.MAIN
            is NetworkRoadType.COMMUNITY -> {
                needToShowRepostBtn = !roadType.isPrivateGroup
                feedAdapter?.isNeedToShowRepostBtn = needToShowRepostBtn
                RoadTypesEnum.COMMUNITY
            }
            NetworkRoadType.HASHTAG -> RoadTypesEnum.HASHTAG
            NetworkRoadType.SUBSCRIPTIONS -> RoadTypesEnum.SUBSCRIPTION
            is NetworkRoadType.USER -> RoadTypesEnum.PERSONAL
        }
        val origin = DestinationOriginEnum.fromNetworkRoadType(roadType)
        feedViewModel.initRoadType(type = type, originEnum = origin)
    }

    fun initRoadTypeAndViewModel(roadType: NetworkRoadType) {
        this.roadType = roadType
        initViewModel(roadType)
    }

    fun initPostsAdapter(
        roadType: NetworkRoadType? = null,
        recyclerView: MeeraFeedRecyclerView?,
        scrollToTopView: View? = null
    ) {

        context?.let {
            this._layoutManager = ExtraLinearLayoutManager(it)
        } ?: kotlin.run {
            this._layoutManager = ExtraLinearLayoutManager(act)
        }
        roadType?.let { this.roadType = it }
        this.feedRecycler = recyclerView
        this.feedRecycler?.layoutManager = _layoutManager
        this.feedRecycler?.setAudioFeedHelper(_audioFeedHelper)
        this.scrollToTopView = scrollToTopView

        initPostViewCollisionDetector(recyclerView)

        this.feedRecycler?.setVolumeStateCallback(this)

        this.recyclerDecoration = PostDividerDecoration.build(requireContext()).also {
            this.feedRecycler?.addItemDecoration(it)
        }

        handleClickScrollRefreshButton(scrollToTopView)

    }

    fun stopVideoIfExists() = feedRecycler?.onStop()

    private fun initPostViewCollisionDetector(recyclerView: RecyclerView?) {
        if (recyclerView == null) {
            return
        }
        if (postCollisionDetector == null && getPostViewRoadSource() != PostViewRoadSource.Disable) {
            val postViewHighlightLiveData = feedViewModel.getPostViewHighlightLiveData()

            postCollisionDetector = PostCollisionDetector.create(
                detectTime = PostCollisionDetector.getDurationMsFromSettings(feedViewModel.getSettings()),
                postViewHighlightEnable = postViewHighlightLiveData.value ?: false,
                recyclerView = recyclerView,
                roadFragment = this,
                roadSource = getPostViewRoadSource(),
                detectPostViewCallback = { postViewDetectModel ->
                    feedViewModel.detectPostView(postViewDetectModel)
                },
                postUploadPostViewsCallback = {
                    feedViewModel.uploadPostViews()
                }
            )

            postViewHighlightLiveData.observe(viewLifecycleOwner) { postViewHighlightEnable ->
                postCollisionDetector?.setPostViewHighlightEnable(postViewHighlightEnable)
            }
        }
    }

    override fun requestNewMomentsPage(pagingTicket: String?) =
        feedViewModel.getMomentDelegate().requestMomentsPage(pagingTicket = pagingTicket)

    override fun onMomentsCarouselBecomeNotVisible() = Unit

    override fun isRequestingMoments(): Boolean = feedViewModel.getMomentDelegate().isUpdatingMoments()

    override fun isMomentsCarouselLastPage(): Boolean =
        feedViewModel.getMomentDelegate().isMomentsLastPage()

    override fun isMomentPagingListTicketValid(ticket: String?): Boolean =
        feedViewModel.getMomentDelegate().isMomentPagingListTicketValid(ticket)

    override fun onMomentTapCreate(entryPoint: AmplitudePropertyMomentEntryPoint) {
        feedViewModel.logMomentTapCreate(entryPoint)
    }

    override fun setVolumeState(volumeState: VolumeState) {
        feedViewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
    }

    override fun getVolumeState() = feedViewModel.getVolumeState()

    override fun onDotsMenuClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        currentMedia: MediaAssetEntity?
    ) {
        requireActivity().vibrate()
        onPostItemDotsMenuClick(post, currentMedia)
    }

    override fun onPostClicked(post: PostUIEntity, adapterPosition: Int) {
        gotoPostFragment(post, adapterPosition, needToShowRepostBtn)
    }

    override fun onCommentClicked(post: PostUIEntity, adapterPosition: Int) {
        gotoPostFragment(post, adapterPosition, needToShowRepostBtn)
    }

    override fun onRepostClicked(post: PostUIEntity) {
        requireActivity().vibrate()
        handleRepostClick(post)
    }

    override fun onTagClicked(
        clickType: SpanDataClickType,
        adapterPosition: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity?
    ) {
        handleTagClicked(clickType, adapterPosition, tagOrigin, post)
    }

    override fun onAvatarClicked(post: PostUIEntity, adapterPosition: Int) {
        handleAvatarClicked(post.user)
    }

    override fun onCommunityClicked(communityId: Long, adapterPosition: Int) {
        handleClickCommunityView(communityId)
    }

    override fun onHolidayWordClicked() {
        act?.showFireworkAnimation {}
    }

    override fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
    ) {
        val reactions = post.reactions ?: emptyList()
        val postOrigin = getAnalyticPostOriginEnum()
        val reactionSource = MeeraReactionSource.Post(
            postId = post.postId,
            reactionHolderViewId = reactionHolderViewId,
            originEnum = postOrigin
        )
        val actionBarType =
            MeeraContentActionBar.ContentActionBarType.getType(post.toMeeraContentActionBarParams())
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getMeeraReactionBubbleViewController()
            .showReactionBubble(
                reactionSource = reactionSource,
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = reactions,
                contentActionBarType = actionBarType,
                reactionsParams = reactionsParams,
                containerInfo = act.getDefaultReactionContainer(),
                postedAt = post.date
            )
    }

    override fun onReactionClickToShowScreenAnimation(
        reactionEntity: ReactionEntity,
        anchorViewLocation: Pair<Int, Int>
    ) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        reactionAnimationHelper?.playLottieAtPosition(
            recyclerView = feedRecycler,
            context = requireContext(),
            parent = getParentContainer(),
            reactionType = reactionType,
            x = anchorViewLocation.first.toFloat(),
            y = anchorViewLocation.second.toFloat()
        )
    }

    override fun onFindPeoplesClicked() {
        handleFindPeoplesClicked()
    }

    override fun onMediaClicked(post: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
        goToMultimediaPostViewFragment(post, mediaAsset, adapterPosition)
    }

    override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigate { feedViewModel.showReactionStatistics(post, ReactionsEntityType.POST) }
    }

    override fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean
    ) {
        val reactions = post.reactions ?: emptyList()
        val postOrigin = getAnalyticPostOriginEnum()
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getMeeraReactionBubbleViewController().onSelectDefaultReaction(
            reactionSource = MeeraReactionSource.Post(
                postId = post.postId,
                reactionHolderViewId = reactionHolderViewId,
                originEnum = postOrigin
            ),
            currentReactionsList = reactions,
            reactionsParams = reactionsParams,
            forceDefault = forceDefault
        )
    }

    override fun onShowMoreRepostClicked(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let { parentPost ->
            gotoPostFragment(parentPost, adapterPosition, !parentPost.isPrivateGroupPost)
            feedViewModel.logPressMoreText(
                postId = parentPost.postId,
                authorId = post.user?.userId ?: NO_USER_ID,
                where = requireNotNull(getAmplitudeWhereFromOpened()),
                postType = AmplitudePropertyPostType.REPOST,
                isPostDetailOpen = true,
            )
        }
    }

    override fun onShowMoreTextClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        isOpenPostDetail: Boolean
    ) {
        if (isOpenPostDetail) {
            gotoPostFragment(post, adapterPosition, !post.isPrivateGroupPost)
        } else {
            triggerPostsAction(FeedViewActions.OnShowMoreText(post))
        }

        getAmplitudeWhereFromOpened()?.let { whereFrom ->
            feedViewModel.logPressMoreText(
                postId = post.postId,
                authorId = post.user?.userId ?: NO_USER_ID,
                where = whereFrom,
                postType = AmplitudePropertyPostType.POST,
                isPostDetailOpen = isOpenPostDetail,
            )
        }
    }

    override fun onPressRepostHeader(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let {
            gotoPostFragment(it, adapterPosition, !it.isPrivateGroupPost)
        }
    }

    override fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigate {
            val isPostAuthor = post.user?.userId == feedViewModel.getUserUid()
            if (isPostAuthor) return@needAuthToNavigate
            val isSubscribed = post.user?.subscriptionOn ?: return@needAuthToNavigate
            if (isSubscribed.isTrue()) {
                showConfirmDialogUnsubscribeUser(
                    postId = post.postId,
                    userId = post.user.userId,
                    fromFollowButton = true,
                    isApproved = post.user.approved.toBoolean(),
                    topContentMaker = post.user.topContentMaker.toBoolean()
                )
            } else {
                triggerPostsAction(
                    FeedViewActions.SubscribeToUser(
                        postId = post.postId,
                        userId = post.user.userId,
                        needToHideFollowButton = false,
                        fromFollowButton = true,
                        isApproved = post.user.approved.toBoolean(),
                        topContentMaker = post.user.topContentMaker.toBoolean()
                    )
                )
            }
        }
    }

    override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
        val container = act?.getRootView() as? ViewGroup
        container?.addView(flyingReaction)
        flyingReaction.startAnimationFlying()
        flyingReaction.setFlyingAnimationPlayListener(object :
            FlyingAnimationPlayListener {
            override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                container?.removeView(playedFlyingReaction)
            }
        })
    }

    override fun onStopLoadingClicked(post: PostUIEntity) {
        feedViewModel.stopDownloadingPostVideo(postId = post.postId)
    }

    override fun onPictureClicked(post: PostUIEntity) {
        goToContentViewer(post)
    }

    override fun onVideoClicked(post: PostUIEntity, adapterPosition: Int) {
        goToVideoPostFragment(post, adapterPosition)
    }

    override fun onShowEventOnMapClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        if (isRepost) {
            gotoPostFragment(post, adapterPosition, true)
        } else {
            isOpeningEvent = true
            movedToEvent = true
            wasHideable = NavigationManager.getManager().getForceUpdatedTopBehavior()?.isHideable.isTrue()
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.isHideable = true
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.setHiddenState()
            NavigationManager.getManager().mainMapFragment.openEventFromAnotherScreen(post, true)
        }
    }

    override fun onNavigateToEventClicked(post: PostUIEntity) {
        val initUiModel = EventNavigationInitUiModel(
            event = post.event ?: return,
            authorId = post.user?.userId ?: return
        )
        MeeraEventNavigationBottomsheetDialogFragment.getInstance(initUiModel)
            .show(childFragmentManager, MeeraEventNavigationBottomsheetDialogFragment::class.java.name)

        feedViewModel.logMapEventGetTherePress(post)
    }

    override fun onShowEventParticipantsClicked(post: PostUIEntity) {
        findNavController().safeNavigate(
            resId = R.id.eventParticipantsListFragment,
            bundle = Bundle().apply {
                post.event?.let { event ->
                    putLong(
                        MeeraEventParticipantsListFragment.ARG_EVENT_ID, event.id
                    )
                    putLong(
                        MeeraEventParticipantsListFragment.ARG_POST_ID, post.postId
                    )
                    putInt(
                        MeeraEventParticipantsListFragment.ARG_PARTICIPANTS_COUNT,
                        event.participation.participantsCount
                    )
                }
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
                it
            }
        )
    }

    override fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {
        feedViewModel.onJoinAnimationFinished(
            postUIEntity = post,
            adapterPosition = adapterPosition
        )
    }

    override fun onJoinEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        if (isRepost) {
            gotoPostFragment(post, adapterPosition, true)
        } else {
            feedViewModel.joinEvent(post)
        }
    }

    override fun onLeaveEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        if (isRepost) {
            gotoPostFragment(post, adapterPosition, true)
        } else {
            feedViewModel.leaveEvent(post)
        }
    }

    override fun onShowUserMomentsClicked(
        userId: Long,
        fromView: View?,
        hasNewMoments: Boolean?
    ) {
        if (isMomentsDisabled()) {
            return
        }
        findNavController().navigate(
            R.id.action_global_meeraViewMomentFragment,
            bundleOf(
                KEY_USER_ID to userId
            )
        )
    }

    override fun onStartPlayingVideoRequested() {
        startVideoIfExists()
    }

    override fun forceStartPlayingVideoRequested() {
        startVideoIfExists()
    }

    override fun onStopPlayingVideoRequested() {
        stopVideoIfExists()
    }

    override fun onMediaExpandCheckRequested() {
        initExpandMedia()
    }

    override fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {
        feedViewModel.onTriggerAction(FeedViewActions.UpdatePostSelectedMediaPosition(postId, selectedMediaPosition))
    }

    private fun isMomentsDisabled(): Boolean =
        (activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false

    private fun showReactionStatistics(event: FeedViewEvent.ShowReactionStatisticsEvent) {
        MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
            event.post.postId,
            event.entityType
        ) { destination ->
            when (destination) {
                is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                    gotoUserProfileFragment(destination.userEntity.userId)
                }
            }
        }.show(childFragmentManager)

        val where = if (event.post.isEvent()) {
            AmplitudePropertyReactionWhere.MAP_EVENT
        } else {
            AmplitudePropertyReactionWhere.POST
        }
        feedViewModel.logStatisticReactionsTap(
            where = where,
            whence = getAnalyticPostOriginEnum().toAmplitudePropertyWhence()
        )
    }

    private fun goToMultimediaPostViewFragment(
        postItem: PostUIEntity,
        mediaAsset: MediaAssetEntity,
        adapterPosition: Int
    ) {
        var videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = feedRecycler,
            position = adapterPosition,
            post = postItem
        )
        videoData = videoData.copy(id = mediaAsset.id)
        stopVideoIfExists()

        findNavController().safeNavigate(
            resId = R.id.mediaNavGraph,
            bundle = Bundle().apply
            {
                putLong(ARG_VIEW_MULTIMEDIA_POST_ID, postItem.postId)
                putString(ARG_VIEW_MULTIMEDIA_ASSET_ID, mediaAsset.id)
                putString(ARG_VIEW_MULTIMEDIA_ASSET_TYPE, mediaAsset.type)
                putParcelable(ARG_VIEW_MULTIMEDIA_DATA, postItem)
                putSerializable(ARG_VIEW_MULTIMEDIA_VIDEO_DATA, videoData)
                putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType))
                putBoolean(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
            })
    }

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            findNavController().safeNavigate(
                R.id.meeraProfilePhotoViewerFragment, bundleOf(
                    IArgContainer.ARG_IS_PROFILE_PHOTO to false,
                    IArgContainer.ARG_IS_OWN_PROFILE to false,
                    IArgContainer.ARG_POST_ID to post.postId,
                    IArgContainer.ARG_GALLERY_ORIGIN to getAnalyticPostOriginEnum()
                )
            )

        } else {
//            TODO ROAD_FIX
//            add(
//                ViewContentFragment(),
//                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                Arg(ARG_VIEW_CONTENT_DATA, post),
//                Arg(IArgContainer.ARG_PHOTO_WHERE, getAmplitudeWhereFromOpened()),
//                Arg(IArgContainer.ARG_POST_ORIGIN, getAnalyticPostOriginEnum())
//            )
        }
    }

    //TODO закомментил на время перехода на MeeraBaseFeedFragment
    private fun goToVideoPostFragment(postItem: PostUIEntity, adapterPosition: Int) {
        val videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = feedRecycler,
            position = adapterPosition,
            post = postItem
        )
        stopVideoIfExists()
        findNavController().safeNavigate(
            resId = R.id.meeraViewVideoFragment,
            bundle = Bundle().apply
            {
                putLong(ARG_VIEW_VIDEO_POST_ID, postItem.postId)
                putParcelable(ARG_VIEW_VIDEO_POST, postItem)
                putSerializable(ARG_VIEW_VIDEO_DATA, videoData)
                putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType))
                putBoolean(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
            })
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    feedViewModel.downloadPostVideo(postId, assetId)
                }

                override fun onDenied() {
                    showCantDownloadToastMessage(postId, assetId)
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    private fun showCantDownloadToastMessage(postId: Long, assetId: String?) {
        undoSnackbar?.dismiss()
        undoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.meera_cant_to_save),
                    buttonActionText = getText(R.string.general_retry),
                    buttonActionListener = {
                        undoSnackbar?.dismiss()
                        feedViewModel.downloadPostVideo(postId, assetId)
                    }
                ),
                dismissOnClick = true
            )
        )
        undoSnackbar?.show()
    }

    private fun showConfirmDialogUnsubscribeUser(
        postId: Long?,
        userId: Long?,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_desc))
            .setLeftBtnText(getString(R.string.general_cancel))
            .setRightBtnText(getString(R.string.unsubscribe))
            .setRightClickListener {
                triggerPostsAction(
                    FeedViewActions.UnsubscribeFromUser(
                        postId = postId,
                        userId = userId,
                        fromFollowButton = fromFollowButton,
                        isApproved = isApproved,
                        topContentMaker = topContentMaker
                    )
                )
            }
            .show(childFragmentManager)
    }

    private fun handleAvatarClicked(user: UserPost?) {
        if (needToShowProfile) {
            user?.let { id ->
                gotoUserProfileFragment(
                    user.userId
                )
            }
        }
    }

    private fun handleFindPeoplesClicked() {
        needAuthToNavigate { openPeoples() }
    }

    private fun openPeoples() {
        findNavController().safeNavigate(R.id.peoplesFragment)
    }


    private fun handleTagClicked(
        clickType: SpanDataClickType,
        position: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity? = null
    ) {
        when (clickType) {
            is SpanDataClickType.ClickUserId -> gotoUserProfileFragment(clickType.userId)
            is SpanDataClickType.ClickUnknownUser -> {
                showToastMessage(
                    R.string.uniqname_unknown_profile_message,
                    messageState = AvatarUiState.ErrorIconState
                )
            }

            is SpanDataClickType.ClickHashtag -> handleHashtagClick(clickType.hashtag, post)
            is SpanDataClickType.ClickBadWord -> handleBadWord(clickType, position, tagOrigin, post)
            is SpanDataClickType.ClickLink -> act.emitDeeplinkCall(clickType.link)
            else -> Unit
        }
    }

    private fun handleBadWord(
        clickType: SpanDataClickType.ClickBadWord,
        position: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity?
    ) {
        val tagSpan = when (tagOrigin) {
            TagOrigin.POST_TEXT -> post?.tagSpan
            TagOrigin.POST_TITLE -> post?.event?.tagSpan
        }
        val originalText = tagSpan?.text ?: ""
        val badWord = clickType.badWord ?: ""
        tagSpan?.text = originalText.replaceRange(clickType.startIndex, clickType.endIndex, badWord)
        if (tagOrigin == TagOrigin.POST_TEXT && tagSpan?.showFullText == false) {
            val shortText = tagSpan.shortText ?: ""
            tagSpan.shortText =
                shortText.replaceRange(clickType.startIndex, clickType.endIndex, badWord)
        }
        tagSpan?.deleteSpanDataById(clickType.tagSpanId)
        tagSpan?.addSpanData(
            UniquenameSpanData(
                id = null,
                tag = null,
                type = UniquenameType.FONT_STYLE_ITALIC.value,
                startSpanPos = clickType.startIndex,
                endSpanPos = clickType.startIndex + badWord.length,
                userId = null,
                groupId = null,
                symbol = badWord
            )
        )
        val newItem = when (tagOrigin) {
            TagOrigin.POST_TEXT -> post?.copy(tagSpan = tagSpan)
            TagOrigin.POST_TITLE -> post?.copy(event = post.event?.copy(tagSpan = tagSpan))
        }
        newItem?.let {
            feedAdapter?.updateModel(position, it)
        }
    }

    override fun onResume() {
        super.onResume()
        blurHelper?.updateLifecycle(viewLifecycleOwner.lifecycle)
        subscribePostRx()

        onStartFragment()
    }

    override fun onStateChanged(newState: Int) {
        super.onStateChanged(newState)
        if (newState == BottomSheetBehavior.STATE_EXPANDED && movedToEvent) {
            movedToEvent = false
            NavigationManager.getManager().getForceUpdatedTopBehavior()?.isHideable = wasHideable
            wasHideable = false
        }
    }

    override fun onPause() {
        super.onPause()

        onStopFragment()
    }

    private fun subscribePostRx() {
        postDisposable = feedViewModel.postsObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handlePostViewEvents) { Timber.e(it) }
    }

    fun initPostsLiveObservable() {
        feedViewModel.livePosts.observe(viewLifecycleOwner, ::fetchAdapter)
        feedViewModel.liveEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)
        feedViewModel.editPostEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)

        // обновление через payloads
        feedViewModel.liveFeedEvents.observe(viewLifecycleOwner) {
            when (it) {
                is FeedViewEventPost.UpdatePostEvent -> feedAdapter?.updateItem(
                    it.post,
                    it.adapterPosition
                )

                is FeedViewEventPost.ShowMediaExpand -> initExpandMedia()
                is FeedViewEventPost.UpdateVolumeState -> handleUpdateVolumeState(it.volumeState)
                is FeedViewEventPost.UpdatePostValues -> handleUpdatePostValues(it.post)
                else -> Unit
            }
        }
    }

    private fun initExpandMedia() {
        feedRecycler?.post {
            feedRecycler?.expandMediaIndicatorAction(true, showInstantly = true)
        }
    }

    private fun handleUpdateVolumeState(volumeState: VolumeState) {
        feedAdapter?.updateVolumeState(volumeState = volumeState)

        if (volumeState == VolumeState.ON) _audioFeedHelper.stopPlaying()
    }

    private fun handleUpdatePostValues(uiPostUpdate: UIPostUpdate) {
        feedAdapter?.updateItem(uiPostUpdate)
    }

    private fun registerComplaintListener() {
        complainsNavigator.registerAdditionalActionListener(this) { result ->
            when {
                result.isSuccess -> showAdditionalStepsForComplain(result.getOrThrow())
                result.isFailure -> showToastMessage(
                    requireContext().string(R.string.user_complain_error),
                    messageState = AvatarUiState.ErrorIconState
                )
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    private fun initСomplainEventsObserver() {
        userComplainViewModel.complainEvents.onEach { event ->
            when (event) {
                is ComplainEvents.ComplainFailed -> {
                    showComplaintInfoSnackbar(R.string.user_complain_error)
                    userComplainViewModel.logAdditionalEvent(ComplainExtraActions.NONE)
                }

                is ComplainEvents.RequestModerators -> {
                    showComplaintInfoSnackbar(R.string.meera_user_complain_request_moderator)
                }

                else -> Unit
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showAdditionalStepsForComplain(userId: Long) {
        checkAppRedesigned(
            isRedesigned = {
                val dialog = MeeraComplaintDialog.newInstance(userId = userId)

                dialog.setComplaintCallback(callback = object : MeeraAdditionalComplainCallback {
                    override fun onSuccess(msg: Int?, reason: ComplainEvents, userId: Long?) {
                        complaintAction(msg, reason, userId)
                    }

                    override fun onError(msg: Int?) {
                        msg?.let { showComplaintInfoSnackbar(msg) }
                    }
                })
                dialog.show(childFragmentManager, "MeeraComplaintDialog")
            },
            isNotRedesigned = {
                val bottomSheet = UserComplainAdditionalBottomSheet.newInstance(userId).apply {
                    callback = object : AdditionalComplainCallback {
                        override fun onSuccess(msg: String?, reason: ComplainEvents) {
                            msg?.let { showToastMessage(it) }
                            onUserComplainAdditionalSuccess()
                        }

                        override fun onError(msg: String?) {
                            msg?.let { showToastMessage(it, messageState = AvatarUiState.ErrorIconState) }
                        }
                    }
                }
                bottomSheet.show(childFragmentManager, "UserComplainAdditionalBottomSheet")
            }
        )
    }

    open fun onUserComplainAdditionalSuccess() = Unit

    private fun complaintAction(msg: Int?, reason: ComplainEvents, userId: Long?) {
        when (reason) {
            ComplainEvents.UserBlocked -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg) {
                        userComplainViewModel.blockUser(userId, true)
                        userComplainViewModel.logAdditionalEvent(ComplainExtraActions.BLOCK)
                    }
                }
            }

            is ComplainEvents.MomentsHidden -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg) {
                        userComplainViewModel.hideUserMoments(userId)
                    }
                }
            }

            ComplainEvents.PostsDisabledEvents -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg) {
                        userComplainViewModel.hideUserRoad(userId)
                        userComplainViewModel.logAdditionalEvent(ComplainExtraActions.HIDE)
                    }
                }
            }

            ComplainEvents.RequestModerators -> {
                userComplainViewModel.hideUserRequestModerators()
            }

            else -> Unit
        }
    }

    private fun fetchAdapter(posts: List<PostUIEntity>) {
        feedAdapter?.submitList(posts)
        feedRecycler?.invalidateItemDecorations()
        feedRecycler?.postDelayed({ controlAlreadyPlayingVideo() }, ACTION_AFTER_SUBMIT_LIST_DELAY)
    }

    private fun showComplaintInfoSnackbar(msg: Int, timerFinishCallback: (() -> Unit)? = null) {
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(msg),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_NOTIFICATION_SEC,
                        onTimerFinished = {
                            timerFinishCallback?.invoke()
                            deleteNotificationState.clear()
                        }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        pendingDeleteSnackbar?.dismiss()
                    }
                )
            )
        )

        pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        pendingDeleteSnackbar?.show()
    }

    fun loadBasePosts() {
        loadPostsRequest(0, roadType)
    }

    fun initPostsLoadScrollListener() {
        _layoutManager?.let { layoutManager ->
            loadScrollListener = object : RecyclerPaginationListener(layoutManager) {
                override fun loadMoreItems() {
                    loadPostsRequest(getStartPostId(), roadType)
                }

                override fun isLastPage(): Boolean = feedViewModel.isLastPage

                override fun isLoading(): Boolean = feedViewModel.isLoading

                // Handle scroll animation button
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    observeScrollForRefreshButton(layoutManager, dy)
                }
            }.also { scrollListener ->
                feedRecycler?.addOnScrollListener(scrollListener)
            }
        }
    }

    private fun loadPostsRequest(startPostId: Long, roadType: NetworkRoadType) {
        when (roadType) {
            is NetworkRoadType.USER -> triggerPostsAction(
                FeedViewActions.GetUserPosts(
                    startPostId = startPostId,
                    userId = roadType.userId ?: 0L,
                    selectedPostId = roadType.selectedPostId
                )
            )

            is NetworkRoadType.COMMUNITY -> triggerPostsAction(
                FeedViewActions.GetGroupPosts(startPostId, roadType.groupId ?: 0)
            )

            is NetworkRoadType.HASHTAG -> triggerPostsAction(
                FeedViewActions.GetHashtagPosts(startPostId, getHashtag())
            )

            else -> Timber.e("Unknown road type")
        }
    }

    private fun triggerPostsAction(action: FeedViewActions) {
        feedViewModel.onTriggerAction(action)
    }

    private fun getStartPostId(): Long {
        val itemCount = feedAdapter?.itemCount ?: 0
        val position = max(itemCount - 1, 0)

        var item = feedAdapter?.getItem(position)
        if (item?.feedType == FeedType.POSTS_VIEWED_PROFILE_VIP || item?.feedType == FeedType.POSTS_VIEWED_PROFILE) {
            item = feedAdapter?.getItem(position - 1)
        }
        return item?.postId ?: 0L
    }

    private fun handlePostViewEvents(event: FeedViewEvent?) {
        when (event) {
            is FeedViewEvent.FailChangeLikeStatus -> showToastMessage(
                R.string.no_internet,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.ShowCommonError -> showToastMessage(
                event.messageResId,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.ShowErrorAndHideProgress -> {
                showToastMessage(event.messageResId, messageState = AvatarUiState.ErrorIconState)
            }

            is FeedViewEvent.ShowCommonSuccess -> showToastMessage(event.messageResId)
            is FeedViewEvent.OnSuccessHideUserRoad -> showToastMessage(getString(event.messageResId))
            is FeedViewEvent.TotalPostCount -> setTotalPostsCount(event.postCount)
            is FeedViewEvent.UpdatePostById -> feedAdapter?.updateItemByPostId(event.postId)
            is FeedViewEvent.LikeChangeVibration -> {
                requireContext().vibrate()
            }

            is FeedViewEvent.OnShowLoader -> feedAdapter?.showLoader(event.show)
            is FeedViewEvent.ShowCommonErrorString -> showToastMessage(
                event.message,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.EmptyFeed -> showEmptyFeedPlaceholder()
            is FeedViewEvent.CopyLinkEvent -> copyLink(event.link)
            is FeedViewEvent.UpdateEventPost -> feedAdapter?.updateItem(
                UIPostUpdate.UpdateEventPostParticipationState(
                    postId = event.post.postId,
                    postUIEntity = event.post
                )
            )

            is FeedViewEvent.ShowEventSharingSuggestion -> {
                meeraOpenRepostMenu(
                    post = event.post,
                    mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                )
            }

            is FeedViewEvent.PostEditAvailableEvent -> updateMenuWitEdit(
                postId = event.post.postId,
                isEditAvailable = event.isAvailable,
                currentMedia = event.currentMedia
            )

            is FeedViewEvent.OpenEditPostEvent -> navigateToEditPost(post = event.post)
            is FeedViewEvent.ShowAvailabilityError -> showNotAvailableError(event.reason)
            is FeedViewEvent.ShowReactionStatisticsEvent -> showReactionStatistics(event)
            is FeedViewEvent.OnFirstPageLoaded -> scrollUpCommunityPosts()
            is FeedViewEvent.LoadInitialPosts -> loadInitialPostAction()
            else -> Unit
        }
    }

    protected open fun loadInitialPostAction() = Unit

    protected open fun showEmptyFeedPlaceholder() {}

    private fun showToastMessage(
        @StringRes messageRes: Int,
        messageState: AvatarUiState = AvatarUiState.SuccessIconState
    ) {
        showToastMessage(getString(messageRes), messageState)
    }

    private fun showToastMessage(
        messageString: String,
        messageState: AvatarUiState = AvatarUiState.SuccessIconState
    ) = doOnUIThread {
        undoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = messageString,
                    avatarUiState = messageState,
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true
            )
        )
        undoSnackbar?.show()
    }

    private fun gotoPostFragment(
        postItem: PostUIEntity,
        adapterPosition: Int,
        isRepostAllowed: Boolean
    ) {
        val isVolumeEnabled = feedRecycler?.isVolumeEnabled() ?: false
        val videoPosition = VideoUtil.getVideoPosition(
            feedAdapter = feedAdapter,
            feedRecycler = feedRecycler,
            position = adapterPosition,
            post = postItem
        )

        val needToShowBlockBtn = this !is MeeraUserInfoFragment
        val args = Bundle().apply {
            putSerializable(ARG_IS_FROM_USER_ROAD, true)
            putSerializable(ARG_FEED_POST_ID, postItem.postId)
            putSerializable(ARG_TIME_MILLS, videoPosition)
            putSerializable(ARG_FEED_POST_POSITION, adapterPosition)
            putSerializable(ARG_FEED_ROAD_TYPE, MeeraBaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index)
            putSerializable(ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled)
            putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType))
            putSerializable(ARG_NEED_TO_REPOST, isRepostAllowed)
            putSerializable(ARG_NEED_TO_SHOW_HIDE_POSTS_BTN, needToShowBlockBtn)
            putParcelable(ARG_FEED_POST, postItem)
        }
        findNavController().safeNavigate(R.id.meeraPostFragmentV2, args)
    }

    private fun gotoUserProfileFragment(userId: Long?) {
        findNavController().safeNavigate(
            R.id.userInfoFragment, bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to getAmplitudeWhereProfileFromOpened()?.property
            )
        )
    }

    private fun handleHashtagClick(hashtag: String?, post: PostUIEntity?) {
        feedViewModel.logPressHashTag(
            requireNotNull(getWhereFromHashTagPressed()),
            post?.postId ?: 0,
            post?.getUserId() ?: 0
        )
        if (hashtag?.replace("#", "")
                .equals(getHashtag()?.replace("#", ""), ignoreCase = true)
        ) {
            scrollToTopWithoutRefresh()
        } else {
            needAuthToNavigate {
                findNavController().safeNavigate(
                    resId = R.id.action_global_meeraHashtagFragment,
                    bundle = bundleOf(IArgContainer.ARG_HASHTAG to hashtag)
                )
            }
        }
    }

    private fun handleRepostClick(postItem: PostUIEntity) = needAuthToNavigate {
        meeraOpenRepostMenu(postItem)
    }

    private fun onPostItemDotsMenuClick(post: PostUIEntity, currentMedia: MediaAssetEntity?) = needAuthToNavigate {
        triggerPostsAction(FeedViewActions.CheckUpdateAvailability(post, currentMedia))
    }

    private fun showPostDotsMenu(
        post: PostUIEntity,
        adapterPosition: Int,
        isEditAvailable: Boolean = false,
        currentMedia: MediaAssetEntity?
    ) = needAuthToNavigate {
        val postId = post.postId
        val selectedPostId: Long = post.postId
        val isPostAuthor = post.user?.userId == feedViewModel.getUserUid()
        val menu = MeeraMenuBottomSheet(context)
        val regularUser = communityUserRole == CommunityUserRole.REGULAR
        val postCreatorUid = post.user?.userId
        selectedUserId = postCreatorUid

        if (isEditAvailable) {
            menu.addItem(
                title = getString(R.string.general_edit),
                icon = R.drawable.ic_outlined_edit_m,
                bottomSeparatorVisible = true,
            ) {
                feedViewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.CHANGE,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(FeedViewActions.EditPost(post = post))
            }
        }

        addSavingMediaItemsToMenu(menu, postCreatorUid, currentMedia, post)

        if (!isPostAuthor) {
            val img = if (post.isPostSubscribed)
                R.drawable.ic_outlined_post_delete_m
            else R.drawable.ic_outlined_post_m
            val textRes = if (post.isPostSubscribed) {
                if (post.event != null) R.string.unsubscribe_event_post_txt else R.string.unsubscribe_post_txt
            } else {
                if (post.event != null) R.string.subscribe_event_post_txt else R.string.subscribe_post_txt
            }
            menu.addItem(
                title = textRes,
                icon = img,
                bottomSeparatorVisible = true
            ) {
                if (!post.isPostSubscribed) {
                    feedViewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_FOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(
                        FeedViewActions.SubscribeToPost(
                            selectedPostId,
                            PostSubscribeTitle.NotificationString()
                        )
                    )
                } else if (post.isPostSubscribed) {
                    feedViewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(
                        FeedViewActions.UnsubscribeFromPost(
                            selectedPostId,
                            PostSubscribeTitle.NotificationString()
                        )
                    )
                }
            }
        }

        post.user?.subscriptionOn?.let { isSubscribed ->
            if (isSubscribed.isFalse() && !isPostAuthor) {
                menu.addItem(
                    R.string.subscribe_user_txt,
                    R.drawable.ic_outlined_user_add_m,
                    bottomSeparatorVisible = true
                ) {
                    feedViewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.USER_FOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(
                        FeedViewActions.SubscribeToUser(
                            postId = post.postId,
                            userId = post.user.userId,
                            needToHideFollowButton = true,
                            fromFollowButton = false,
                            isApproved = post.user.approved.toBoolean(),
                            topContentMaker = post.user.topContentMaker.toBoolean()
                        )
                    )
                }
            }
        }

        if (!post.isPrivateGroupPost) {
            menu.addItem(
                title = R.string.general_share,
                icon = R.drawable.ic_outlined_repost_m,
                bottomSeparatorVisible = true
            ) {
                handleRepostClick(post)
            }
            menu.addItem(
                title = R.string.copy_link,
                icon = R.drawable.ic_outlined_copy_m,
                bottomSeparatorVisible = true
            ) {
                feedViewModel.onTriggerAction(FeedViewActions.CopyPostLink(post.postId))
            }
        }

        // Скрыть дорогу пользователя
        if (!isPostAuthor) {
            if (post.user?.subscriptionOn.toBoolean().not()) {
                menu.addItem(
                    title = R.string.profile_complain_hide_all_posts,
                    icon = R.drawable.ic_outlined_eye_off_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    feedViewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.HIDE_USER_POSTS,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.HideUserRoads(postCreatorUid))
                }
            }
            val complainTitleResId =
                if (post.event != null) R.string.complain_about_event_post else R.string.complain_about_post
            menu.addItem(
                complainTitleResId, R.drawable.ic_outlined_attention_m,
                iconAndTitleColor = R.color.uiKitColorAccentWrong
            ) {
                feedViewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.POST_REPORT,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(FeedViewActions.ComplainToPost(postId))
            }
        }


        // Удалить
        if (isPostAuthor || !regularUser) {
            menu.addItem(
                title = R.string.road_delete,
                icon = R.drawable.ic_outlined_delete_m,
                iconAndTitleColor = R.color.uiKitColorAccentWrong
            ) {
                feedViewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.DELETE,
                    authorId = postCreatorUid,
                )
                feedViewModel.logDeletedPost(
                    post.toPost().toAnalyticPost(),
                    requireNotNull(getAmplitudeWhereFromOpened())
                )
                triggerPostsAction(FeedViewActions.DeletePost(post, adapterPosition))
                resetCurrentPlayingHolderIfNeeded(adapterPosition)
                startVideoDelayed()
            }
        }

        dotsMenuPost = post
        menu.show(childFragmentManager)
    }

    private fun resetCurrentPlayingHolderIfNeeded(position: Int) {
        feedRecycler?.resetCurrentPlayingHolderIfNeeded(position)
    }

    private fun startVideoDelayed() {
        doDelayed(350) {
            feedRecycler?.playVideo(false)
        }
    }

    private fun addSavingMediaItemsToMenu(
        menu: MeeraMenuBottomSheet,
        postCreatorUid: Long?,
        currentMedia: MediaAssetEntity?,
        post: PostUIEntity
    ) {
        val image: String?
        val video: String?
        var assetId: String? = null

        when {
            currentMedia != null -> {
                image = currentMedia.image
                video = currentMedia.video
                assetId = currentMedia.id
            }

            else -> {
                image = post.getImageUrl()
                video = post.getVideoUrl()
            }
        }

        when {
            !image.isNullOrEmpty() -> addImageItemToMenu(menu, post, postCreatorUid, image)
            !video.isNullOrEmpty() -> addVideoItemToMenu(menu, post, postCreatorUid, assetId)
        }
    }

    private fun addImageItemToMenu(
        menu: MeeraMenuBottomSheet,
        post: PostUIEntity,
        postCreatorUid: Long?,
        image: String?
    ) {
        if (image.isNullOrEmpty()) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_outlined_download_m,
            bottomSeparatorVisible = true
        ) {
            feedViewModel.logPostMenuAction(
                post = post,
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = postCreatorUid,
                saveType = AmplitudePropertySaveType.PHOTO
            )
            saveImageOrVideoFile(
                imageUrl = image,
                act = act,
                viewLifecycleOwner = viewLifecycleOwner,
                successListener = {
                    showToastMessage(getString(R.string.image_saved))
                }
            )
        }
    }

    private fun addVideoItemToMenu(
        menu: MeeraMenuBottomSheet,
        post: PostUIEntity,
        postCreatorUid: Long?,
        mediaId: String? = null
    ) {
        val savingVideoIsAvailable = (requireActivity().application as App).remoteConfigs.postVideoSaving
        if (!savingVideoIsAvailable) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_outlined_download_m,
            bottomSeparatorVisible = true
        ) {
            feedViewModel.logPostMenuAction(
                post = post,
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = postCreatorUid,
                saveType = AmplitudePropertySaveType.VIDEO
            )
            saveVideo(post.postId, mediaId)
        }
    }

    protected open fun onNewPost() {}

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (NavigationManager.getManager().isMapMode) return
        MeeraShareSheet().show(
            fm = childFragmentManager,
            data = MeeraShareBottomSheetData(
                groupId = getCommunityId(),
                post = post.toPost(),
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        openGroups()
                    }

                    override fun onShareFindFriend() {
                        openSearch()
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        feedViewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        feedViewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_own_road))
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        feedViewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showToastMessage(getString(strResId))
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        gotoUserProfileFragment(userId)
                    }
                }
            )
        )
    }

    private fun openGroups() {
        findNavController().safeNavigate(R.id.communities_nav_graph)
    }

    private fun openSearch() {
        findNavController().safeNavigate(
            resId = R.id.searchNavGraph,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.SHARE
                )
            }
        )
    }

    private fun updateMenuWitEdit(postId: Long, isEditAvailable: Boolean, currentMedia: MediaAssetEntity?) {
        val adapter = feedAdapter ?: return
        val position = adapter.getPositionById(postId)
        val post = adapter.getItem(position) ?: return
        showPostDotsMenu(
            post = post,
            adapterPosition = position,
            isEditAvailable = isEditAvailable,
            currentMedia = currentMedia
        )
    }

    private fun navigateToEditPost(post: PostUIEntity?) {
        navigateEditPostFragment(post)
    }

    private fun handleClickCommunityView(groupId: Long?) {
        groupId?.let { id: Long ->
            findNavController().safeNavigate(
                resId = R.id.meeraCommunityRoadFragmentMainFlow,
                bundle = Bundle().apply {
                    putInt(IArgContainer.ARG_GROUP_ID, id.toInt())
                }
            )
        }
    }

    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            showToastMessage(R.string.copy_link_success)
        }
    }

    // Call from onStartFragment() / onReturnTransitionFragment()
    private fun startVideoIfExists() {
        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = feedViewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            feedAdapter.updateItem(postUpdate)
        }

        feedRecycler?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    private fun controlAlreadyPlayingVideo() = feedRecycler?.onStopIfNeeded()

    private fun observeScrollForRefreshButton(
        layoutManager: LinearLayoutManager?,
        dY: Int
    ) {
        val firstItemVisiblePosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
        // Handle scroll Lottie button
        if (dY < 0 && firstItemVisiblePosition !in 0..3) {
            if (firstItemVisiblePosition != -1) {
                handleScrollRefreshButton(true)
            }
        } else {
            handleScrollRefreshButton(false)
        }
    }

    private fun handleScrollRefreshButton(
        isVisible: Boolean
    ) {
        isVisibleScrollRefresh = if (isVisible) {
            if (!isVisibleScrollRefresh) {
                showRefreshTopButton()
            }
            true
        } else {
            if (isVisibleScrollRefresh) {
                hideRefreshTopButton()
                isPushScrollRefresh = false
            }
            false
        }
    }

    protected open fun showRefreshTopButton() {
        val refreshTopButton = getRefreshTopButtonView() ?: return

        if (isRefreshButtonShown) {
            return
        }

        isRefreshButtonShown = true

        (refreshTopButton as? UiKitButton?)?.buttonType = ButtonType.ELEVATED
        refreshTopButton.visible()

        refreshTopButton.animateAlpha(
            from = REFRESH_ALPHA_INVISIBLE,
            to = REFRESH_ALPHA_VISIBLE,
            duration = REFRESH_VISIBLE_ANIMATION_DURATION
        )
    }

    protected open fun hideRefreshTopButton() {
        val refreshTopButton = getRefreshTopButtonView() ?: return

        if (isRefreshButtonShown.not()) {
            return
        }

        isRefreshButtonShown = false

        refreshTopButton.let { button ->
            if (button.isVisible.not()) {
                return
            }

            refreshTopButton.animateAlpha(
                from = REFRESH_ALPHA_VISIBLE,
                to = REFRESH_ALPHA_INVISIBLE,
                duration = REFRESH_VISIBLE_ANIMATION_DURATION,
                onAnimationEnd = { refreshTopButton.gone() }
            )
        }
    }

    private fun showNotAvailableError(reason: NotAvailableReasonUiEntity) {
        when (reason) {
            NotAvailableReasonUiEntity.POST_NOT_FOUND -> showToastMessage(
                getString(R.string.post_edit_error_not_found_message),
                messageState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.USER_NOT_CREATOR -> showToastMessage(
                getString(R.string.post_edit_error_not_creator_message),
                messageState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.POST_DELETED -> showToastMessage(
                getString(R.string.post_edit_error_deleted_message),
                messageState = AvatarUiState.WarningIconState
            )

            NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE,
            NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER -> {
                MeeraConfirmDialogBuilder()
                    .setHeader(getString(R.string.post_edit_error_expired_title))
                    .setDescription(getString(R.string.post_edit_error_expired_description))
                    .setTopBtnText(getString(R.string.i_have_read))
                    .setTopBtnType(ButtonType.FILLED)
                    .hideBottomBtn()
                    .show(childFragmentManager)
            }
        }
    }

    private fun handleClickScrollRefreshButton(scrollToTopView: View?) {
        scrollToTopView?.setOnClickListener {
            isPushScrollRefresh = true
            hideRefreshTopButton()
            onClickScrollUpButton()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment lifecycle callbacks
    ///////////////////////////////////////////////////////////////////////////

    private fun onStartFragment() {
        startVideoIfExists()
        feedViewModel.logScreenForFragment(this.javaClass.simpleName)
        feedRecycler?.scrollEnabled = true
        registerComplaintListener()
        resetLastVideoPlaybackPosition()
    }

    private fun onStopFragment() {
        stopVideoIfExists()
        _audioFeedHelper.stopPlaying(isLifecycleStop = true)
        feedRecycler?.scrollEnabled = false
        unregisterComplaintListener()
    }

    override fun onStart() {
        super.onStart()
        _audioFeedHelper.addAudioPriorityListener(priorityListener)
    }

    override fun onDestroyView() {
        reactionAnimationHelper?.clearData()
        reactionAnimationHelper = null
        featureTogglesContainer = null
        formatterProvider = null
        sensitiveContentManager = null
        feedAdapter.onDestroyView()
        clearFeedRecycler()
        cacheUtil = null
        zoomyProvider = null
        dotsMenuPost = null
        blurHelper?.cancel()
        blurHelper = null
        infoTooltip = null
        dotsMenuPost = null
        postDisposable?.dispose()
        feedViewModel.livePosts.removeObservers(viewLifecycleOwner)
        postCollisionDetector?.release()
        postCollisionDetector = null
        scrollFirstPageJob?.cancel()
        scrollFirstPageJob = null
        (activity as? MeeraAct)?.getMeeraStatusToastViewController()?.hideStatusToast()

        super.onDestroyView()
    }

    private fun clearFeedRecycler() {
        recyclerDecoration?.let {
            feedRecycler?.removeItemDecoration(it)
            recyclerDecoration = null
        }

        loadScrollListener?.let {
            feedRecycler?.removeOnScrollListener(it)
            loadScrollListener = null
        }

        feedRecycler?.release()
        feedRecycler?.layoutManager = null
        _layoutManager = null
        feedRecycler?.adapter = null
        feedRecycler = null
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExists()
        infoTooltip?.dismiss()
        infoTooltip = null
        _audioFeedHelper.stopPlaying(isLifecycleStop = true)
        _audioFeedHelper.removeAudioPriorityListener(priorityListener)
        postDisposable?.dispose()
        feedViewModel.clearEvents()
        feedViewModel.subjectPosts.cleanupBuffer()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
