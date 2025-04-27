package com.numplates.nomera3.modules.feed.ui.fragment

import android.animation.Animator
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.string
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.noomeera.nmravatarssdk.ui.positiveButton
import com.numplates.nomera3.ACTION_AFTER_SUBMIT_LIST_DELAY
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAnalyticPost
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.ExtraLinearLayoutManager
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.FeedAdapter
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
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
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.PostSubscribeTitle
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_ASSET_TYPE
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_POST_ID
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_VIEW_MULTIMEDIA_VIDEO_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaFragment
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ARG_VIEW_CONTENT_DATA
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ViewContentFragment
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventNavigation
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventOnMap
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventParticipantsList
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.SAVING_PICTURE_DELAY
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.MeeraAdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.MeeraComplaintDialog
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_DATA
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_POST_ID
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoItemFragment
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_DEFAULT_VOLUME_ENABLED
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_POSITION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_ROAD_TYPE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_HASHTAG
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_FROM_USER_ROAD
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_SHOW_HIDE_POSTS_BTN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PHOTO_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TIME_MILLS
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.ui.FeedRecyclerView
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.math.max

private const val DELAY_DELETE_NOTIFICATION_SEC = 4L

/**
 * Базовый класс для всех постов, загружаемых только по сети
 * без кэштрования в БД
 */
abstract class BaseFeedFragment<T : ViewBinding> :
    BaseFragmentNew<T>(),
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl() {

    private var postDisposable: Disposable? = null
    private var feedAdapter: FeedAdapter? = null
    private val deleteNotificationState = linkedMapOf<String, NotificationCellUiModel>()
    private var pendingDeleteSnackbar: UiKitSnackBar? = null

    companion object {
        // Road types
        const val REQUEST_ROAD_TYPE_ALL = 0
        const val REQUEST_ROAD_TYPE_USER = 1
        const val REQUEST_ROAD_TYPE_GROUP = 2
        const val REQUEST_ROAD_TYPE_SUBSCRIPTIONS = 5
    }

    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    protected val feedViewModel by viewModels<FeedViewModel> { App.component.getViewModelFactory() }

    private var divider: PostDividerDecoration? = null

    private lateinit var _layoutManager: ExtraLinearLayoutManager

    // scroll refresh button
    private var isVisibleScrollRefresh = false
    private var isPushScrollRefresh = false
    private var selectedUserId: Long? = 0
    protected var isSavingFeedPhoto = false

    private var needToShowRepostBtn = true

    var scrollListener: RecyclerPaginationListener? = null
    private var infoTooltip: NSnackbar? = null

    private lateinit var roadType: NetworkRoadType

    protected var feedRecycler: FeedRecyclerView? = null

    private var lottieAnimation: LottieAnimationView? = null

    protected var communityUserRole = CommunityUserRole.REGULAR

    abstract fun onClickScrollUpButton()

    // нужно ли открывать профиль при нажатии на аватар и на ник в посте
    abstract val needToShowProfile: Boolean

    private val _audioFeedHelper: AudioFeedHelper
        get() = feedViewModel.getAudioHelper()

    private val priorityListener: () -> Unit = {
        feedRecycler?.turnOffAudioOfVideo()
    }

    private var postCollisionDetector: PostCollisionDetector? = null

    private var dotsMenuPost: PostUIEntity? = null

    open fun scrollToTopWithoutRefresh() {}

    open fun getHashtag(): String? = null

    open fun setTotalPostsCount(count: Long) {}

    abstract fun getAnalyticPostOriginEnum(): DestinationOriginEnum?

    fun getAdapterPosts() = feedAdapter

    open fun getCommunityId() = -1L

    open fun getWhereCommunityOpened() = AmplitudePropertyWhereCommunityOpen.FEED

    open fun getWhereFromHashTagPressed(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereFromOpened(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereProfileFromOpened(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereMomentOpened(): AmplitudePropertyMomentScreenOpenWhere =
        AmplitudePropertyMomentScreenOpenWhere.OTHER

    abstract fun getFormatter(): AllRemoteStyleFormatter

    abstract fun getPostViewRoadSource(): PostViewRoadSource

    fun resetAllZoomViews() {
        feedRecycler?.apply {
            val viewHolders = ArrayList<RecyclerView.ViewHolder>()
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                viewHolders.add(getChildViewHolder(view))
            }

            viewHolders.forEach { viewHolder -> when (viewHolder) { is BasePostHolder -> viewHolder.endZoom() } }
        }
    }

    fun resetLastPostMediaViewInfo() {
        triggerPostsAction(FeedViewActions.SaveLastPostMediaViewInfo(null))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
    }

    override fun onCancelByUser(menuTag: String?) {
        feedViewModel.logPostMenuAction(
            post = dotsMenuPost ?: return,
            action = AmplitudePropertyMenuAction.CANCEL,
            authorId = selectedUserId,
        )
        dotsMenuPost = null
    }

    private fun initViewModel() {
        val type = when (roadType) {
            NetworkRoadType.ALL -> RoadTypesEnum.MAIN
            is NetworkRoadType.COMMUNITY -> RoadTypesEnum.COMMUNITY
            NetworkRoadType.HASHTAG -> RoadTypesEnum.HASHTAG
            NetworkRoadType.SUBSCRIPTIONS -> RoadTypesEnum.SUBSCRIPTION
            is NetworkRoadType.USER -> RoadTypesEnum.PERSONAL
        }
        val origin = DestinationOriginEnum.fromNetworkRoadType(roadType)
        feedViewModel.initRoadType(type = type, originEnum = origin)
    }

    fun initPostsAdapter(
        roadType: NetworkRoadType,
        recyclerView: FeedRecyclerView?,
        lottieAnimationView: LottieAnimationView? = null
    ) {

        context?.let {
            this._layoutManager = ExtraLinearLayoutManager(it)
        } ?: kotlin.run {
            this._layoutManager = ExtraLinearLayoutManager(act)
        }
        this.roadType = roadType
        initViewModel()
        this.feedRecycler = recyclerView
        this.feedRecycler?.audioFeedHelper = _audioFeedHelper
        this.lottieAnimation = lottieAnimationView

        initPostViewCollisionDetector(recyclerView)


        initAdapter()

        divider?.let { divider ->
            this.feedRecycler?.removeItemDecoration(divider)
        }

        PostDividerDecoration.build(requireContext()).let { newDivider ->
            divider = newDivider
            this.feedRecycler?.addItemDecoration(newDivider)
        }
        handleClickScrollRefreshButton(lottieAnimationView)

        // Config specific road
        when (roadType) {
            is NetworkRoadType.COMMUNITY -> {
                needToShowRepostBtn = !roadType.isPrivateGroup
                feedAdapter?.isNeedToShowRepostBtn = needToShowRepostBtn
            }

            else -> Unit
        }
    }

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

    // usedForTest
    private fun initAdapter() {
        context?.let { cntx ->
            val blurHelper = BlurHelper(context = cntx, lifecycle = viewLifecycleOwner.lifecycle)
            val sensitiveContentManager = getSensitiveContentManager()

            val postCallback = object : PostCallback {

                override fun onDotsMenuClicked(post: PostUIEntity, adapterPosition: Int, currentMedia: MediaAssetEntity?) {
                    requireActivity().vibrate()
                    onPostItemDotsMenuClick(post = post, currentMedia = currentMedia)
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
                    reactionHolderViewId: ContentActionBar.ReactionHolderViewId
                ) {
                    val reactions = post.reactions ?: emptyList()
                    val postOrigin = getAnalyticPostOriginEnum()
                    val reactionSource = ReactionSource.Post(
                        postId = post.postId,
                        reactionHolderViewId = reactionHolderViewId,
                        originEnum = postOrigin
                    )
                    val actionBarType = ContentActionBar.ContentActionBarType.getType(post.toContentActionBarParams())
                    val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
                    act.getReactionBubbleViewController()
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

                override fun onFindPeoplesClicked() {
                    handleFindPeoplesClicked()
                }

                override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) = needAuth {
                    if (feedViewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                        checkAppRedesigned(
                            isRedesigned = {
                                MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                                    entityId = post.postId,
                                    entityType = ReactionsEntityType.POST
                                ).show(childFragmentManager)
                            },
                            isNotRedesigned = {
                                ReactionsStatisticsBottomSheetFragment.getInstance(
                                    entityId = post.postId,
                                    entityType = ReactionsEntityType.POST
                                ).show(childFragmentManager)
                            }
                        )

                    } else {
                        val reactions = post.reactions ?: return@needAuth
                        val menu = ReactionsStatisticBottomMenu(context)

                        menu.addTitle(R.string.reactions_on_post, reactions.reactionCount())
                        reactions.forEachIndexed { index, value ->
                            menu.addReaction(value, index != reactions.size - 1)
                        }
                        menu.show(childFragmentManager)
                    }
                    val where = if (post.isEvent()) {
                        AmplitudePropertyReactionWhere.MAP_EVENT
                    } else {
                        AmplitudePropertyReactionWhere.POST
                    }
                    feedViewModel.logStatisticReactionsTap(
                        where = where,
                        whence = getAnalyticPostOriginEnum().toAmplitudePropertyWhence()
                    )
                }

                override fun onReactionRegularClicked(
                    post: PostUIEntity,
                    adapterPosition: Int,
                    reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
                    forceDefault: Boolean
                ) {
                    val reactions = post.reactions ?: emptyList()
                    val postOrigin = getAnalyticPostOriginEnum()
                    val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
                    act.getReactionBubbleViewController().onSelectDefaultReaction(
                        reactionSource = ReactionSource.Post(
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
                    needAuth {
                        val isPostAuthor = post.user?.userId == feedViewModel.getUserUid()
                        if (isPostAuthor) return@needAuth
                        val isSubscribed = post.user?.subscriptionOn ?: return@needAuth
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
                    flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
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

                override fun onMediaClicked(post: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
                    goToMultimediaPostViewFragment(post, mediaAsset, adapterPosition)
                }

                override fun onShowEventOnMapClicked(post: PostUIEntity) = openEventOnMap(post)

                override fun onNavigateToEventClicked(post: PostUIEntity) {
                    openEventNavigation(post)
                    feedViewModel.logMapEventGetTherePress(post)
                }

                override fun onShowEventParticipantsClicked(post: PostUIEntity) = openEventParticipantsList(post)

                override fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {
                    feedViewModel.onJoinAnimationFinished(
                        postUIEntity = post,
                        adapterPosition = adapterPosition
                    )
                }

                override fun onJoinEventClicked(post: PostUIEntity) {
                    feedViewModel.joinEvent(post)
                }

                override fun onLeaveEventClicked(post: PostUIEntity) {
                    feedViewModel.leaveEvent(post)
                }

                override fun onShowUserMomentsClicked(
                    userId: Long,
                    fromView: View?,
                    hasNewMoments: Boolean?
                ) {
                    if(isMomentsDisabled()) {
                        return
                    }
                    act.openUserMoments(
                        userId = userId,
                        fromView = fromView,
                        openedWhere = getAmplitudeWhereMomentOpened(),
                        viewedEarly = hasNewMoments?.not()
                        )
                }

                override fun onStartPlayingVideoRequested() {
                    if (isFragmentStarted.not() || isFragmentAdding) return
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
            }
            val zoomyProvider = Zoomy.ZoomyProvider { Zoomy.Builder(act) }
            val momentCallback = object : MomentCallback {
                override fun requestNewMomentsPage(pagingTicket: String?) =
                    feedViewModel.getMomentDelegate().requestMomentsPage(pagingTicket = pagingTicket)

                override fun onMomentsCarouselBecomeNotVisible() = Unit

                override fun isRequestingMoments(): Boolean = feedViewModel.getMomentDelegate().isUpdatingMoments()

                override fun isMomentsCarouselLastPage(): Boolean = feedViewModel.getMomentDelegate().isMomentsLastPage()

                override fun isMomentPagingListTicketValid(ticket: String?): Boolean =
                    feedViewModel.getMomentDelegate().isMomentPagingListTicketValid(ticket)

                override fun onMomentTapCreate(entryPoint: AmplitudePropertyMomentEntryPoint) {
                    feedViewModel.logMomentTapCreate(entryPoint)
                }
            }
            val volumeStateCallback = object : VolumeStateCallback {
                override fun setVolumeState(volumeState: VolumeState) {
                    feedViewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
                }

                override fun getVolumeState() = feedViewModel.getVolumeState()
            }
            feedRecycler?.setVolumeStateCallback(volumeStateCallback)
            feedAdapter = FeedAdapter(
                blurHelper = blurHelper,
                contentManager = sensitiveContentManager,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                zoomyProvider = zoomyProvider,
                cacheUtil = CacheUtil(requireContext()),
                audioFeedHelper = _audioFeedHelper,
                formatter = getFormatter(),
                lifecycleOwner = viewLifecycleOwner,
                needToShowCommunityLabel = roadType !is NetworkRoadType.COMMUNITY,
                featureTogglesContainer = feedViewModel.getFeatureTogglesContainer(),
                momentCallback = momentCallback
            )
        }
    }

    private fun isMomentsDisabled(): Boolean =
        (activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            checkAppRedesigned(
                isRedesigned = {
//                    add(
//                        MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
//                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, false),
//                        Arg(IArgContainer.ARG_POST_ID, post.postId),
//                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, getAnalyticPostOriginEnum())
//                    )
                },
                isNotRedesigned = {
                    add(
                        ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                        Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
                        Arg(IArgContainer.ARG_IS_OWN_PROFILE, false),
                        Arg(IArgContainer.ARG_POST_ID, post.postId),
                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, getAnalyticPostOriginEnum())
                    )
                }
            )

        } else {
            add(
                ViewContentFragment(),
                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                Arg(ARG_VIEW_CONTENT_DATA, post),
                Arg(ARG_PHOTO_WHERE, getAmplitudeWhereFromOpened()),
                Arg(ARG_POST_ORIGIN, getAnalyticPostOriginEnum())
            )
        }
    }

    private fun goToVideoPostFragment(postItem: PostUIEntity, adapterPosition: Int) {
        val videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = feedRecycler,
            position = adapterPosition,
            post = postItem
        )
        val isVolumeEnabled = feedRecycler?.isVolumeEnabled() ?: false
        stopVideoIfExists()
        add(
            ViewVideoItemFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_VIDEO_POST_ID, postItem.postId),
            Arg(ARG_VIEW_VIDEO_POST, postItem),
            Arg(ARG_VIEW_VIDEO_DATA, videoData),
            Arg(ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType)),
            Arg(ARG_NEED_TO_REPOST, needToShowRepostBtn)
        )
    }

    private fun goToMultimediaPostViewFragment(postItem: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
        var videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = feedRecycler,
            position = adapterPosition,
            post = postItem
        )
        videoData = videoData.copy(id =  mediaAsset.id)
        stopVideoIfExists()
        add(
            ViewMultimediaFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_MULTIMEDIA_POST_ID, postItem.postId),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_ID, mediaAsset.id),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_TYPE, mediaAsset.type),
            Arg(ARG_VIEW_MULTIMEDIA_DATA, postItem),
            Arg(ARG_VIEW_MULTIMEDIA_VIDEO_DATA, videoData),
            Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType)),
            Arg(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
        )
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    feedViewModel.downloadPostVideo(postId, assetId)
                }

                override fun onDenied() {
                    NToast.with(act)
                        .text(act.getString(R.string.you_must_grant_permissions))
                        .durationLong()
                        .button(act.getString(R.string.general_retry)) {
                            feedViewModel.downloadPostVideo(postId, assetId)
                        }.show()
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
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
        needAuth {
            add(
                PeoplesFragment(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_SHOW_SWITCHER, false),
            )
        }
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
                NToast.with(view)
                    .text(context?.string(R.string.uniqname_unknown_profile_message))
                    .show()
            }

            is SpanDataClickType.ClickHashtag -> handleHashtagClick(clickType.hashtag, post)
            is SpanDataClickType.ClickBadWord -> handleBadWord(clickType, position, tagOrigin, post)
            is SpanDataClickType.ClickLink -> act.openLink(clickType.link)
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
            val postUpdate = UIPostUpdate.UpdateTagSpan(postId = it.postId, post = it)
            feedAdapter?.updateItem(adapterPos = position, payload = postUpdate)
        }
    }

    override fun onResume() {
        super.onResume()
        subscribePostRx()
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
                result.isFailure -> showTextError(requireContext().string(R.string.user_complain_error))
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    private fun showAdditionalStepsForComplain(userId: Long) {
        checkAppRedesigned(
            isRedesigned = {
                val dialog = MeeraComplaintDialog.newInstance(userId = userId)

                dialog.setComplaintCallback(callback = object : MeeraAdditionalComplainCallback {
                    override fun onSuccess(msg: Int?, reason: ComplainEvents, userId: Long?) {
                        msg?.let { showComplaintInfoSnackbar(msg) }
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
                            msg?.let { defaultSuccessMessage(it) }
                            onUserComplainAdditionalSuccess()
                        }
                        override fun onError(msg: String?) {
                            msg?.let { showTextError(it) }
                        }
                    }
                }
                bottomSheet.show(childFragmentManager, "UserComplainAdditionalBottomSheet")
            }
        )
    }

    open fun onUserComplainAdditionalSuccess() = Unit

    private fun fetchAdapter(posts: List<PostUIEntity>) {
        feedAdapter?.submitList(posts)
        feedRecycler?.invalidateItemDecorations()
        feedRecycler?.postDelayed({ controlAlreadyPlayingVideo() }, ACTION_AFTER_SUBMIT_LIST_DELAY)
    }

    private fun showComplaintInfoSnackbar(msg: Int) {
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(msg),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_NOTIFICATION_SEC,
                        onTimerFinished = {
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

    fun startLoadPosts() {
        feedRecycler?.layoutManager = _layoutManager

        var scrollListener: RecyclerPaginationListener? = null
        // Clear all items and listeners if exists
        scrollListener?.let {
            feedRecycler?.removeOnScrollListener(it)
        }

        loadPostsRequest(0, roadType)
        scrollListener = object : RecyclerPaginationListener(_layoutManager) {
            override fun loadMoreItems() {
                loadPostsRequest(getStartPostId(), roadType)
            }

            override fun isLastPage(): Boolean = feedViewModel.isLastPage

            override fun isLoading(): Boolean = feedViewModel.isLoading

            // Handle scroll animation button
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                observeScrollForRefreshButton(
                    _layoutManager, lottieAnimation, dy
                )
            }
        }
        scrollListener.let { feedRecycler?.addOnScrollListener(it) }

    }

    private fun loadPostsRequest(startPostId: Long, roadType: NetworkRoadType) {
        when (roadType) {
            is NetworkRoadType.USER -> triggerPostsAction(
                FeedViewActions
                    .GetUserPosts(
                        startPostId = startPostId,
                        userId = roadType.userId ?: 0L,
                        selectedPostId = roadType.selectedPostId
                    )
            )

            is NetworkRoadType.COMMUNITY -> triggerPostsAction(
                FeedViewActions
                    .GetGroupPosts(startPostId, roadType.groupId ?: 0)
            )

            is NetworkRoadType.HASHTAG -> triggerPostsAction(
                FeedViewActions
                    .GetHashtagPosts(startPostId, getHashtag())
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

    private fun getSensitiveContentManager(): ISensitiveContentManager =
        object : ISensitiveContentManager {
            override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
                return feedViewModel.isMarkedAsSensitivePost(postId)
            }

            override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
                feedViewModel.markPostAsNotSensitiveForUser(postId, parentPostId)
                feedViewModel.refreshPost(postId)
                feedViewModel.refreshPost(parentPostId)
            }
        }

    private fun handlePostViewEvents(event: FeedViewEvent) {
        when (event) {
            is FeedViewEvent.FailChangeLikeStatus -> showCommonError()
            is FeedViewEvent.ShowCommonError -> defaultErrorMessage(event.messageResId)
            is FeedViewEvent.ShowErrorAndHideProgress -> {
                defaultErrorMessage(event.messageResId)
            }

            is FeedViewEvent.ShowCommonSuccess -> defaultSuccessMessage(event.messageResId)
            is FeedViewEvent.TotalPostCount -> setTotalPostsCount(event.postCount)
            is FeedViewEvent.UpdatePostById -> feedAdapter?.updateItemByPostId(event.postId)
            is FeedViewEvent.LikeChangeVibration -> {
                requireContext().vibrate()
            }
            is FeedViewEvent.OnShowLoader -> feedAdapter?.showLoader(event.show)
            is FeedViewEvent.ShowCommonErrorString -> defaultErrorMessage(event.message)
            is FeedViewEvent.EmptyFeed -> showEmptyFeedPlaceholder()
            is FeedViewEvent.CopyLinkEvent -> copyLink(event.link)
            is FeedViewEvent.UpdateEventPost -> feedAdapter?.updateItem(
                UIPostUpdate.UpdateEventPostParticipationState(postId = event.post.postId, postUIEntity = event.post)
            )

            is FeedViewEvent.ShowEventSharingSuggestion -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraOpenRepostMenu(
                            post = event.post,
                            mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                        )
                    },
                    isNotRedesigned = {
                        openRepostMenu(
                            post = event.post,
                            mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                        )
                    }
                )
            }
            is FeedViewEvent.PostEditAvailableEvent -> updateMenuWitEdit(
                postId = event.post.postId,
                isEditAvailable = event.isAvailable,
                currentMedia = event.currentMedia
            )
            is FeedViewEvent.OpenEditPostEvent -> navigateToEditPost(post = event.post)
            is FeedViewEvent.ShowAvailabilityError -> showNotAvailableError(event.reason)
            else -> Unit
        }
    }

    protected open fun showEmptyFeedPlaceholder() {}

    private fun showTextError(message: String) {
        view?.let {
            NToast.with(it)
                .text(message)
                .typeError()
                .show()
        }
    }

    private fun defaultSuccessMessage(stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeSuccess()
            .show()
    }

    private fun defaultSuccessMessage(msg: String) {
        NToast.with(act)
            .text(msg)
            .typeSuccess()
            .show()
    }

    private fun defaultErrorMessage(@StringRes stringRes: Int) {
        NToast.with(act)
            .text(getString(stringRes))
            .typeError()
            .show()
    }

    private fun defaultErrorMessage(string: String) {
        NToast.with(act)
            .text(string)
            .typeError()
            .show()
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
        val needToShowBlockBtn = this !is UserInfoFragment
        add(
            PostFragmentV2(null),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_IS_FROM_USER_ROAD, true),
            Arg(ARG_FEED_POST, postItem),
            Arg(ARG_FEED_POST_ID, postItem.postId),
            Arg(ARG_TIME_MILLS, videoPosition),
            Arg(ARG_FEED_POST_POSITION, adapterPosition),
            Arg(ARG_FEED_ROAD_TYPE, BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD.index),
            Arg(ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled),
            Arg(ARG_NEED_TO_REPOST, isRepostAllowed),
            Arg(ARG_NEED_TO_SHOW_HIDE_POSTS_BTN, needToShowBlockBtn),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.fromNetworkRoadType(roadType))
        )
    }

    private fun gotoUserProfileFragment(
        userId: Long?
    ) {
        add(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, userId),
            Arg(ARG_TRANSIT_FROM, getAmplitudeWhereProfileFromOpened()?.property)
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
            needAuth {
                add(
                    HashtagFragment(), Act.LIGHT_STATUSBAR,
                    Arg(ARG_HASHTAG, hashtag)
                )
            }
        }
    }

    private fun handleRepostClick(postItem: PostUIEntity) = needAuth {
        checkAppRedesigned(
            isRedesigned = {
                meeraOpenRepostMenu(postItem)
            },
            isNotRedesigned = {
                openRepostMenu(postItem)
            }
        )
    }

    private fun onPostItemDotsMenuClick(post: PostUIEntity, currentMedia: MediaAssetEntity?) = needAuth { wasLoginAuthorization ->
        triggerPostsAction(FeedViewActions.CheckUpdateAvailability(post = post, currentMedia = currentMedia))
    }

    private fun showPostDotsMenu(
        post: PostUIEntity,
        adapterPosition: Int,
        isEditAvailable: Boolean = false,
        currentMedia: MediaAssetEntity?
    ) = needAuth {
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
                icon = R.drawable.ic_edit_purple_plain,
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
            val img =
                if (post.isPostSubscribed) R.drawable.ic_unsubscribe_post_menu_purple
                else R.drawable.ic_subscribe_post_menu_purple
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
                    triggerPostsAction(FeedViewActions.SubscribeToPost(selectedPostId, PostSubscribeTitle.NotificationString()))
                } else if (post.isPostSubscribed) {
                    feedViewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.UnsubscribeFromPost(selectedPostId, PostSubscribeTitle.NotificationString()))
                }
            }
        }

        post.user?.subscriptionOn?.let { isSubscribed ->
            if (isSubscribed.isFalse() && !isPostAuthor) {
                menu.addItem(
                    R.string.subscribe_user_txt,
                    R.drawable.ic_subscribe_on_user_new,
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
                icon = R.drawable.ic_share_purple_new,
                bottomSeparatorVisible = true
            ) {
                handleRepostClick(post)
            }
            menu.addItem(
                title = R.string.copy_link,
                icon = R.drawable.ic_chat_copy_message,
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
                    icon = R.drawable.ic_eye_off_all_menu_item_red,
                    bottomSeparatorVisible = true
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
            menu.addItem(complainTitleResId, R.drawable.ic_report_profile) {
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
                icon = R.drawable.ic_delete_menu_red
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
                startVideoDelayed()
            }
        }

        dotsMenuPost = post
        menu.show(childFragmentManager)
    }

    private fun addSavingMediaItemsToMenu(menu: MeeraMenuBottomSheet, postCreatorUid: Long?, currentMedia: MediaAssetEntity?, post: PostUIEntity) {
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

    private fun addImageItemToMenu(menu: MeeraMenuBottomSheet, post: PostUIEntity, postCreatorUid: Long?, image: String?) {
        if (image.isNullOrEmpty()) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_download_new,
            bottomSeparatorVisible = true
        ) {
            feedViewModel.logPostMenuAction(
                post = post,
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = postCreatorUid,
                saveType = AmplitudePropertySaveType.PHOTO
            )
            isSavingFeedPhoto = true
            saveImageOrVideoFile(
                imageUrl = image,
                act = act,
                viewLifecycleOwner = viewLifecycleOwner,
                successListener = {
                    Timber.d("saveMediaFile")
                    defaultSuccessMessage(R.string.image_saved)
                    doDelayed(SAVING_PICTURE_DELAY) { isSavingFeedPhoto = false }
                }
            )
        }
    }

    private fun addVideoItemToMenu(menu: MeeraMenuBottomSheet, post: PostUIEntity, postCreatorUid: Long?, mediaId: String? = null) {
        val savingVideoIsAvailable = (requireActivity().application as App).remoteConfigs.postVideoSaving
        if (!savingVideoIsAvailable) return

        menu.addItem(
            title = getString(R.string.save_to_device),
            icon = R.drawable.ic_download_new,
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

    private fun startVideoDelayed() {
        doDelayed(350) {
            feedRecycler?.playVideo(false)
        }
    }

    protected open fun onNewPost() {}

    private fun openRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (isFragmentStarted.not()) return
        getCommunityId()
        SharePostBottomSheet(
            groupId = getCommunityId(),
            post = post.toPost(),
            event = post.event,
            mode = mode,
            callback = object : IOnSharePost {
                override fun onShareFindGroup() {
                    act.goToGroups()
                }

                override fun onShareFindFriend() {
                    add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
                }

                override fun onShareToGroupSuccess(groupName: String?) {
                    feedViewModel.repostSuccess(post)
                    repostMenuSuccessMessage(R.string.success_repost_to_group, groupName)
                }

                override fun onShareToRoadSuccess() {
                    feedViewModel.repostSuccess(post)
                    repostMenuSuccessMessage(R.string.success_repost_to_own_road)
                }

                override fun onShareToChatSuccess(repostTargetCount: Int) {
                    feedViewModel.repostSuccess(post, repostTargetCount)
                    val strResId = if (post.isEvent()) {
                        R.string.success_event_repost_to_chat
                    } else {
                        R.string.success_repost_to_chat
                    }
                    repostMenuSuccessMessage(strResId)
                }

                override fun onPostItemUniqnameUserClick(userId: Long?) {
                    act.addFragment(
                        UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                        Arg(ARG_USER_ID, userId)
                    )
                }
            },
            postOrigin = getAnalyticPostOriginEnum()
        ).show(childFragmentManager)
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (isFragmentStarted.not()) return
        getCommunityId()
        MeeraShareSheet().show(
            fm = childFragmentManager,
            data = MeeraShareBottomSheetData(
                groupId = getCommunityId(),
                post = post.toPost(),
                postOrigin = getAnalyticPostOriginEnum(),
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        act.goToGroups()
                    }

                    override fun onShareFindFriend() {
                        add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        feedViewModel.repostSuccess(post)
                        repostMenuSuccessMessage(R.string.success_repost_to_group, groupName)
                    }

                    override fun onShareToRoadSuccess() {
                        feedViewModel.repostSuccess(post)
                        repostMenuSuccessMessage(R.string.success_repost_to_own_road)
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        feedViewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        repostMenuSuccessMessage(strResId)
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        act.addFragment(
                            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                            Arg(ARG_USER_ID, userId)
                        )
                    }
                }

            )
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

    private fun navigateToEditPost(post: PostUIEntity) {
        add(
            fragment = AddMultipleMediaPostFragment(),
            isLightStatusBar = Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, post.groupId?.toInt()),
            Arg(IArgContainer.ARG_POST, post)
        )
    }

    private fun repostMenuSuccessMessage(
        @StringRes messageRes: Int,
        groupName: String? = null
    ) {
        NToast.with(act)
            .durationLong()
            .text(getString(messageRes, groupName ?: ""))
            .typeSuccess()
            .show()
    }

    private fun handleClickCommunityView(groupId: Long?) {
        groupId?.let { id: Long ->
            add(
                CommunityRoadFragment(),
                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                Arg(ARG_GROUP_ID, id.toInt())
            )
        }
    }


    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            showInfoTooltip(R.string.copy_link_success)
        }
    }

    private fun showInfoTooltip(@StringRes text: Int) {
        val navBarHeightDp = pxToDp(context.getNavigationBarHeight())
        infoTooltip = NSnackbar.with(requireView())
            .typeSuccess()
            .marginBottom(navBarHeightDp)
            .text(getString(text))
            .durationLong()
            .show()
    }

    // Call from onDestroy()
    private fun releaseVideoPlayer() {
        feedRecycler?.clear()
    }

    // Call from onStartFragment() / onReturnTransitionFragment()
    private fun startVideoIfExists() {
        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = feedViewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            feedAdapter?.updateItem(postUpdate)
        }
        feedRecycler?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    // Call from onStop() / onStartAnimationTransitionFragment() / onStopFragment()
    private fun stopVideoIfExists() = feedRecycler?.onStop()

    private fun controlAlreadyPlayingVideo() = feedRecycler?.onStopIfNeeded()

    private fun observeScrollForRefreshButton(
        layoutManager: LinearLayoutManager,
        animationView: LottieAnimationView?,
        dY: Int
    ) {
        val firstItemVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        // Handle scroll Lottie button
        if (dY < 0 && firstItemVisiblePosition !in 0..3) {
            if (firstItemVisiblePosition != -1) {
                handleScrollRefreshButton(animationView, true)
            }
        } else {
            handleScrollRefreshButton(animationView, false)
        }
    }

    private fun handleScrollRefreshButton(
        animationView: LottieAnimationView?,
        isVisible: Boolean
    ) {
        isVisibleScrollRefresh = if (isVisible) {
            if (!isVisibleScrollRefresh) {
                animationView?.visible()
                animationView?.let { animView ->
                    playLottieAnimation(animView, "scroll_refresh_start_animation.json") {
                        animView.visible()
                    }
                }
            }
            true
        } else {
            if (isVisibleScrollRefresh) {
                if (!isPushScrollRefresh) {
                    animationView?.let { animView ->
                        playLottieAnimation(animView, "scroll_refresh_end_animation.json") {
                            animView.gone()
                        }
                    }
                } else {
                    animationView?.gone()
                }
                isPushScrollRefresh = false
            }
            false
        }
    }

    private fun playLottieAnimation(
        animView: LottieAnimationView,
        animJson: String,
        completeAnimation: () -> Unit
    ) {
        animView.setAnimation(animJson)
        animView.playAnimation()
        animView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                animView.removeAllAnimatorListeners()
                completeAnimation.invoke()
            }

            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationStart(animation: Animator) = Unit
        })
    }

    private fun showNotAvailableError(reason: NotAvailableReasonUiEntity) {
        when(reason) {
            NotAvailableReasonUiEntity.POST_NOT_FOUND -> showTextError(getString(R.string.post_edit_error_not_found_message))
            NotAvailableReasonUiEntity.USER_NOT_CREATOR -> showTextError(getString(R.string.post_edit_error_not_creator_message))
            NotAvailableReasonUiEntity.POST_DELETED -> showTextError(getString(R.string.post_edit_error_deleted_message))
            NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE,
            NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER -> {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.post_edit_error_expired_title))
                    setMessage(getString(R.string.post_edit_error_expired_description))
                    positiveButton() { }
                }.show()
            }
        }
    }

    private fun handleClickScrollRefreshButton(animView: LottieAnimationView?) {
        animView?.setOnClickListener {
            isPushScrollRefresh = true
            playLottieAnimation(animView, "scroll_refresh_push_animation.json") {
                animView.gone()
            }
            onClickScrollUpButton()
        }
    }

    private fun initExpandMedia() {
        feedRecycler?.post {
            feedRecycler?.expandMediaIndicatorAction(true, showInstantly = true)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment lifecycle callbacks
    ///////////////////////////////////////////////////////////////////////////

    override fun onStartFragment() {
        super.onStartFragment()
        startVideoIfExists()
        feedViewModel.logScreenForFragment(this.javaClass.simpleName)
        feedRecycler?.scrollEnabled = true
        registerComplaintListener()
        resetLastPostMediaViewInfo()
        initExpandMedia()
    }

    override fun onStopFragment() {
        super.onStopFragment()
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
        super.onDestroyView()
        feedRecycler?.onDestroyView()
        feedRecycler = null
        lottieAnimation = null
        scrollListener = null
        infoTooltip = null
        dotsMenuPost = null
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExists()
        infoTooltip?.dismiss()
        infoTooltip = null
        _audioFeedHelper.stopPlaying(isLifecycleStop = true)
        _audioFeedHelper.removeAudioPriorityListener(priorityListener)
        postDisposable?.dispose()
        feedViewModel.subjectPosts.cleanupBuffer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseVideoPlayer()
    }

    override fun onStartAnimationTransitionFragment() {
        super.onStartAnimationTransitionFragment()
        stopVideoIfExists()
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        startVideoIfExists()
    }
}
