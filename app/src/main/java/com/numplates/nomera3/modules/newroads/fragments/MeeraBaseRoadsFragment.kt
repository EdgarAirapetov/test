package com.numplates.nomera3.modules.newroads.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.common.MEERA_APP_SCHEME
import com.meera.core.common.NEW_MEERA_APP_SCHEME
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.addAnimationTransitionByDefault
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.convertUnixDate
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.UploadType
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.ACTION_AFTER_SUBMIT_LIST_DELAY
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.NO_USER_ID
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAnalyticPost
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraFeedAdapter
import com.numplates.nomera3.modules.feed.ui.data.MOMENTS_POST_ID
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.feed.ui.util.divider.PostDividerDecoration
import com.numplates.nomera3.modules.feed.ui.util.preloader.getPreloader
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraBasePostHolder
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
import com.numplates.nomera3.modules.moments.show.presentation.MomentCallback
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_START_GROUP_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.newroads.BaseRoadRecyclerGestureDetector
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.util.FixedScrollingLinearLayoutManager
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingAnimationPlayListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBubble
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.deeplink.MeeraDeeplink
import com.numplates.nomera3.modules.redesign.deeplink.MeeraDeeplinkAction
import com.numplates.nomera3.modules.redesign.deeplink.MeeraDeeplinkParam
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.MainRoadFragment
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.redesign.fragments.main.map.participant.MeeraEventParticipantsListFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
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
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TIME_MILLS
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.utils.viewModels
import com.numplates.nomera3.presentation.view.fragments.MainFragment
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


//TODO ROAD_FIX
//const val SHOW_PLACE_HOLDER_DELAY = 650L
//const val SHOW_PLACE_HOLDER_DURATION = 250L
//const val HIDE_PLACE_HOLDER_DURATION = 150L
//const val DELAY_START_VIDEO = 300L
//const val DELAY_AFTER_FOLLOW_AUTH_MS = 1000L
const val CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD = 10f

private const val MOMENTS_BLOCK_POSITION_CALCULATE_DELAY = 500L

private const val REFRESH_ALPHA_INVISIBLE = 0f
private const val REFRESH_ALPHA_VISIBLE = 1f
private const val REFRESH_VISIBLE_ANIMATION_DURATION = 200L
private const val MARGIN_PLACEHOLDER_WITH_MOMENTS = 160

abstract class MeeraBaseRoadsFragment<T : ViewBinding>(@LayoutRes layout: Int) :
    MeeraBaseFragment(layout),
    MeeraPostCallback,
    MomentCallback,
    VolumeStateCallback,
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl() {

    protected val binding: T?
        get() = _binding

    private var _binding: T? = null

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T
    private var undoSnackbar: UiKitSnackBar? = null

    protected val act by lazy { activity as MeeraAct }

    private var postDisposable: Disposable? = null

    val viewModel by viewModels<MeeraFeedViewModel>()

    private var blurHelper: BlurHelper? = null

    private var zoomyProvider: ZoomyProvider? = null

    private var formatterProvider: AllRemoteStyleFormatter? = null

    private var sensitiveContentManager: ISensitiveContentManager? = null

    private var cacheUtil: CacheUtil? = null

    private var featureTogglesContainer: FeatureTogglesContainer? = null

    protected val feedAdapter: MeeraFeedAdapter by lazy {
        MeeraFeedAdapter()
    }

    private var layoutManagerInstanceState: Parcelable? = null
    private var layoutManager: FixedScrollingLinearLayoutManager? = null
    private var appHintsScrollListener: OnScrollListener? = null
    private var glidePreloaderScrollListener: OnScrollListener? = null
    private var videoPreloaderScrollListener: OnScrollListener? = null
    private var loadScrollListener: RecyclerPaginationListener? = null

    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    private var selectedPostId: Long? = 0
    private var selectedUserId: Long? = 0

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy {
        SyncContactsDialogDelegate(
            childFragmentManager
        )
    }

    private var recyclerView: MeeraFeedRecyclerView? = null

    private var recyclerDecoration: PostDividerDecoration? = null

    private var isRefreshButtonShown = false

    private var audioFeedHelper: AudioFeedHelper? = null

    private var postCollisionDetector: PostCollisionDetector? = null

    //works once then it's null
    private var postFirstLoadCallback: (() -> Unit)? = null

    private var dotsMenuPost: PostUIEntity? = null

    private var emptyPostsTopMargin = 0

    enum class RoadTypeEnum(var index: Int) {
        MAIN_ROAD(0),
        CUSTOM_ROAD(1),
        SUBSCRIPTIONS_ROAD(2)
    }

    var currentFragment: MainRoadFragment.OnParentFragmentActionsListener? = null

    private var onViewPagerSwipeStateChangeListener: MainRoadFragment.OnViewPagerSwipeStateChangeListener? = null
    private var onRoadScrollListener: MainRoadFragment.OnRoadScrollListener? = null

    private var calculateMomentsBlockPositionHandler: Handler? = null

    private var reactionAnimationHelper: ReactionAnimationHelper? = null

    abstract fun getPostViewRoadSource(): PostViewRoadSource

    abstract fun getRoadType(): RoadTypeEnum

    abstract fun getNetworkRoadType(): NetworkRoadType

    abstract fun getRootView(): View?

    protected open fun getProgress(): View? = null

    open fun onPostsSubmitted() = Unit

    abstract fun hideRefreshLayoutProgress()

    open fun isRepostAllowed(): Boolean = true

    open fun getAmplitudeWhereFromOpened(): AmplitudePropertyWhere? = null

    open fun getAmplitudeWhereMomentOpened(fromUser: Boolean): AmplitudePropertyMomentScreenOpenWhere =
        AmplitudePropertyMomentScreenOpenWhere.OTHER

    abstract fun startVideoIfExist()

    abstract fun forceStartVideo()

    abstract fun forcePlayVideoFromStart()

    abstract fun stopVideoIfExist(isFromMultimedia: Boolean = false)

    abstract fun controlAlreadyPlayingVideo()

    abstract fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String? = null)

    abstract fun getParentContainer(): ViewGroup?

    abstract fun onRefresh(showAdditionalLoader: Boolean)

    open fun setNewPostBtnMarginTop(bottomSheetState: Int) = Unit

    private val childView: View?
        get() = getRootView()

    fun getRoadVerticalScrollPosition(): Int {
        return recyclerView?.computeVerticalScrollOffset() ?: 0
    }

    fun bindListeners(
        currentFragmentListener: MainRoadFragment.OnParentFragmentActionsListener?,
        onViewPagerSwipeStateChangeListener: MainRoadFragment.OnViewPagerSwipeStateChangeListener?,
        onRoadScrollListener: MainRoadFragment.OnRoadScrollListener?
    ) {
        this.currentFragment = currentFragmentListener
        this.onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener
        this.onRoadScrollListener = onRoadScrollListener
    }

    fun clearListeners() {
        this.currentFragment = null
        this.onViewPagerSwipeStateChangeListener = null
        this.onRoadScrollListener = null
    }

    fun resetAllZoomViews() {
        recyclerView?.apply {
            val viewHolders = ArrayList<RecyclerView.ViewHolder>()
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                viewHolders.add(getChildViewHolder(view))
            }

            viewHolders.forEach { viewHolder ->
                when (viewHolder) {
                    is MeeraBasePostHolder -> viewHolder.endZoom()
                }
            }
        }
    }

    fun resetLastPostMediaViewInfo() {
        triggerPostsAction(FeedViewActions.SaveLastPostMediaViewInfo(null))
    }

    fun scrollMomentsToStart(smoothScroll: Boolean = false) {
        val momentsPosition = feedAdapter.getMomentsItemPosition().takeIf { it != -1 }
        momentsPosition?.let {
            val momentsViewHolder = this.recyclerView?.findViewHolderForAdapterPosition(it)
            feedAdapter.scrollMomentsToStart(momentsViewHolder, smoothScroll)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRoadType()
        audioFeedHelper = App.component.getAudioFeedHelper()
    }

    private fun initRoadType() {
        val roadType = when (getRoadType()) {
            RoadTypeEnum.MAIN_ROAD -> RoadTypesEnum.MAIN
            RoadTypeEnum.CUSTOM_ROAD -> RoadTypesEnum.CUSTOM
            RoadTypeEnum.SUBSCRIPTIONS_ROAD -> RoadTypesEnum.SUBSCRIPTION
        }
        viewModel.initRoadType(type = roadType, originEnum = DestinationOriginEnum.fromRoadType(getRoadType()))

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioFeedHelper?.init()
        initAdapterCallback()
        initReactionAnimationHelper()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initMomentsBlockHandler()
        initPostsLiveObservable()
        initFeatureToggleObservable()
        initStatusToastCallback()
        restoreRecyclerScrollState()
    }

    private fun restoreRecyclerScrollState() {
        recyclerView?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                recyclerView?.viewTreeObserver?.removeOnPreDrawListener(this)
                val (position, offset) = viewModel.getSavedScrollOffset()
                (recyclerView?.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(position, offset)
                return true
            }
        })
    }

    private fun initAdapterCallback() {
        feedAdapter.setPostCallback(this)
        feedAdapter.setMomentCallback(this)
        feedAdapter.setVolumeStateCallback(this)

        blurHelper =
            BlurHelper(context = requireContext(), lifecycle = viewLifecycleOwner.lifecycle).also { blurHelper ->
                feedAdapter.setBlurHelper(blurHelper)
            }

        sensitiveContentManager = object : ISensitiveContentManager {
            override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
                return viewModel.isMarkedAsSensitivePost(postId)
            }

            override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
                viewModel.markPostAsNotSensitiveForUser(postId, parentPostId)
                viewModel.refreshPost(postId)
                viewModel.refreshPost(parentPostId)
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

        feedAdapter.setAudioFeedHelper(audioFeedHelper)

        featureTogglesContainer = viewModel.getFeatureTogglesContainer().also { featureTogglesContainer ->
            feedAdapter.setFeatureTogglesContainer(featureTogglesContainer)
        }
        initPostFormatterIfNeed()
    }

    private fun initReactionAnimationHelper() {
        reactionAnimationHelper = ReactionAnimationHelper()
    }

    private fun initStatusToastCallback() {
        this.view?.post {
            act.getMeeraStatusToastViewController().apply {
                setOnToastControllerRepeatListener { type, postStringEntity ->
                    when (type) {
                        UploadType.Post, UploadType.EditPost -> {
                            navigateEditPostFragment(post = null, postStringEntity = postStringEntity)
                        }

                        UploadType.EventPost -> {
                            //todo ROAD FIX
                        }

                        else -> Unit
                    }
                }
                initAdditionalMargin(countMessageInputBottomPadding())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        blurHelper?.updateLifecycle(viewLifecycleOwner.lifecycle)
        subscribePostRx()
        viewModel.logScreenForFragment(this.javaClass.simpleName)
        registerComplaintListener()
        initExpandMedia()
    }

    override fun onPause() {
        viewModel.removePostsViewedBlock()
        stopAudio()
        unregisterComplaintListener()
        super.onPause()
    }

    override fun onFindPeoplesClicked() {
        openPeoples()
    }

    override fun onCancelByUser(menuTag: String?) {
        viewModel.logPostMenuAction(
            post = dotsMenuPost ?: return,
            action = AmplitudePropertyMenuAction.CANCEL,
            authorId = selectedUserId,
        )
        dotsMenuPost = null
    }

    override fun onShowMoreSuggestionsClicked() {
        findNavController().safeNavigate(R.id.action_mainRoadFragment_to_peoplesFragment)
        viewModel.logOpenPeoplesFromSuggestions()
    }

    override fun onUnsubscribeSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {
        viewModel.unsubscribeSuggestedUser(userId, isApprovedUser, topContentMaker)
    }

    override fun onSubscribeSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {
        viewModel.subscribeSuggestedUser(userId, isApprovedUser, topContentMaker)
    }

    override fun onAddFriendSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {
        viewModel.addFriendSuggestedUser(userId, isApprovedUser, topContentMaker)
    }

    override fun onRemoveFriendSuggestedUserClicked(userId: Long) {
        viewModel.removeFriendSuggestedUser(userId)
    }

    override fun onHideSuggestedUserClicked(userId: Long) {
        viewModel.hideSuggestedUser(userId)
    }

    override fun onSuggestedUserClicked(
        isTopContentMaker: Boolean,
        isApproved: Boolean,
        hasMutualFriends: Boolean,
        isSubscribed: Boolean,
        toUserId: Long
    ) {
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_userInfoFragment,
            bundle = Bundle().apply {
                putSerializable(IArgContainer.ARG_USER_ID, toUserId)
            }
        )
    }

    override fun onReferralClicked() {
        //TODO ROAD_FIX
//        add(ReferralFragment(), Act.LIGHT_STATUSBAR)
        viewModel.logOpenReferral()
    }

    override fun onSyncContactsClicked() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_access_description,
            positiveButtonRes = R.string.allow,
            positiveButtonAction = { viewModel.onSyncContactsPositiveButtonClicked() },
            negativeButtonRes = R.string.general_later,
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog
        )
        viewModel.logSyncContactsClicked()
    }

    override fun onStartPlayingVideoRequested() {
        startVideoIfExist()
    }

    override fun forceStartPlayingVideoRequested() {
        forceStartVideo()
    }

    override fun onStopPlayingVideoRequested() {
        stopVideoIfExist(isFromMultimedia = true)
    }

    override fun onMediaExpandCheckRequested() {
        initExpandMedia()
    }

    override fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {
        viewModel.onTriggerAction(FeedViewActions.UpdatePostSelectedMediaPosition(postId, selectedMediaPosition))
    }

    override fun onReactionClickToShowScreenAnimation(
        reactionEntity: ReactionEntity,
        anchorViewLocation: Pair<Int, Int>
    ) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        reactionAnimationHelper?.playLottieAtPosition(
            recyclerView = recyclerView,
            requireContext(),
            parent = getParentContainer(),
            reactionType = reactionType,
            x = anchorViewLocation.first.toFloat(),
            y = anchorViewLocation.second.toFloat()
        )
    }

    override fun requestNewMomentsPage(pagingTicket: String?) =
        viewModel.getMomentDelegate().requestMomentsPage(pagingTicket = pagingTicket)

    override fun onMomentsCarouselBecomeNotVisible() = Unit

    override fun isRequestingMoments(): Boolean =
        viewModel.getMomentDelegate().isMomentsLastPage()

    override fun isMomentsCarouselLastPage(): Boolean =
        viewModel.getMomentDelegate().isMomentsLastPage()

    override fun isMomentPagingListTicketValid(ticket: String?): Boolean =
        viewModel.getMomentDelegate().isMomentPagingListTicketValid(ticket)

    override fun onMomentTapCreate(entryPoint: AmplitudePropertyMomentEntryPoint) {
        viewModel.logMomentTapCreate(entryPoint)
    }

    private fun initMomentsBlockHandler() {
        calculateMomentsBlockPositionHandler = Handler(Looper.getMainLooper())
    }

    private fun subscribePostRx() {
        postDisposable = viewModel.postsObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handlePostViewEvents) { Timber.e(it) }
    }

    fun setOnFirstPostLoadedListener(postLoadCallback: (() -> Unit)? = null) {
        this.postFirstLoadCallback = postLoadCallback
    }

    override fun onAddMomentClicked() {
        needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
            act.getMomentsViewController().open()
        }
    }

    override fun onShowMomentsClicked(
        startGroupId: Long,
        view: View?,
        isViewed: Boolean
    ) {
        if (isMomentsDisabled()) {
            return
        }
        findNavController().safeNavigate(
            R.id.action_global_meeraViewMomentFragment,
            bundleOf(
                KEY_START_GROUP_ID to startGroupId,
                KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromRoadType(getRoadType())
            )
        )
    }

    override fun onMomentClicked(moment: MomentItemUiModel) {
        onShowMomentsClicked(isViewed = moment.isViewed)
    }

    override fun onMomentGroupClicked(
        momentGroup: MomentGroupUiModel,
        view: View?,
        isViewed: Boolean
    ) {
        onShowMomentsClicked(
            momentGroup.id,
            view,
            isViewed
        )
    }

    override fun onMomentGroupLongClicked(momentGroup: MomentGroupUiModel) {
        needAuthToNavigate {
            MeeraMomentsMenuBottomDialog {
                viewModel.hideUserMoments(momentGroup.userId)
            }.show(
                childFragmentManager,
                MeeraMomentsMenuBottomDialog::class.simpleName
            )
        }
    }

    override fun onUpdateMomentsClicked() {
        viewModel.getMomentDelegate().initialLoadMoments(scrollToStart = true)
    }

    private fun initPostFormatterIfNeed() {
        if (formatterProvider != null) return

        val settings = viewModel.getSettings()
        settings?.let {
            formatterProvider = AllRemoteStyleFormatter(settings).also { formatterProvider ->
                feedAdapter.setRemoteStyleFormatter(formatterProvider)
            }
        }
    }

    private fun initPostsLiveObservable() {
        viewModel.livePosts.observe(viewLifecycleOwner) { posts ->
            hideRefreshLayoutProgress()
            initPostFormatterIfNeed()
            feedAdapter.submitList(posts)
            setupPlaceholderMargin(posts)
            recyclerView?.invalidateItemDecorations()
            getProgress()?.gone()
            onViewPagerSwipeStateChangeListener?.requestCalculateMomentsBlockPosition(getRoadType())
            recyclerView?.postDelayed({ controlAlreadyPlayingVideo() }, ACTION_AFTER_SUBMIT_LIST_DELAY)
        }
        viewModel.liveEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)
        viewModel.editPostEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)

        // обновление через payloads
        viewModel.liveFeedEvents.observe(viewLifecycleOwner) { event ->
            when (event) {
                is FeedViewEventPost.UpdatePostEvent -> feedAdapter.updateItem(
                    event.post,
                    event.adapterPosition
                )

                is FeedViewEventPost.UpdatePosts -> feedAdapter.submitList(
                    data = event.posts,
                    isShouldUpdateTime = true
                )

                is FeedViewEventPost.ShowMediaExpand -> initExpandMedia()
                is FeedViewEventPost.UpdateVolumeState -> handleUpdateVolumeState(event.volumeState)
                is FeedViewEventPost.UpdatePostValues -> handleUpdatePostValues(event.post)
                else -> Unit
            }
        }
    }

    private fun handleUpdateVolumeState(volumeState: VolumeState) {
        feedAdapter.updateVolumeState(
            volumeState = volumeState,
            visiblePositions = recyclerView?.getVisiblePositions()
        )

        if (volumeState == VolumeState.ON) audioFeedHelper?.stopPlaying()
    }

    private fun handleUpdatePostValues(uiPostUpdate: UIPostUpdate) {
        feedAdapter.updateItem(uiPostUpdate)
    }

    private fun initFeatureToggleObservable() {
        act.activityViewModel.onFeatureTogglesLoaded.observe(viewLifecycleOwner) {
            viewModel.initLoadMoments()
        }
    }

    private fun handlePostViewEvents(event: FeedViewEvent?) {
        //TODO ROAD_FIX
        when (event) {
            is FeedViewEvent.FailChangeLikeStatus -> showToastMessage(
                R.string.no_internet,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.ShowCommonError -> showToastMessage(
                event.messageResId,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.ShowCommonSuccess -> showToastMessage(getString(event.messageResId))
            is FeedViewEvent.OnSuccessHideUserRoad -> showToastMessage(getString(event.messageResId))
            is FeedViewEvent.UpdatePostById -> feedAdapter.updateItemByPostId(event.postId)
            is FeedViewEvent.LikeChangeVibration -> {
                requireContext().vibrate()
            }

            is FeedViewEvent.OpenDeepLink -> handleDeeplink(event.deepLink)
            is FeedViewEvent.OnShowLoader -> feedAdapter.showLoader(event.show)
            is FeedViewEvent.ShowCommonErrorString -> showToastMessage(
                event.message,
                messageState = AvatarUiState.ErrorIconState
            )

            is FeedViewEvent.OnFirstPageLoaded -> handleFirstPageLoaded()
            is FeedViewEvent.CopyLinkEvent -> copyLink(event.link)
            is FeedViewEvent.UpdateEventPost -> feedAdapter.updateItem(
                UIPostUpdate.UpdateEventPostParticipationState(postId = event.post.postId, postUIEntity = event.post)
            )

            is FeedViewEvent.ShowEventSharingSuggestion -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraOpenRepostMenu(
                            post = event.post,
                            mode = SharingDialogMode.SUGGEST_EVENT_SHARING
                        )
                    }
                )
            }

            is FeedViewEvent.ScrollMomentsToStart -> scrollMomentsToStart()

            is FeedViewEvent.RequestContactsPermission -> requestContactsPermission()
            is FeedViewEvent.ShowSyncDialogPermissionDenied -> showSyncDialogPermissionDenied()
            is FeedViewEvent.ShowContactsHasBeenSyncDialog -> showContactsHasBeenSyncDialog()
            is FeedViewEvent.OnSuccessGetVip -> {
                showToastMessage(
                    getString(
                        com.meera.referrals.R.string.referral_vip_activated_until,
                        convertUnixDate(event.vipUntilDate)
                    )
                )
            }

            is FeedViewEvent.OnFailGetVip -> {
                showToastMessage(
                    getString(com.meera.referrals.R.string.referral_vip_activated_fail),
                    messageState = AvatarUiState.ErrorIconState
                )
            }

            is FeedViewEvent.PostEditAvailableEvent -> updateMenuWitEdit(
                postId = event.post.postId,
                isAvailable = event.isAvailable,
                currentMedia = event.currentMedia
            )


            is FeedViewEvent.OpenEditPostEvent -> navigateToEditPost(post = event.post)
            is FeedViewEvent.ShowAvailabilityError -> showNotAvailableError(event.reason)

            is FeedViewEvent.ShowReactionStatisticsEvent -> showReactionStatistics(event)
            else -> Unit
        }
    }

    private fun showReactionStatistics(event: FeedViewEvent.ShowReactionStatisticsEvent) {
        MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
            event.post.postId,
            event.entityType
        ) { destination ->
            when (destination) {
                is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                    findNavController().safeNavigate(
                        resId = R.id.action_mainRoadFragment_to_userInfoFragment,
                        bundle = bundleOf(
                            IArgContainer.ARG_USER_ID to destination.userEntity.userId
                        )
                    )
                }
            }
        }.show(childFragmentManager)

        val where = if (event.post.isEvent()) {
            AmplitudePropertyReactionWhere.MAP_EVENT
        } else {
            AmplitudePropertyReactionWhere.POST
        }

        viewModel.logStatisticReactionsTap(
            where = where,
            whence = DestinationOriginEnum.fromRoadType(getRoadType()).toAmplitudePropertyWhence()
        )
    }

    private fun handleDeeplink(deepLink: String) {
        if (!(deepLink.contains(MEERA_APP_SCHEME) || deepLink.contains(NEW_MEERA_APP_SCHEME))) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)))
        } else {
            val deeplinkWithOrigin =
                MeeraDeeplink.addDeeplinkOrigin(deepLink, DeeplinkOrigin.ANNOUNCEMENT)
            act.emitDeeplinkCall(
                MeeraDeeplinkParam.MeeraDeeplinkActionContainer(
                    MeeraDeeplink.getAction(
                        deeplinkWithOrigin
                    ) ?: MeeraDeeplinkAction.None
                )
            )
        }
    }

    private fun showContactsHasBeenSyncDialog() {
        if (!isResumed) return
        if (this !is MeeraMainRoadItemFragment) return
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_done else R.drawable.ic_sync_contacts_done,
            isAppRedesigned = true
        )
        viewModel.logSyncContactsClicked()
    }

    private fun showSyncDialogPermissionDenied() {
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.contacts_synchronization_allow_access,
            descriptionRes = R.string.contacts_sync_allow_in_settings_description,
            positiveButtonRes = R.string.go_to_settings,
            positiveButtonAction = { sendUserToAppSettings() },
            negativeButtonRes = R.string.general_later,
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_dialog else R.drawable.ic_sync_contacts_dialog
        )
    }

    private fun requestContactsPermission() {
        setPermissions(
            permission = Manifest.permission.READ_CONTACTS,
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.onContactsPermissionGranted()
                }

                override fun onDenied() {
                    val deniedAndNoRationaleNeededAfterRequest =
                        !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                    viewModel.onContactsPermissionDenied(deniedAndNoRationaleNeededAfterRequest)
                }

                override fun onError(error: Throwable?) {
                    Timber.e(error)
                }
            }
        )
    }

    private fun handleFirstPageLoaded() {
        scrollMomentsToStart(smoothScroll = true)
    }

    private fun registerComplaintListener() {
        complainsNavigator.registerAdditionalActionListener(this) { result ->
            when {
                result.isSuccess -> showAdditionalStepsForComplain(result.getOrThrow())
                result.isFailure -> showToastMessage(
                    R.string.user_complain_error,
                    messageState = AvatarUiState.ErrorIconState
                )
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            showToastMessage(getString(R.string.copy_link_success))
        }
    }

    private fun showToastMessage(
        @StringRes messageRes: Int,
        messageState: AvatarUiState = AvatarUiState.SuccessIconState
    ) {
        showToastMessage(getString(messageRes), messageState)
    }

    private fun showToastMessage(messageString: String, messageState: AvatarUiState = AvatarUiState.SuccessIconState) =
        doOnUIThread {
            undoSnackbar = UiKitSnackBar.make(
                view = requireView(),
                params = SnackBarParams(
                    snackBarViewState = SnackBarContainerUiState(
                        messageText = messageString,
                        avatarUiState = messageState,
                    ),
                    duration = BaseTransientBottomBar.LENGTH_SHORT,
                    dismissOnClick = true,
                    paddingState = PaddingState(
                        bottom = countMessageInputBottomPadding()
                    )
                )
            )
            undoSnackbar?.show()
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
                        viewModel.downloadPostVideo(postId, assetId)
                    }
                ),
                dismissOnClick = true,
                paddingState = PaddingState(
                    bottom = countMessageInputBottomPadding()
                )
            )
        )
        undoSnackbar?.show()
    }

    private fun countMessageInputBottomPadding(): Int =
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().height


    private fun showAdditionalStepsForComplain(userId: Long) {
        val bottomSheet = UserComplainAdditionalBottomSheet.newInstance(userId).apply {
            callback = object : AdditionalComplainCallback {
                override fun onSuccess(msg: String?, reason: ComplainEvents) {
                    showToastMessage(msg.orEmpty())
                }

                override fun onError(msg: String?) {
                    showToastMessage(msg.orEmpty(), messageState = AvatarUiState.ErrorIconState)
                }
            }
        }
        bottomSheet.show(childFragmentManager, "UserComplainAdditionalBottomSheet")
    }

    val isRecyclerViewIdle: Boolean
        get() {
            val currentScrollState = recyclerView?.scrollState ?: -1
            return currentScrollState == RecyclerView.SCROLL_STATE_IDLE
        }

    @SuppressLint("ClickableViewAccessibility")
    fun initRecycler(recyclerView: MeeraFeedRecyclerView) {
        initPostViewCollisionDetector(recyclerView)
        recyclerView.setHasFixedSize(true)
        this.recyclerView = recyclerView
        this.recyclerDecoration = PostDividerDecoration.build(requireContext()).also {
            this.recyclerView?.addItemDecoration(it)
        }
        (recyclerView as? MeeraFeedRecyclerView)?.setAudioFeedHelper(audioFeedHelper)

        // При нажатии на ячейку холдера RecyclerView может автоматичекски доскроливать к самому
        // холдеру. Сам скролл происходит в родительском методе requestChildRectangleOnScreen
        // Для того, что-бы избежать автоскролл необходимо переопределить данный метод
        layoutManager = object : FixedScrollingLinearLayoutManager(act) {
            override fun requestChildRectangleOnScreen(
                parent: RecyclerView,
                child: View, rect: Rect,
                immediate: Boolean,
                focusedChildVisible: Boolean
            ) = false

            override fun scrollVerticallyBy(
                dy: Int,
                recycler: RecyclerView.Recycler,
                state: RecyclerView.State
            ): Int {
                return super.scrollVerticallyBy(dy, recycler, state)
            }
        }
        layoutManagerInstanceState?.apply { layoutManager?.onRestoreInstanceState(this) }
        recyclerView.layoutManager = layoutManager.apply {
            this?.initialPrefetchItemCount = 5
        }
        recyclerView.setItemViewCacheSize(5)
        recyclerView.itemAnimator?.changeDuration = 0
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.setOnViewPagerSwipeStateChangeListener(onViewPagerSwipeStateChangeListener)
        recyclerView.setVolumeStateCallback(this)

        if (recyclerView.adapter == null)
            recyclerView.adapter = feedAdapter

        // Scroll listener for app hints
        appHintsScrollListener = object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                parentFragment?.getMainRoadFragment()?.callOnScroll()
                controlParentExpand(dy)
                calculateMomentsBlockPosition()
            }
        }.also {
            recyclerView.addOnScrollListener(it)
        }

        val gestureDetector = BaseRoadRecyclerGestureDetector()
        recyclerView.setOnTouchListener({ v, e -> gestureDetector.onTouchEvent(v, e) })

        initGlidePreloader()
        initVideoPreloader()
    }

    private fun getCurrentBehaviorState() = NavigationManager.getManager().getTopBehaviour()?.state

    private fun controlParentExpand(dy: Int) {
        val currentState = NavigationManager.getManager().getTopBehaviour()?.state ?: return
        if (currentState == BottomSheetBehavior.STATE_DRAGGING
            || currentState == BottomSheetBehavior.STATE_SETTLING
        ) {
            return
        }

        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return
        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        val shouldShowBigTabs = firstVisibleItemPosition != 0

        if (dy < -CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD) {
            currentFragment?.expandAppBar(expand = true, showBigTabs = shouldShowBigTabs)
        } else if (dy > CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD) {
            currentFragment?.expandAppBar(expand = false, showBigTabs = shouldShowBigTabs)
        }

        if (dy < -CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD
            && firstVisibleItemPosition == 0
            && getCurrentBehaviorState() == BottomSheetBehavior.STATE_EXPANDED
        ) {
            NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        } else if (dy > CHANGING_BEHAVIOR_STATE_SCROLL_THRESHOLD && firstVisibleItemPosition != 0) {
            NavigationManager.getManager().getTopBehaviour()?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initGlidePreloader() = getPreloader(feedAdapter)?.let { preloader ->
        glidePreloaderScrollListener = preloader.also {
            recyclerView?.addOnScrollListener(it)
        }
    }

    private fun initVideoPreloader() {
        videoPreloaderScrollListener = object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                viewModel.preloadVideoPosts(currentVisiblePostPosition = layoutManager.findLastVisibleItemPosition())
            }
        }.also {
            recyclerView?.addOnScrollListener(it)
        }
    }

    fun initPostsLoadScrollListener(withRefresh: Boolean = false) {
        // Clear listeners if exists
        loadScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
        }

        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        if (withRefresh) {
            loadPostsRequest(0, getNetworkRoadType())
            scrollMomentsToStart()
        }
        loadScrollListener = object : RecyclerPaginationListener(layoutManager) {
            override fun loadMoreItems() {
                viewModel.logFeedScroll()
                loadPostsRequest(getStartPostId(), getNetworkRoadType())
            }

            override fun isLastPage(): Boolean = viewModel.isLastPage

            override fun isLoading(): Boolean = viewModel.isLoading

            // Handle scroll animation button
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                observeScrollForRefreshButton(layoutManager, dy)
            }
        }
        loadScrollListener?.let { recyclerView?.addOnScrollListener(it) }
    }

    private fun getStartPostId(): Long {
        val position = feedAdapter.itemCount
        val item = feedAdapter.getItem(position - 1)
        Timber.tag("ROAD POSTS").d("loaded postId = ${item?.postId} with position = $position")
        return item?.postId ?: 0L
    }

    private fun loadPostsRequest(startPostId: Long, roadType: NetworkRoadType) {
        when (roadType) {
            is NetworkRoadType.ALL -> triggerPostsAction(
                FeedViewActions
                    .GetAllPosts(startPostId)
            )

            is NetworkRoadType.USER -> triggerPostsAction(
                FeedViewActions
                    .GetUserPosts(
                        startPostId = startPostId,
                        userId = roadType.userId ?: 0L,
                        selectedPostId = roadType.selectedPostId
                    )
            )

            is NetworkRoadType.SUBSCRIPTIONS -> triggerPostsAction(
                FeedViewActions
                    .GetSubscriptionPosts(startPostId)
            )

            else -> Timber.e("Unknown road type")
        }
    }

    private fun observeScrollForRefreshButton(
        layoutManager: LinearLayoutManager,
        dY: Int
    ) {
        val firstItemVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (dY < 0 && firstItemVisiblePosition !in 0..3) {
            if (firstItemVisiblePosition != -1) {
                handleScrollRefreshButton(true)
            }
        } else {
            handleScrollRefreshButton(false)
        }
    }

    fun loadPosts() {
        loadPostsRequest(0, getNetworkRoadType())
    }

    private fun MainPostRoadsFragment.callOnScroll() {
        appBarScrollController?.onRoadScroll()
    }

    private fun Fragment.getMainRoadFragment(): MainPostRoadsFragment? {
        return (this as? MainFragment)?.currentFragment as? MainPostRoadsFragment
    }

    // PostCallbacks
    override fun onDotsMenuClicked(post: PostUIEntity, adapterPosition: Int, currentMedia: MediaAssetEntity?) {
        requireActivity().vibrate()
        viewModel.onTriggerAction(FeedViewActions.CheckUpdateAvailability(post = post, currentMedia = currentMedia))
    }

    override fun onStopLoadingClicked(post: PostUIEntity) {
        viewModel.stopDownloadingPostVideo(postId = post.postId)
    }

    override fun onPostClicked(post: PostUIEntity, adapterPosition: Int) {
        gotoPostFragment(post, adapterPosition)
    }

    override fun onPictureClicked(post: PostUIEntity) {
        goToContentViewer(post)
        recyclerView?.post { resetAllZoomViews() }
    }

    override fun onVideoClicked(post: PostUIEntity, adapterPosition: Int) {
        goToVideoPostFragment(post, adapterPosition)
    }

    override fun onMediaClicked(post: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
        goToMultimediaPostViewFragment(post, mediaAsset, adapterPosition)
    }

    override fun onCommentClicked(post: PostUIEntity, adapterPosition: Int) {
        gotoPostFragment(post, adapterPosition)
    }

    override fun onRepostClicked(post: PostUIEntity) {
        requireActivity().vibrate()
        handleRepostClick(post)
    }

    override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigate { viewModel.showReactionStatistics(post, ReactionsEntityType.POST) }
    }

    override fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean
    ) {
        val postOrigin = DestinationOriginEnum.fromRoadType(getRoadType())
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getMeeraReactionBubbleViewController().onSelectDefaultReaction(
            reactionSource = MeeraReactionSource.Post(
                postId = post.postId,
                reactionHolderViewId = reactionHolderViewId,
                originEnum = postOrigin
            ),
            currentReactionsList = post.reactions ?: emptyList(),
            reactionsParams = reactionsParams,
            forceDefault = forceDefault,
            isShouldVibrate = false
        )
    }

    override fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
    ) {
        val postOrigin = DestinationOriginEnum.fromRoadType(getRoadType())
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        val actionBarType = MeeraContentActionBar.ContentActionBarType.getType(post.toMeeraContentActionBarParams())
        act.getMeeraReactionBubbleViewController().showReactionBubble(
            reactionSource = MeeraReactionSource.Post(
                postId = post.postId,
                reactionHolderViewId = reactionHolderViewId,
                originEnum = postOrigin
            ),
            showPoint = showPoint,
            viewsToHide = viewsToHide,
            reactionTip = reactionTip,
            currentReactionsList = post.reactions ?: emptyList(),
            contentActionBarType = actionBarType,
            reactionsParams = reactionsParams,
            containerInfo = act.getDefaultReactionContainer(),
            postedAt = post.date
        )
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
        post.user?.userId?.let { id ->
            gotoUserProfileFragment(userId = id)
        }
    }

    override fun onMomentProfileClicked(momentGroup: MomentGroupUiModel) {
        gotoUserProfileFragment(
            userId = momentGroup.userId,
            where = AmplitudePropertyWhere.MOMENT
        )
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
            bundleOf(KEY_USER_ID to userId)
        )
    }

    override fun onCommunityClicked(communityId: Long, adapterPosition: Int) {
        handleClickCommunityView(communityId)
    }

    override fun onRateUsClicked(rating: Int, comment: String, adapterPosition: Int) {
        viewModel.onTriggerAction(FeedViewActions.RateUs(rating = rating, comment = comment))
    }

    override fun onRateUsProcessAnalytic(rateUsAnalyticsRating: RateUsAnalyticsRating) {
        viewModel.onTriggerAction(FeedViewActions.RateUsAnalytic(rateUsAnalyticsRating = rateUsAnalyticsRating))
    }

    override fun onRateUsGoToGoogleMarketClicked() {
        handleMarketClicked()
    }

    override fun onHideRateUsPostClicked(adapterPosition: Int) {
        viewModel.onTriggerAction(FeedViewActions.HideRateUsPost)
    }

    override fun onHolidayWordClicked() {
        act.showFireworkAnimation {}
    }

    override fun onFeatureClicked(
        featureId: Long,
        haveAction: Boolean,
        dismiss: Boolean,
        deepLink: String?,
        featureText: String?,
    ) {
        handleFeatureClicked(featureId, dismiss, deepLink)
        viewModel.logAnnouncementButtonEvent(
            dismissed = dismiss,
            haveAction = haveAction,
            announceName = featureText,
        )
    }

    override fun onShowMoreRepostClicked(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let { parentPost ->
            gotoPostFragment(parentPost, adapterPosition)
            viewModel.logPressMoreText(
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
            gotoPostFragment(post, adapterPosition)
        } else {
            triggerPostsAction(FeedViewActions.OnShowMoreText(post))
        }

        viewModel.logPressMoreText(
            postId = post.postId,
            authorId = post.user?.userId ?: NO_USER_ID,
            where = requireNotNull(getAmplitudeWhereFromOpened()),
            postType = AmplitudePropertyPostType.POST,
            isPostDetailOpen = isOpenPostDetail,
        )
    }

    override fun onPressRepostHeader(post: PostUIEntity, adapterPosition: Int) {
        post.parentPost?.let {
            gotoPostFragment(it, adapterPosition)
        }
    }

    override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {
        val container = act.getRootView() as? ViewGroup
        container?.addView(flyingReaction)
        flyingReaction.startAnimationFlying()
        flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
            override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                container?.removeView(playedFlyingReaction)
            }
        })
    }

    override fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {
        needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) { followUser(post) }
    }

    override fun onShowEventOnMapClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        (parentFragment as? MainRoadFragment?)?.navigateToEventOnMap(post)
    }

    //TODO ROAD_MAP_FIX
    override fun onNavigateToEventClicked(post: PostUIEntity) {
//        openEventNavigation(post)
        viewModel.logMapEventGetTherePress(post)
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
        viewModel.onJoinAnimationFinished(
            postUIEntity = post,
            adapterPosition = adapterPosition
        )
    }

    override fun onJoinEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        viewModel.joinEvent(post)
    }

    override fun onLeaveEventClicked(post: PostUIEntity, isRepost: Boolean, adapterPosition: Int) {
        viewModel.leaveEvent(post)
    }

    private fun followUser(post: PostUIEntity) {
        val isPostAuthor = post.user?.userId == viewModel.getUserUid()
        if (isPostAuthor) return
        val isSubscribed = post.user?.subscriptionOn ?: return
        if (isSubscribed.isTrue()) {
            showUnsubscribeConfirmDialog(
                postId = post.postId,
                selectedUserId = post.user.userId,
                clearPost = false,
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

    private fun initPostViewCollisionDetector(recyclerView: RecyclerView) {
        if (postCollisionDetector == null && getPostViewRoadSource() != PostViewRoadSource.Disable) {
            val postViewHighlightLiveData = viewModel.getPostViewHighlightLiveData()

            postCollisionDetector = PostCollisionDetector.create(
                detectTime = PostCollisionDetector.getDurationMsFromSettings(viewModel.getSettings()),
                postViewHighlightEnable = postViewHighlightLiveData.value ?: false,
                recyclerView = recyclerView,
                roadFragment = this,
                roadSource = getPostViewRoadSource(),
                detectPostViewCallback = { postViewDetectModel ->
                    viewModel.detectPostView(postViewDetectModel)
                },
                postUploadPostViewsCallback = {
                    viewModel.uploadPostViews()
                }
            )

            postViewHighlightLiveData.observe(viewLifecycleOwner) { postViewHighlightEnable ->
                postCollisionDetector?.setPostViewHighlightEnable(postViewHighlightEnable)
            }
        }
    }

    private val priorityListener: () -> Unit = {
        recyclerView?.turnOffAudioOfVideo()
    }

    private fun initExpandMedia() {
        recyclerView?.post {
            recyclerView?.expandMediaIndicatorAction(true, showInstantly = true)
        }
    }

    override fun onStop() {
        super.onStop()
        stopAudio()
        viewModel.clearEvents()
        postDisposable?.dispose()
        audioFeedHelper?.removeAudioPriorityListener(priorityListener)
        layoutManagerInstanceState = layoutManager?.onSaveInstanceState()
    }

    override fun onStart() {
        super.onStart()
        audioFeedHelper?.addAudioPriorityListener(priorityListener)

        NavigationManager.getManager().mainMapFragment.initNavigationButtonsListeners(fromMap = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        postFirstLoadCallback = null
    }

    fun stopAudio() {
        audioFeedHelper?.stopPlaying(isLifecycleStop = true)
    }

    fun calculateMomentsBlockPosition() {
        val momentsPositionInList = feedAdapter.getMomentsItemPosition().takeIf { it != -1 }
        if (momentsPositionInList == null) {
            onViewPagerSwipeStateChangeListener?.onMomentsBlockPositionChanged(
                momentsBlockCoords = null,
                currentRoadType = getRoadType()
            )
            return
        }
        val momentsHolder = recyclerView?.findViewHolderForAdapterPosition(momentsPositionInList)
        val momentsItemView = momentsHolder?.itemView

        if (momentsHolder == null || momentsItemView == null) {
            onViewPagerSwipeStateChangeListener?.onMomentsBlockPositionChanged(
                momentsBlockCoords = null,
                currentRoadType = getRoadType()
            )
            return
        }

        calculateMomentsBlockPositionHandler?.removeCallbacksAndMessages(null)
        calculateMomentsBlockPositionHandler?.postDelayed({
            val momentsBlockCoords = Rect()
            momentsItemView.getGlobalVisibleRect(momentsBlockCoords)

            onViewPagerSwipeStateChangeListener?.onMomentsBlockPositionChanged(
                momentsBlockCoords = momentsBlockCoords,
                currentRoadType = getRoadType()
            )
        }, MOMENTS_BLOCK_POSITION_CALCULATE_DELAY)
    }

    fun handleNetworkState(state: NetworkState.Status) {
        if (state == NetworkState.Status.FAILED) showErrorMessageLoadPosts()
    }

    private fun showErrorMessageLoadPosts() =
        showToastMessage(getString(R.string.error_load_posts), messageState = AvatarUiState.ErrorIconState)

    private fun gotoPostFragment(
        postItem: PostUIEntity,
        adapterPosition: Int
    ) {
        val isVolumeEnabled = recyclerView?.isVolumeEnabled() ?: false
        val videoPosition = VideoUtil.getVideoPosition(
            feedAdapter = feedAdapter,
            feedRecycler = recyclerView,
            position = adapterPosition,
            post = postItem
        )
        val needToShowBlockBtn = !postItem.isPrivateGroupPost

        val args = Bundle().apply {
            putSerializable(ARG_FEED_POST_ID, postItem.postId)
            putSerializable(ARG_TIME_MILLS, videoPosition)
            putSerializable(ARG_FEED_POST_POSITION, adapterPosition)
            putSerializable(ARG_FEED_ROAD_TYPE, getRoadType().index)
            putSerializable(ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled)
            putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType()))
            putSerializable(ARG_NEED_TO_REPOST, needToShowBlockBtn)
            putParcelable(ARG_FEED_POST, postItem)
        }
        findNavController().safeNavigate(R.id.action_mainRoadFragment_to_meeraPostFragmentV2, args)
    }

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            findNavController().safeNavigate(
                resId = R.id.action_mainRoadFragment_to_meeraProfilePhotoViewerFragment,
                bundle = bundleOf(
                    IArgContainer.ARG_IS_PROFILE_PHOTO to false,
                    IArgContainer.ARG_IS_OWN_PROFILE to false,
                    IArgContainer.ARG_POST_ID to post.postId,
                    IArgContainer.ARG_GALLERY_ORIGIN to DestinationOriginEnum.fromRoadType(getRoadType())
                )
            )
        } else {
//            add(
//                ViewContentFragment(),
//                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                Arg(ARG_VIEW_CONTENT_DATA, post),
//                Arg(
//                    ARG_PHOTO_WHERE,
//                    DestinationOriginEnum.fromRoadType(getRoadType()).toAmplitudePropertyWhere()
//                ),
//                Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType()))
//            )
        }
    }

    private fun goToVideoPostFragment(postItem: PostUIEntity, adapterPosition: Int) {
        val videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = recyclerView,
            position = adapterPosition,
            post = postItem
        )
        stopVideoIfExist()
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_meeraViewVideoFragment,
            bundle = Bundle().apply
            {
                putLong(ARG_VIEW_VIDEO_POST_ID, postItem.postId)
                putParcelable(ARG_VIEW_VIDEO_POST, postItem)
                putSerializable(ARG_VIEW_VIDEO_DATA, videoData)
                putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType()))
                putBoolean(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
            })
    }

    private fun goToMultimediaPostViewFragment(
        postItem: PostUIEntity,
        mediaAsset: MediaAssetEntity,
        adapterPosition: Int
    ) {
        var videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = recyclerView,
            position = adapterPosition,
            post = postItem
        )
        videoData = videoData.copy(id = mediaAsset.id)
        stopVideoIfExist()
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_meeraViewMultimediaFragment,
            bundle = Bundle().apply
            {
                putLong(ARG_VIEW_MULTIMEDIA_POST_ID, postItem.postId)
                putString(ARG_VIEW_MULTIMEDIA_ASSET_ID, mediaAsset.id)
                putString(ARG_VIEW_MULTIMEDIA_ASSET_TYPE, mediaAsset.type)
                putParcelable(ARG_VIEW_MULTIMEDIA_DATA, postItem)
                putSerializable(ARG_VIEW_MULTIMEDIA_VIDEO_DATA, videoData)
                putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType()))
                putBoolean(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
            })
    }

    fun triggerPostsAction(action: FeedViewActions) {
        viewModel.onTriggerAction(action)
    }

    private fun showPostDotsMenu(
        post: PostUIEntity,
        adapterPosition: Int,
        isEditAvailable: Boolean = false,
        currentMedia: MediaAssetEntity?
    ) {
        needAuthToNavigate {
            //TODO ROAD_FIX
//            if (viewModel.isNeedToShowOnBoarding()) return@needAuthToNavigate
            val postId = post.postId
            val selectedPostId: Long = post.postId
            val isPostAuthor = post.user?.userId == viewModel.getUserUid()
            val menu = MeeraMenuBottomSheet(context)
            val postCreatorUid = post.user?.userId
            selectedUserId = postCreatorUid

            if (isEditAvailable) {
                menu.addItem(
                    title = getString(R.string.general_edit),
                    icon = R.drawable.ic_outlined_edit_m,
                    bottomSeparatorVisible = true,
                ) {
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.CHANGE,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.EditPost(post = post))
                }
            }

            addSavingMediaItemsToMenu(menu, postCreatorUid, currentMedia, post)

            // Подписаться / Отписаться
            if (!isPostAuthor) {
                val img = if (post.isPostSubscribed) R.drawable.ic_outlined_post_delete_m
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
                        viewModel.logPostMenuAction(
                            post = post,
                            action = AmplitudePropertyMenuAction.POST_FOLLOW,
                            authorId = postCreatorUid,
                        )
                        triggerPostsAction(
                            FeedViewActions.SubscribeToPost(
                                selectedPostId,
                                PostSubscribeTitle.SubscribeString()
                            )
                        )
                    } else if (post.isPostSubscribed) {
                        viewModel.logPostMenuAction(
                            post = post,
                            action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                            authorId = postCreatorUid,
                        )
                        triggerPostsAction(
                            FeedViewActions.UnsubscribeFromPost(
                                selectedPostId,
                                PostSubscribeTitle.SubscribeString()
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
                        viewModel.logPostMenuAction(
                            post = post,
                            action = AmplitudePropertyMenuAction.USER_FOLLOW,
                            authorId = postCreatorUid,
                        )
                        triggerPostsAction(
                            FeedViewActions.SubscribeToUser(
                                postId = post.postId,
                                userId = post.user.userId,
                                needToHideFollowButton = false,
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
                    onRepostClicked(post)
                }
                menu.addItem(
                    title = R.string.copy_link,
                    icon = R.drawable.ic_outlined_copy_m,
                    bottomSeparatorVisible = true
                ) {
                    triggerPostsAction(FeedViewActions.CopyPostLink(post.postId))
                }
            }

            if (!isPostAuthor) {
                if (post.user?.isSystemAdministrator != true && post.user?.subscriptionOn.toBoolean().not()) {
                    menu.addItem(
                        title = R.string.profile_complain_hide_all_posts,
                        icon = R.drawable.ic_outlined_eye_off_m,
                        iconAndTitleColor = R.color.uiKitColorAccentWrong
                    ) {
                        viewModel.logPostMenuAction(
                            post = post,
                            action = AmplitudePropertyMenuAction.HIDE_USER_POSTS,
                            authorId = postCreatorUid,
                        )
                        triggerPostsAction(FeedViewActions.HideUserRoads(post.user?.userId))
                    }
                }

                // Жалоба
                val complainTitleResId =
                    if (post.event != null) R.string.complain_about_event_post else R.string.complain_about_post
                menu.addItem(
                    title = complainTitleResId,
                    icon = R.drawable.ic_outlined_attention_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_REPORT,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.ComplainToPost(postId))
                }
            }

            // Удалить
            if (isPostAuthor) {
                menu.addItem(
                    R.string.road_delete, R.drawable.ic_outlined_delete_m,
                    iconAndTitleColor = R.color.uiKitColorAccentWrong
                ) {
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.DELETE,
                        authorId = postCreatorUid,
                    )
                    viewModel.logDeletedPost(post.toPost().toAnalyticPost(), AmplitudePropertyWhere.FEED)
                    triggerPostsAction(FeedViewActions.DeletePost(post, adapterPosition))
                    feedAdapter.stopCurrentAudio(adapterPosition)
                }
            }

            dotsMenuPost = post
            menu.show(childFragmentManager)

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
            viewModel.logPostMenuAction(
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
            viewModel.logPostMenuAction(
                post = post,
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = postCreatorUid,
                saveType = AmplitudePropertySaveType.VIDEO
            )
            saveVideo(post.postId, mediaId)
        }
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.downloadPostVideo(postId, assetId)
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

    private fun handleTagClicked(
        clickType: SpanDataClickType,
        position: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity? = null
    ) {
        clickCheckBubble {
            when (clickType) {
                is SpanDataClickType.ClickBadWord -> {
                    handleBadWord(clickType, position, tagOrigin, post)
                }

                is SpanDataClickType.ClickHashtag -> needAuthToNavigate {
                    handleHashtagClick(clickType.hashtag, post?.postId ?: 0, post?.user?.userId ?: 0)
                }

                is SpanDataClickType.ClickUserId -> {
                    gotoUserProfileFragment(clickType.userId)
                }

                is SpanDataClickType.ClickLink -> {
                    act.emitDeeplinkCall(clickType.link)
                }

                else -> {}
            }
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
            tagSpan.shortText = shortText.replaceRange(clickType.startIndex, clickType.endIndex, badWord)
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
        Timber.d("basePostHolder handleBadWord clickType = ${tagSpan}")
        val newItem = when (tagOrigin) {
            TagOrigin.POST_TEXT -> post?.copy(tagSpan = tagSpan)
            TagOrigin.POST_TITLE -> post?.copy(event = post.event?.copy(tagSpan = tagSpan))
        }
        newItem?.let {
            val postUpdate = UIPostUpdate.UpdateTagSpan(postId = it.postId, post = it)
            feedAdapter.updateItem(adapterPos = position, payload = postUpdate)
        }
    }

    private fun showUnsubscribeConfirmDialog(
        postId: Long?,
        selectedUserId: Long?,
        clearPost: Boolean = true,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {

        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_dialog_description))
            .setTopBtnText(getString(R.string.unsubscribe))
            .setBottomBtnText(getString(R.string.general_cancel))
            .setCancelable(true)
            .setTopClickListener {
                selectedUserId?.let {
                    if (clearPost) {
                        triggerPostsAction(FeedViewActions.UnsubscribeFromUserAndClear(postId, it))
                    } else {
                        triggerPostsAction(
                            FeedViewActions.UnsubscribeFromUser(
                                postId = postId,
                                userId = it,
                                fromFollowButton = fromFollowButton,
                                isApproved = isApproved,
                                topContentMaker = topContentMaker
                            )
                        )
                    }
                }
            }
            .show(childFragmentManager)
    }

    override fun onDestroyView() {
        currentFragment = null
        onViewPagerSwipeStateChangeListener = null
        onRoadScrollListener = null
        viewModel.livePosts.removeObservers(viewLifecycleOwner)
        postCollisionDetector?.release()
        postCollisionDetector = null
        feedAdapter.onDestroyView()
        saveRecyclerPosition()
        clearFeedRecycler()
        reactionAnimationHelper?.clearData()
        featureTogglesContainer = null
        formatterProvider = null
        sensitiveContentManager = null
        cacheUtil = null
        zoomyProvider = null
        reactionAnimationHelper = null
        dotsMenuPost = null
        blurHelper?.cancel()
        blurHelper = null
        postDisposable?.dispose()
        formatterProvider = null
        _binding = null
        (activity as? MeeraAct)?.getMeeraStatusToastViewController()?.hideStatusToast()
        super.onDestroyView()
    }

    private fun saveRecyclerPosition() {
        val firstVisible = layoutManager?.findFirstVisibleItemPosition()?:0
        val offset = recyclerView?.getChildAt(0)?.top ?: 0
        viewModel.savePositionAndScrollOffset(firstVisible, offset)
    }

    private fun clearFeedRecycler() {
        recyclerDecoration?.let {
            recyclerView?.removeItemDecoration(it)
            recyclerDecoration = null
        }
        loadScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
            loadScrollListener = null
        }
        glidePreloaderScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
            glidePreloaderScrollListener = null
        }
        videoPreloaderScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
            videoPreloaderScrollListener = null
        }
        recyclerView?.release()
        recyclerView?.layoutManager = null
        layoutManager = null
        recyclerView?.adapter = null
        recyclerView = null
    }

    private fun gotoUserProfileFragment(
        userId: Long?,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.FEED
    ) {
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to where
            )
        )
    }

    private fun isMomentsDisabled(): Boolean =
        (activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false

    private fun handleHashtagClick(
        hashtag: String?,
        postId: Long,
        authorId: Long
    ) {
        viewModel.logPressHashTag(requireNotNull(getAmplitudeWhereFromOpened()), postId, authorId)
        needAuthToNavigate {
            Timber.e("$hashtag")
            findNavController().safeNavigate(
                resId = R.id.action_mainRoadFragment_to_meeraHashTagFragment,
                bundle = bundleOf(IArgContainer.ARG_HASHTAG to hashtag)
            )
        }
    }

    private fun handleClickCommunityView(groupId: Long?) {
        needAuthToNavigate {
            groupId?.let { id: Long ->
                findNavController().safeNavigate(
                    resId = R.id.action_mainRoadFragment_to_meeraCommunityRoadFragment,
                    bundle = Bundle().apply {
                        putInt(IArgContainer.ARG_GROUP_ID, id.toInt())
                    }
                )
                viewModel.getAmplitudeHelper().logCommunityScreenOpened(AmplitudePropertyWhereCommunityOpen.FEED)
            }
        }
    }

    private fun handleFeatureClicked(featureId: Long, dismiss: Boolean, deepLink: String?) =
        needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
            if (dismiss) viewModel.getAmplitudeHelper().logUnderstandablyPress()
            viewModel.onTriggerAction(FeedViewActions.FeatureClick(featureId, dismiss, deepLink))
        }

    private fun handleMarketClicked() {
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
    }

    /**
     * Repost button clicked
     */
    private fun handleRepostClick(post: PostUIEntity) {
        needAuthToNavigate {
            meeraOpenRepostMenu(post)
        }
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        MeeraShareSheet().show(
            fm = childFragmentManager,
            data = MeeraShareBottomSheetData(
                post = post.toPost(),
                postOrigin = DestinationOriginEnum.fromRoadType(getRoadType()),
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
                        viewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.repostSuccess(post)
                        showToastMessage(getString(R.string.success_repost_to_own_road))
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showToastMessage(getString(strResId))
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        gotoUserProfileFragment(userId, AmplitudePropertyWhere.FEED)
                    }
                }
            )
        )
    }

    private fun openGroups() {
        findNavController().safeNavigate(R.id.action_mainRoadFragment_to_meeraCommunitiesListsContainerFragment)
    }

    private fun openSearch() {
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_meeraSearchFragment,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.SHARE
                )
            }
        )
    }

    fun showEmptyPostsPlaceholder(type: RoadTypeEnum) {
        val placeholder = childView?.findViewById<LinearLayout>(R.id.ll_empty_list_container) ?: return

        placeholder.clearAnimation()

        placeholder
            .animate()
            .setStartDelay(SHOW_PLACE_HOLDER_DELAY)
            .setDuration(SHOW_PLACE_HOLDER_DURATION)
            .alpha(1f)

        when (type) {
            RoadTypeEnum.SUBSCRIPTIONS_ROAD -> subscriptionsRoadPlaceholderConfig()
            RoadTypeEnum.MAIN_ROAD -> mainRoadPlaceholderConfig()
            else -> Unit
        }
    }

    fun hideEmptyPostsPlaceholder() {
        val placeholder = childView?.findViewById<LinearLayout>(R.id.ll_empty_list_container) ?: return

        placeholder.clearAnimation()

        placeholder
            .animate()
            .setStartDelay(0)
            .setDuration(HIDE_PLACE_HOLDER_DURATION)
            .alpha(0f)
    }

    private fun setupPlaceholderMargin(posts: List<PostUIEntity>) {
        val placeholder = childView?.findViewById<LinearLayout>(R.id.ll_empty_list_container) ?: return
        val hasMomentsItem = posts.any { it.postId == MOMENTS_POST_ID }
        placeholder.layoutParams = (placeholder.layoutParams as ViewGroup.MarginLayoutParams).apply {
            emptyPostsTopMargin = if (hasMomentsItem) MARGIN_PLACEHOLDER_WITH_MOMENTS.dp else 0
            topMargin = emptyPostsTopMargin
        }
    }

    private fun subscriptionsRoadPlaceholderConfig() {
        childView?.findViewById<TextView>(R.id.tv_empty_posts_description)
            ?.setText(R.string.meera_subscriptions_road_empty_description)
        childView?.findViewById<UiKitButton>(R.id.vg_subscription_placeholder_button)?.text =
            getString(R.string.meera_subscriptions_road_empty_find_friends)
        childView?.findViewById<UiKitButton>(R.id.vg_subscription_placeholder_button)?.setThrottledClickListener {
            openPeoples()
        }
    }

    private fun openPeoples() {
        findNavController().safeNavigate(R.id.action_mainRoadFragment_to_peoplesFragment)
    }

    private fun mainRoadPlaceholderConfig() {
        childView?.findViewById<TextView>(R.id.tv_empty_posts_description)
            ?.setText(R.string.meera_no_posts_change_filter)
        childView?.findViewById<UiKitButton>(R.id.vg_subscription_placeholder_button)?.text =
            getString(R.string.meera_main_road_empty_change_filters)
        childView?.findViewById<UiKitButton>(R.id.vg_subscription_placeholder_button)?.setThrottledClickListener {
            (parentFragment as? MainRoadFragment?)?.openFilters()
        }
    }

    abstract fun onFeedBtnClicked()

    fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }

    private fun handleScrollRefreshButton(isVisible: Boolean) {
        val canShowRefreshButton = canShowRefreshTopButtonView()
        val isShowRefreshTopButton = isVisible && canShowRefreshButton

        if (isShowRefreshTopButton) {
            showRefreshTopButton()
        } else {
            hideRefreshTopButton()
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

    protected open fun getRefreshTopButtonView(): View? {
        return null
    }

    protected open fun canShowRefreshTopButtonView(): Boolean {
        return true
    }

    /**
     * Счетчик записывает в RxPref количество просмотренных постов
     **/
    protected fun initPostCounter() = Unit

    fun initRefreshViewClickListener(refreshView: View) {
        refreshView.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                refreshRoadAfterScrollRefresh()
            }
        }
    }

    open fun refreshRoadAfterScrollRefresh() {
        NavigationManager.getManager().getTopBehaviour()?.state =
            BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    open fun updateTopMarginEmptyPostsView(marginTop: Int) {
        val placeholder = childView?.findViewById<LinearLayout>(R.id.ll_empty_list_container) ?: return
        placeholder.layoutParams = (placeholder.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = emptyPostsTopMargin + marginTop
        }
    }

    private fun updateMenuWitEdit(postId: Long, isAvailable: Boolean, currentMedia: MediaAssetEntity?) {
        val adapter = feedAdapter ?: return
        val position = adapter.getPositionById(postId)
        val post = adapter.getItem(position) ?: return
        showPostDotsMenu(
            post = post,
            adapterPosition = position,
            isEditAvailable = isAvailable,
            currentMedia = currentMedia
        )
    }

    private fun navigateToEditPost(post: PostUIEntity) {
        navigateEditPostFragment(post)
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

    private fun clickCheckBubble(click: () -> Unit) {
        if (isBubbleNotExist()) {
            click()
        }
    }

    private fun isBubbleNotExist(): Boolean {
        val act = context as? MeeraAct ?: return false
        val bubble =
            (act.getRootView() as? ViewGroup)?.children?.find { it is MeeraReactionBubble } as? MeeraReactionBubble
        return bubble == null
    }

}
