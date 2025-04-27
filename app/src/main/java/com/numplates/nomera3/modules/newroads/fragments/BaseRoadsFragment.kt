package com.numplates.nomera3.modules.newroads.fragments

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.common.MEERA_APP_SCHEME
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.gone
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
import com.meera.core.extensions.textColor
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.convertUnixDate
import com.meera.core.utils.pagination.RecyclerPaginationListener
import com.meera.db.models.message.UniquenameSpanData
import com.meera.referrals.ui.ReferralFragment
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.noomeera.nmravatarssdk.ui.positiveButton
import com.numplates.nomera3.ACTION_AFTER_SUBMIT_LIST_DELAY
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.auth.util.needAuth
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
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAnalyticPost
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityTransitFrom
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.FeedAdapter
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhere
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.feed.ui.util.divider.PostDividerDecoration
import com.numplates.nomera3.modules.feed.ui.util.preloader.getPreloader
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
import com.numplates.nomera3.modules.maps.ui.events.participants.openEventParticipantsList
import com.numplates.nomera3.modules.maps.ui.model.MainMapOpenPayload
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.FeatureDeepLink
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.newroads.util.FixedScrollingLinearLayoutManager
import com.numplates.nomera3.modules.peoples.ui.delegate.SyncContactsDialogDelegate
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
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
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.share.ui.model.SharingDialogMode
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
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
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_HASHTAG
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_OWN_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_PROFILE_PHOTO
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_NEED_TO_REPOST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PHOTO_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TIME_MILLS
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_COMMUNITY_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.fragments.MainFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.ui.FeedRecyclerView
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

const val SHOW_PLACE_HOLDER_DELAY = 650L
const val SHOW_PLACE_HOLDER_DURATION = 250L
const val HIDE_PLACE_HOLDER_DURATION = 150L
const val DELAY_START_VIDEO = 300L
const val DELAY_AFTER_FOLLOW_AUTH_MS = 1000L
private const val MOMENTS_BLOCK_POSITION_CALCULATE_DELAY = 500L
private const val MEERA_POST_DOTS_MENU_BOTTOM_SHEET_DIALOG = "MEERA_POST_DOTS_MENU_BOTTOM_SHEET_DIALOG"
private const val FONT_SANS_PRO_BOLD = "fonts/source_sanspro_bold"

abstract class BaseRoadsFragment<T : ViewBinding> :
    BaseFragmentNew<T>(),
    PostCallback,
    VolumeStateCallback,
    MeeraMenuBottomSheet.Listener,
    BasePermission by BasePermissionDelegate(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl() {

    private var postDisposable: Disposable? = null

    val viewModel by viewModels<FeedViewModel> { App.component.getViewModelFactory() }
    protected var feedAdapter: FeedAdapter? = null
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }

    private var selectedPostId: Long? = 0
    private var selectedUserId: Long? = 0

    private val syncContactsDialogDelegate: SyncContactsDialogDelegate by lazy { SyncContactsDialogDelegate(childFragmentManager) }

    private var recyclerView: FeedRecyclerView? = null

    private var isRefreshButtonShown = false

    private lateinit var audioFeedHelper: AudioFeedHelper

    private var postCollisionDetector: PostCollisionDetector? = null

    //works once then it's null
    private var postFirstLoadCallback: (() -> Unit)? = null

    private var dotsMenuPost: PostUIEntity? = null

    enum class RoadTypeEnum(var index: Int) {
        MAIN_ROAD(0),
        CUSTOM_ROAD(1),
        SUBSCRIPTIONS_ROAD(2)
    }

    var currentFragment: MainPostRoadsFragment.OnCurrentFragmentListener? = null

    var onViewPagerSwipeStateChangeListener: MainPostRoadsFragment.OnViewPagerSwipeStateChangeListener? = null

    private var calculateMomentsBlockPositionHandler: Handler? = null

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

    abstract fun stopVideoIfExist()

    abstract fun controlAlreadyPlayingVideo()

    private val childView: View?
        get() = getRootView()

    fun getRoadVerticalScrollPosition(): Int {
        return recyclerView?.computeVerticalScrollOffset() ?: 0
    }

    fun resetAllZoomViews() {
        recyclerView?.apply {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioFeedHelper = App.component.getAudioFeedHelper()
        val roadType = when (getRoadType()) {
            RoadTypeEnum.MAIN_ROAD -> RoadTypesEnum.MAIN
            RoadTypeEnum.CUSTOM_ROAD -> RoadTypesEnum.CUSTOM
            RoadTypeEnum.SUBSCRIPTIONS_ROAD -> RoadTypesEnum.SUBSCRIPTION
        }
        viewModel.initRoadType(type = roadType, originEnum = DestinationOriginEnum.fromRoadType(getRoadType()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initMomentsBlockHandler()
        initPostsLiveObservable()
        initFeatureToggleObservable()
    }

    override fun onResume() {
        super.onResume()
        subscribePostRx()
    }

    override fun onFindPeoplesClicked() {
        add(
            PeoplesFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_SHOW_SWITCHER, false),
        )
    }

    override fun onCancelByUser(menuTag: String?) {
        viewModel.logPostMenuAction(
            post = dotsMenuPost ?: return,
            action = AmplitudePropertyMenuAction.CANCEL,
            authorId = selectedUserId,
        )
        dotsMenuPost = null
    }

    fun setRoadMode(mode: MainRoadMode) {
        recyclerView?.currentRoadMode = mode
    }

    override fun onShowMoreSuggestionsClicked() {
        add(
            PeoplesFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_SHOW_SWITCHER, false)
        )
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
        add(
            UserInfoFragment(),
            Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, toUserId),
            Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.SUGGEST_MAIN_FEED.property)
        )
    }

    override fun onReferralClicked() {
        add(ReferralFragment(), Act.LIGHT_STATUSBAR)
        viewModel.logOpenReferral()
    }

    override fun onActivateVipClicked(data: ReferralDataUIModel) {
        viewModel.startActivatingVip(data)
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
        if (isFragmentStarted.not() || isFragmentAdding) return
        startVideoIfExist()
    }

    override fun forceStartPlayingVideoRequested() {
        forceStartVideo()
    }

    override fun onStopPlayingVideoRequested() {
        stopVideoIfExist()
    }

    override fun onMediaExpandCheckRequested() {
        initExpandMedia()
    }

    override fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {
        viewModel.onTriggerAction(FeedViewActions.UpdatePostSelectedMediaPosition(postId, selectedMediaPosition))
    }

    private fun showDialogGetVip(vipUntilDate: Long, descriptionStringId: Int) {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(com.meera.referrals.R.string.referral_get_vip_dialog_title))
            setMessage(getString(descriptionStringId, convertUnixDate(vipUntilDate)))
            setCancelable(false)
            setPositiveButton(com.meera.referrals.R.string.general_activate) { dialog, _ ->
                viewModel.getVipReferral(vipUntilDate)
                dialog.cancel()
            }
            setNegativeButton(com.meera.referrals.R.string.general_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
        val message = dialog.findViewById<TextView>(android.R.id.message)
        message?.textColor(ContextCompat.getColor(requireContext(), com.meera.referrals.R.color.colorGrayA7A5))
        message?.typeface = Typeface.createFromAsset(requireContext().assets, FONT_SANS_PRO_BOLD)
        dialog.show()
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

    override fun onAddMomentClicked() = needAuth {
        act.getMomentsViewController().open()
    }

    override fun onShowMomentsClicked(
        startGroupId: Long,
        view: View?,
        isViewed: Boolean
    ) {
        if(isMomentsDisabled()) {
            return
        }
        act.openUserMoments(
            startGroupId = startGroupId,
            fromView = view,
            openedFrom = MomentClickOrigin.fromRoadType(getRoadType()),
            openedWhere = getAmplitudeWhereMomentOpened(fromUser = false),
            viewedEarly = isViewed
        )
    }

    override fun onMomentClicked(moment: MomentItemUiModel) {
        onShowMomentsClicked(isViewed = moment.isViewed)
    }

    override fun onMomentGroupClicked(
        momentGroup: MomentGroupUiModel,
        view: View?,
        isViewed:Boolean
    ) {
         onShowMomentsClicked(
             momentGroup.id,
             view,
             isViewed)
    }

    override fun onMomentGroupLongClicked(momentGroup: MomentGroupUiModel) {
        needAuth {
            val menu = MeeraMenuBottomSheet(context)
            menu.addDescriptionItem(
                R.string.moments_bottom_menu_hide_user,
                R.drawable.ic_eye_off_all_menu_item_red,
                R.string.moments_bottom_menu_hide_user_description
            ) {
                viewModel.hideUserMoments(momentGroup.userId)
            }

            menu.show(childFragmentManager)
        }
    }

    override fun onUpdateMomentsClicked() {
        viewModel.getMomentDelegate().initialLoadMoments(scrollToStart = true)
    }

    private fun initPostsLiveObservable() {
        viewModel.livePosts.observe(viewLifecycleOwner) { posts ->
            hideRefreshLayoutProgress()
            feedAdapter?.submitList(posts)
            recyclerView?.invalidateItemDecorations()
            getProgress()?.gone()
            onViewPagerSwipeStateChangeListener?.requestCalculateMomentsBlockPosition(getRoadType())
            recyclerView?.postDelayed({ controlAlreadyPlayingVideo() }, ACTION_AFTER_SUBMIT_LIST_DELAY)
        }
        viewModel.liveEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)

        // обновление через payloads
        viewModel.liveFeedEvents.observe(viewLifecycleOwner) { event ->
            when (event) {
                is FeedViewEventPost.UpdatePostEvent -> feedAdapter?.updateItem(
                    event.post,
                    event.adapterPosition
                )
                is FeedViewEventPost.UpdatePosts -> feedAdapter?.submitList(
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
        feedAdapter?.updateVolumeState(
            volumeState = volumeState,
            visiblePositions = recyclerView?.getVisiblePositions()
        )

        if (volumeState == VolumeState.ON) audioFeedHelper.stopPlaying()
    }

    private fun handleUpdatePostValues(uiPostUpdate: UIPostUpdate) {
        feedAdapter?.updateItem(uiPostUpdate)
    }

    private fun initFeatureToggleObservable() {
        act.activityViewModel.onFeatureTogglesLoaded.observe(viewLifecycleOwner) {
            viewModel.initLoadMoments()
        }
    }

    private fun handlePostViewEvents(event: FeedViewEvent) {
        when (event) {
            is FeedViewEvent.FailChangeLikeStatus -> showCommonError()
            is FeedViewEvent.ShowCommonError -> showCommonError(event.messageResId)
            is FeedViewEvent.ShowCommonSuccess -> showMessage(getString(event.messageResId))
            is FeedViewEvent.OnSuccessHideUserRoad -> showMessage(getString(event.messageResId))
            is FeedViewEvent.UpdatePostById -> feedAdapter?.updateItemByPostId(event.postId)
            is FeedViewEvent.LikeChangeVibration -> {
                requireContext().vibrate()
            }
            is FeedViewEvent.OpenDeepLink -> handleDeeplink(event.deepLink)
            is FeedViewEvent.OnShowLoader -> feedAdapter?.showLoader(event.show)
            is FeedViewEvent.ShowCommonErrorString -> showError(event.message)
            is FeedViewEvent.OnFirstPageLoaded -> handleFirstPageLoaded()
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
            is FeedViewEvent.ScrollMomentsToStart -> scrollMomentsToStart()

            is FeedViewEvent.RequestContactsPermission -> requestContactsPermission()
            is FeedViewEvent.ShowSyncDialogPermissionDenied -> showSyncDialogPermissionDenied()
            is FeedViewEvent.ShowContactsHasBeenSyncDialog -> showContactsHasBeenSyncDialog()
            is FeedViewEvent.ShowGetVipDialog -> showDialogGetVip(event.vipUntilDate, event.descriptionTextId)
            is FeedViewEvent.OnSuccessGetVip -> handleSuccessGetVip(event.vipUntilDate)
            is FeedViewEvent.OnFailGetVip -> {
                showError(getString(com.meera.referrals.R.string.referral_vip_activated_fail))
            }
            is FeedViewEvent.PostEditAvailableEvent -> updateMenuWitEdit(
                postId = event.post.postId,
                isAvailable = event.isAvailable,
                currentMedia = event.currentMedia
            )


            is FeedViewEvent.OpenEditPostEvent -> navigateToEditPost(post = event.post)
            is FeedViewEvent.ShowAvailabilityError -> showNotAvailableError(event.reason)
            else -> Unit
        }
    }

    private fun handleDeeplink(deepLink: String) {
        if (!deepLink.contains(MEERA_APP_SCHEME)) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)))
        } else {
            val deeplinkWithOrigin =
                FeatureDeepLink.addDeeplinkOrigin(deepLink, DeeplinkOrigin.ANNOUNCEMENT)
            act.handleFeatureDeepLink(deeplinkWithOrigin)
        }
    }

    private fun scrollMomentsToStart() {
        val momentsPosition = feedAdapter?.getMomentsItemPosition()?.takeIf { it != -1 }
        momentsPosition?.let {
            val momentsViewHolder = this.recyclerView?.findViewHolderForAdapterPosition(it)
            feedAdapter?.scrollMomentsToStart(momentsViewHolder)
        }
    }

    private fun showContactsHasBeenSyncDialog() {
        if (!isFragmentStarted) return
        if (this !is MainRoadFragment) return
        syncContactsDialogDelegate.showSyncContactsDialog(
            labelRes = R.string.ready,
            descriptionRes = R.string.contacts_has_been_synchronized,
            positiveButtonRes = R.string.general_great,
            iconRes = if (IS_APP_REDESIGNED) R.drawable.meera_ic_sync_contacts_done else R.drawable.ic_sync_contacts_done
        )
        viewModel.logSyncContactsClicked()
    }

    private fun handleSuccessGetVip(vipUntilDate: Long) {
        com.meera.core.utils.NToast.with(view)
            .typeSuccess()
            .text(getString(com.meera.referrals.R.string.referral_vip_activated_until, convertUnixDate(vipUntilDate)))
            .show()
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
        doDelayed(DELAY_START_VIDEO) {
            val isRoadsContainerShowed = act.getCurrentFragment() is MainPostRoadsFragment
            val isMainRoadSelected = getRoadType() == RoadTypeEnum.MAIN_ROAD
            if (isMainRoadSelected && isRoadsContainerShowed) {
                postFirstLoadCallback?.invoke()
                postFirstLoadCallback = null
            }
        }
    }

    private fun registerComplaintListener() {
        complainsNavigator.registerAdditionalActionListener(this) { result ->
            when {
                result.isSuccess -> showAdditionalStepsForComplain(result.getOrThrow())
                result.isFailure -> showError(context?.string(R.string.user_complain_error))
            }
        }
    }

    private fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    private fun showError(msg: String?) {
        NToast.with(view)
            .typeError()
            .text(msg)
            .show()
    }

    private fun showMessage(msg: String?) = doOnUIThread {
        NToast.with(view)
            .typeSuccess()
            .text(msg)
            .show()
    }

    private fun copyLink(link: String) {
        copyCommunityLink(context, link) {
            (requireActivity() as? ActivityToolsProvider)
                ?.getTooltipController()
                ?.showSuccessTooltip(R.string.copy_link_success)
        }
    }

    private fun showAdditionalStepsForComplain(userId: Long) {
        val bottomSheet = UserComplainAdditionalBottomSheet.newInstance(userId).apply {
            callback = object : AdditionalComplainCallback {
                override fun onSuccess(msg: String?, reason: ComplainEvents) {
                    showMessage(msg)
                }

                override fun onError(msg: String?) {
                    showError(msg)
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

    fun initRecycler(recyclerView: FeedRecyclerView) {
        initPostViewCollisionDetector(recyclerView)

        recyclerView.setHasFixedSize(true)
        this.recyclerView = recyclerView
        this.recyclerView?.addItemDecoration(PostDividerDecoration.build(requireContext()))
        (recyclerView as? FeedRecyclerView)?.audioFeedHelper = audioFeedHelper

        // При нажатии на ячейку холдера RecyclerView может автоматичекски доскроливать к самому
        // холдеру. Сам скролл происходит в родительском методе requestChildRectangleOnScreen
        // Для того, что-бы избежать автоскролл необходимо переопределить данный метод
        val layoutManager: FixedScrollingLinearLayoutManager = object : FixedScrollingLinearLayoutManager(act) {
            override fun requestChildRectangleOnScreen(
                parent: RecyclerView,
                child: View, rect: Rect,
                immediate: Boolean,
                focusedChildVisible: Boolean
            ) = false
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator?.changeDuration = 0
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        val formatterProvider = AllRemoteStyleFormatter(viewModel.getSettings())
        val blurHelper = BlurHelper(context = requireContext(), lifecycle = viewLifecycleOwner.lifecycle)
        val zoomyProvider = Zoomy.ZoomyProvider { Zoomy.Builder(act) }
        val momentCallback = object :
            com.numplates.nomera3.modules.moments.show.presentation.MomentCallback {
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
        }

        recyclerView.setOnViewPagerSwipeStateChangeListener(onViewPagerSwipeStateChangeListener)
        recyclerView.setVolumeStateCallback(this)

        feedAdapter = FeedAdapter(
            blurHelper = blurHelper,
            contentManager = getSensitiveContentManager(),
            postCallback = this,
            volumeStateCallback = this,
            zoomyProvider = zoomyProvider,
            cacheUtil = CacheUtil(requireContext()),
            audioFeedHelper = audioFeedHelper,
            formatter = formatterProvider,
            lifecycleOwner = viewLifecycleOwner,
            momentCallback = momentCallback,
            featureTogglesContainer = viewModel.getFeatureTogglesContainer()
        )

        recyclerView.adapter = feedAdapter

        // Scroll listener for app hints
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                parentFragment?.getMainRoadFragment()?.callOnScroll()
                calculateMomentsBlockPosition()
            }
        })

        initGlidePreloader()
        initVideoPreloader()
    }

    private fun initGlidePreloader() = getPreloader(feedAdapter)?.let { preloader ->
        recyclerView?.addOnScrollListener(preloader)
    }

    private fun initVideoPreloader() {
        recyclerView?.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                viewModel.preloadVideoPosts(currentVisiblePostPosition = layoutManager.findLastVisibleItemPosition())
            }
        })
    }

    private var loadScrollListener: RecyclerPaginationListener? = null

    fun loadPosts(withRefresh: Boolean = false) {
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
        val position = feedAdapter?.itemCount ?: 0
        val item = feedAdapter?.getItem(position - 1)
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
        // Clear listeners if exists
        loadScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
        }

        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        loadPostsRequest(0, getNetworkRoadType())
        loadScrollListener = object : RecyclerPaginationListener(layoutManager) {
            override fun loadMoreItems() {
                loadPostsRequest(getStartPostId(), getNetworkRoadType())
            }

            override fun isLastPage(): Boolean = viewModel.isLoading

            override fun isLoading(): Boolean = viewModel.isLastPage

            // Handle scroll animation button
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                observeScrollForRefreshButton(layoutManager, dy)
            }
        }
        loadScrollListener?.let { recyclerView?.addOnScrollListener(it) }

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

    override fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) = needAuth {
        if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
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
            val sortedReactions = reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
            val menu = ReactionsStatisticBottomMenu(context)
            menu.addTitle(R.string.reactions_on_post, sortedReactions.reactionCount())
            sortedReactions.forEachIndexed { index, value ->
                menu.addReaction(value, index != sortedReactions.size - 1)
            }
            menu.show(childFragmentManager)
        }
        val where = if (post.isEvent()) {
            AmplitudePropertyReactionWhere.MAP_EVENT
        } else {
            AmplitudePropertyReactionWhere.POST
        }
        viewModel.logStatisticReactionsTap(
            where = where,
            whence = DestinationOriginEnum.fromRoadType(getRoadType()).toAmplitudePropertyWhence()
        )
    }

    override fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean
    ) {
        val postOrigin = DestinationOriginEnum.fromRoadType(getRoadType())
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        act.getReactionBubbleViewController().onSelectDefaultReaction(
            reactionSource = ReactionSource.Post(
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
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId
    ) {
        val postOrigin = DestinationOriginEnum.fromRoadType(getRoadType())
        val reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
        val actionBarType = ContentActionBar.ContentActionBarType.getType(post.toContentActionBarParams())
        act.getReactionBubbleViewController().showReactionBubble(
            reactionSource = ReactionSource.Post(
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
            gotoUserProfileFragment(
                userId = id
            )
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
        if(isMomentsDisabled()) {
            return
        }
        act.openUserMoments(
            userId = userId,
            fromView = fromView,
            openedWhere = getAmplitudeWhereMomentOpened(fromUser = true),
            viewedEarly = hasNewMoments?.not()
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
        act?.showFireworkAnimation {}
    }

    override fun onCreatePostClicked(withImage: Boolean) {
        val openFrom = when (getRoadType()) {
            RoadTypeEnum.CUSTOM_ROAD -> {
                AddMultipleMediaPostFragment.OpenFrom.SelfRoad
            }

            RoadTypeEnum.MAIN_ROAD -> {
                AddMultipleMediaPostFragment.OpenFrom.MainRoad
            }

            else -> {
                null
            }
        }

        gotoAddPost(withImage, openFrom)
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
        val container = act?.getRootView() as? ViewGroup
        container?.addView(flyingReaction)
        flyingReaction.startAnimationFlying()
        flyingReaction.setFlyingAnimationPlayListener(object : FlyingAnimationPlayListener {
            override fun onFlyingAnimationPlayed(playedFlyingReaction: FlyingReaction) {
                container?.removeView(playedFlyingReaction)
            }
        })
    }

    override fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {
        needAuth { wasLoginAuthorization ->
            if (wasLoginAuthorization) {
                doDelayed(DELAY_AFTER_FOLLOW_AUTH_MS) { followUser(post) }
            } else {
                followUser(post)
            }
        }
    }

    override fun onShowEventOnMapClicked(post: PostUIEntity) {
        val payload = MainMapOpenPayload.EventPayload(post)
        act.mainFragment?.getMainRoadFragment()?.setMapModeWithPayload(payload)
    }

    override fun onNavigateToEventClicked(post: PostUIEntity) {
        openEventNavigation(post)
        viewModel.logMapEventGetTherePress(post)
    }

    override fun onShowEventParticipantsClicked(post: PostUIEntity) = openEventParticipantsList(post)

    override fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {
        viewModel.onJoinAnimationFinished(
            postUIEntity = post,
            adapterPosition = adapterPosition
        )
    }

    override fun onJoinEventClicked(post: PostUIEntity) {
        viewModel.joinEvent(post)
    }

    override fun onLeaveEventClicked(post: PostUIEntity) {
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

    override fun onStartFragment() {
        super.onStartFragment()
        viewModel.logScreenForFragment(this.javaClass.simpleName)
        registerComplaintListener()
        initExpandMedia()
    }

    private fun initExpandMedia() {
        recyclerView?.post {
            recyclerView?.expandMediaIndicatorAction(true, showInstantly = true)
        }
    }

    override fun onStopFragment() {
        super.onStopFragment()
        viewModel.removePostsViewedBlock()
        stopAudio()
        unregisterComplaintListener()
    }

    override fun onStop() {
        super.onStop()
        stopAudio()
        audioFeedHelper.removeAudioPriorityListener(priorityListener)
    }

    override fun onStart() {
        super.onStart()
        audioFeedHelper.addAudioPriorityListener(priorityListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        postFirstLoadCallback = null
    }

    fun stopAudio() {
        audioFeedHelper.stopPlaying(isLifecycleStop = true)
    }

    fun calculateMomentsBlockPosition() {
        val momentsPositionInList = feedAdapter?.getMomentsItemPosition().takeIf { it != -1 }
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

    private fun getSensitiveContentManager(): ISensitiveContentManager =
        object : ISensitiveContentManager {
            override fun isMarkedAsNonSensitivePost(postId: Long?): Boolean {
                return viewModel.isMarkedAsSensitivePost(postId)
            }

            override fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
                viewModel.markPostAsNotSensitiveForUser(postId, parentPostId)
                viewModel.refreshPost(postId)
                viewModel.refreshPost(parentPostId)
            }
        }

    fun gotoAddPost(isShowGallery: Boolean = false, openFrom: AddMultipleMediaPostFragment.OpenFrom?) = needAuth {
        add(
            AddMultipleMediaPostFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_SHOW_MEDIA_GALLERY, isShowGallery),
            Arg(AddMultipleMediaPostFragment.OpenFrom.EXTRA_KEY, openFrom)
        )
    }

    fun handleNetworkState(state: NetworkState.Status) {
        if (state == NetworkState.Status.FAILED) showErrorMessageLoadPosts()
    }

    private fun showErrorMessageLoadPosts() =
        showError(getString(R.string.error_load_posts))

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
        add(
            PostFragmentV2(null),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_FEED_POST_ID, postItem.postId),
            Arg(ARG_FEED_POST, postItem),
            Arg(ARG_TIME_MILLS, videoPosition),
            Arg(ARG_FEED_POST_POSITION, adapterPosition),
            Arg(ARG_FEED_ROAD_TYPE, getRoadType().index),
            Arg(ARG_DEFAULT_VOLUME_ENABLED, isVolumeEnabled),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType())),
            Arg(ARG_NEED_TO_REPOST, needToShowBlockBtn)
        )
    }

    private fun goToContentViewer(post: PostUIEntity) {
        if (post.type == PostTypeEnum.AVATAR_HIDDEN || post.type == PostTypeEnum.AVATAR_VISIBLE) {
            checkAppRedesigned(
                isRedesigned = {
//                    add(
//                        MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                        Arg(ARG_IS_PROFILE_PHOTO, false),
//                        Arg(ARG_IS_OWN_PROFILE, false),
//                        Arg(ARG_POST_ID, post.postId),
//                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType())),
//                    )
                },
                isNotRedesigned = {
                    add(
                        ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                        Arg(ARG_IS_PROFILE_PHOTO, false),
                        Arg(ARG_IS_OWN_PROFILE, false),
                        Arg(ARG_POST_ID, post.postId),
                        Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType())),
                    )
                }
            )
        } else {
            add(
                ViewContentFragment(),
                Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                Arg(ARG_VIEW_CONTENT_DATA, post),
                Arg(
                    ARG_PHOTO_WHERE,
                    DestinationOriginEnum.fromRoadType(getRoadType()).toAmplitudePropertyWhere()
                ),
                Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType()))
            )
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
        add(
            ViewVideoItemFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_VIDEO_POST_ID, postItem.postId),
            Arg(ARG_VIEW_VIDEO_POST, postItem),
            Arg(ARG_VIEW_VIDEO_DATA, videoData),
            Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType())),
            Arg(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
        )
    }

    private fun goToMultimediaPostViewFragment(postItem: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {
        var videoData = VideoUtil.getVideoInitData(
            feedAdapter = feedAdapter,
            feedRecycler = recyclerView,
            position = adapterPosition,
            post = postItem
        )
        videoData = videoData.copy(id = mediaAsset.id)
        stopVideoIfExist()
        add(
            ViewMultimediaFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_VIEW_MULTIMEDIA_POST_ID, postItem.postId),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_ID, mediaAsset.id),
            Arg(ARG_VIEW_MULTIMEDIA_ASSET_TYPE, mediaAsset.type),
            Arg(ARG_VIEW_MULTIMEDIA_DATA, postItem),
            Arg(ARG_VIEW_MULTIMEDIA_VIDEO_DATA, videoData),
            Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.fromRoadType(getRoadType())),
            Arg(ARG_NEED_TO_REPOST, !postItem.isPrivateGroupPost)
        )
    }

    fun triggerPostsAction(action: FeedViewActions) {
        viewModel.onTriggerAction(action)
    }

    private fun showPostDotsMenu(
        post: PostUIEntity,
        adapterPosition: Int,
        isEditAvailable: Boolean = false,
        currentMedia: MediaAssetEntity?
    ) = needAuth {
        if (viewModel.isNeedToShowOnBoarding()) return@needAuth
        val postId = post.postId
        val selectedPostId: Long = post.postId
        val isPostAuthor = post.user?.userId == viewModel.getUserUid()
        val menu = MeeraMenuBottomSheet(context)
        val postCreatorUid = post.user?.userId
        selectedUserId = postCreatorUid

        checkAppRedesigned(
            isRedesigned = {
                MeeraPostDotsMenuBottomDialog(
                    post = post,
                    isEditAvailable = isEditAvailable,
                    isPostAuthor = isPostAuthor,
                    menuItemClick = {
                        postDotsMenuItemClick(
                            action = it,
                            post = post,
                            adapterPosition = adapterPosition,
                            currentMedia = currentMedia
                        )
                    }
                ).show(
                    childFragmentManager,
                    MEERA_POST_DOTS_MENU_BOTTOM_SHEET_DIALOG
                )
            },
            isNotRedesigned = {
                if (isEditAvailable) {
                    menu.addItem(
                        title = getString(R.string.general_edit),
                        icon = R.drawable.ic_edit_purple_plain,
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
                    val img = if (post.isPostSubscribed) R.drawable.ic_unsubscribe_post_menu_purple
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
                        val postSubscribeText = if (post.isEvent()) {
                            PostSubscribeTitle.NotificationString()
                        } else {
                            PostSubscribeTitle.SubscribeString()
                        }
                        if (!post.isPostSubscribed) {
                            viewModel.logPostMenuAction(
                                post = post,
                                action = AmplitudePropertyMenuAction.POST_FOLLOW,
                                authorId = postCreatorUid,
                            )
                            triggerPostsAction(FeedViewActions.SubscribeToPost(selectedPostId, postSubscribeText))
                        } else if (post.isPostSubscribed) {
                            viewModel.logPostMenuAction(
                                post = post,
                                action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                                authorId = postCreatorUid,
                            )
                            triggerPostsAction(FeedViewActions.UnsubscribeFromPost(selectedPostId, postSubscribeText))
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
                        icon = R.drawable.ic_share_purple_new,
                        bottomSeparatorVisible = true
                    ) {
                        onRepostClicked(post)
                    }
                    menu.addItem(
                        title = R.string.copy_link,
                        icon = R.drawable.ic_chat_copy_message,
                        bottomSeparatorVisible = true
                    ) {
                        triggerPostsAction(FeedViewActions.CopyPostLink(post.postId))
                    }
                }

                if (!isPostAuthor) {
                    if (post.user?.isSystemAdministrator != true && post.user?.subscriptionOn.toBoolean().not()) {
                        menu.addItem(
                            title = R.string.profile_complain_hide_all_posts,
                            icon = R.drawable.ic_eye_off_all_menu_item_red,
                            bottomSeparatorVisible = true
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
                        icon = R.drawable.ic_report_profile,
                        bottomSeparatorVisible = true
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
                    menu.addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
                        viewModel.logPostMenuAction(
                            post = post,
                            action = AmplitudePropertyMenuAction.DELETE,
                            authorId = postCreatorUid,
                        )
                        viewModel.logDeletedPost(post.toPost().toAnalyticPost(), AmplitudePropertyWhere.FEED)
                        triggerPostsAction(FeedViewActions.DeletePost(post, adapterPosition))
                        feedAdapter?.stopCurrentAudio(adapterPosition)
                    }
                }

                dotsMenuPost = post
                menu.show(childFragmentManager)
            }
        )
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
                    showMessage(getString(R.string.image_saved))
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
            viewModel.logPostMenuAction(
                post = post,
                action = AmplitudePropertyMenuAction.SAVE,
                authorId = postCreatorUid,
                saveType = AmplitudePropertySaveType.VIDEO
            )
            saveVideo(post.postId, mediaId)
        }
    }

    private fun postDotsMenuItemClick(
        action: MeeraPostDotsMenuAction,
        post: PostUIEntity,
        adapterPosition: Int,
        currentMedia: MediaAssetEntity?
        ){
        val postId = post.postId
        val selectedPostId: Long = post.postId
        val postCreatorUid = post.user?.userId
        when(action){
            MeeraPostDotsMenuAction.ComplainPost -> {
                viewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.POST_REPORT,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(FeedViewActions.ComplainToPost(postId))
            }
            MeeraPostDotsMenuAction.CopyLink -> {
                triggerPostsAction(FeedViewActions.CopyPostLink(post.postId))
            }
            MeeraPostDotsMenuAction.DeletePost -> {
                viewModel.logDeletedPost(post.toPost().toAnalyticPost(), AmplitudePropertyWhere.FEED)
                triggerPostsAction(FeedViewActions.DeletePost(post, adapterPosition))
                feedAdapter?.stopCurrentAudio(adapterPosition)
            }
            MeeraPostDotsMenuAction.EditPost -> {
                viewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.CHANGE,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(FeedViewActions.EditPost(post = post))
            }
            MeeraPostDotsMenuAction.HideAllProfilePost -> {
                viewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.HIDE_USER_POSTS,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(FeedViewActions.HideUserRoads(post.user?.userId))
            }
            MeeraPostDotsMenuAction.SaveToDevice -> {
                post.postImage?.let { imageUrl ->
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.SAVE,
                        authorId = postCreatorUid,
                        saveType = AmplitudePropertySaveType.PHOTO
                    )
                    saveImageOrVideoFile(
                        imageUrl = imageUrl,
                        act = act,
                        viewLifecycleOwner = viewLifecycleOwner,
                        successListener = {
                            showMessage(getString(R.string.image_saved))
                        }
                    )
                }

                val savingVideoIsAvailable =
                    (requireActivity().application as App).remoteConfigs?.postVideoSaving ?: true
                if (post.video != null && savingVideoIsAvailable){
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.SAVE,
                        authorId = postCreatorUid,
                        saveType = AmplitudePropertySaveType.VIDEO
                    )
                    saveVideo(postId, currentMedia?.id)
                }
            }
            MeeraPostDotsMenuAction.SharePost -> {
                onRepostClicked(post)
            }
            MeeraPostDotsMenuAction.SubscribeToPost -> {
                if (!post.isPostSubscribed) {
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_FOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.SubscribeToPost(selectedPostId, PostSubscribeTitle.SubscribeString()))
                } else if (post.isPostSubscribed) {
                    viewModel.logPostMenuAction(
                        post = post,
                        action = AmplitudePropertyMenuAction.POST_UNFOLLOW,
                        authorId = postCreatorUid,
                    )
                    triggerPostsAction(FeedViewActions.UnsubscribeFromPost(selectedPostId, PostSubscribeTitle.SubscribeString()))
                }
            }
            MeeraPostDotsMenuAction.SubscribeToProfile -> {
                viewModel.logPostMenuAction(
                    post = post,
                    action = AmplitudePropertyMenuAction.USER_FOLLOW,
                    authorId = postCreatorUid,
                )
                triggerPostsAction(
                    FeedViewActions.SubscribeToUser(
                        postId = post.postId,
                        userId = post.user?.userId,
                        needToHideFollowButton = false,
                        fromFollowButton = false,
                        isApproved = post.user?.approved.toBoolean(),
                        topContentMaker = post.user?.topContentMaker.toBoolean()
                    )
                )
            }
        }
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.downloadPostVideo(postId, assetId)
                }

                override fun onDenied() {
                    NToast.with(act)
                        .text(act.getString(R.string.you_must_grant_permissions))
                        .durationLong()
                        .button(act.getString(R.string.general_retry)) {
                            viewModel.downloadPostVideo(postId, assetId)
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

    private fun handleTagClicked(
        clickType: SpanDataClickType,
        position: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity? = null
    ) {
        clickCheckBubble {
            when (clickType) {
                is SpanDataClickType.ClickUserId -> {
                    gotoUserProfileFragment(
                        userId = clickType.userId
                    )
                }

                is SpanDataClickType.ClickUnknownUser -> {
                    NToast.with(view)
                        .text(context?.string(R.string.uniqname_unknown_profile_message))
                        .show()
                }

                is SpanDataClickType.ClickHashtag -> {
                    handleHashtagClick(clickType.hashtag, post?.postId ?: 0, post?.user?.userId ?: 0)
                }

                is SpanDataClickType.ClickBadWord -> {
                    handleBadWord(clickType, position, tagOrigin, post)
                }

                is SpanDataClickType.ClickLink -> {
                    act.openLink(clickType.link)
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
            feedAdapter?.updateItem(adapterPos = position, payload = postUpdate)
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
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_dialog_description))
            .setLeftBtnText(getString(R.string.unsubscribe_dialog_close))
            .setRightBtnText(getString(R.string.user_info_unsub_dialog_action))
            .setLeftClickListener {
            }
            .setRightClickListener {
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
        super.onDestroyView()
        feedAdapter = null
        recyclerView?.clear()
        recyclerView?.onDestroyView()
        recyclerView = null
        dotsMenuPost = null
    }

    private fun gotoUserProfileFragment(
        userId: Long?,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.FEED
    ) {
        val analyticWhere = if (getRoadType() == RoadTypeEnum.CUSTOM_ROAD)
            AmplitudePropertyWhere.FEED_PROFILE_WALL else where
        add(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, userId),
            Arg(ARG_TRANSIT_FROM, analyticWhere.property)
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
        needAuth {
            add(
                HashtagFragment(), Act.LIGHT_STATUSBAR,
                Arg(ARG_HASHTAG, hashtag)
            )
        }
    }

    private fun handleClickCommunityView(groupId: Long?) = needAuth {
        groupId?.let { id: Long ->
            add(
                CommunityRoadFragment(),
                Act.LIGHT_STATUSBAR,
                Arg(ARG_GROUP_ID, id.toInt()),
                Arg(ARG_TRANSIT_COMMUNITY_FROM, CommunityTransitFrom.FEED.key)
            )
            viewModel.getAmplitudeHelper().logCommunityScreenOpened(AmplitudePropertyWhereCommunityOpen.FEED)
        }
    }

    private fun handleFeatureClicked(featureId: Long, dismiss: Boolean, deepLink: String?) = needAuth {
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
    private fun handleRepostClick(post: PostUIEntity) = needAuth {
        checkAppRedesigned(
            isRedesigned = {
                meeraOpenRepostMenu(post)
            },
            isNotRedesigned = {
                openRepostMenu(post)
            }
        )
    }

    private fun openRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (act.navigatorAdapter.getFragmentsCount() > 1 || parentFragment?.getMainRoadFragment()
                ?.getMode() == MainRoadMode.MAP
        ) return
        SharePostBottomSheet(
            postOrigin = DestinationOriginEnum.fromRoadType(getRoadType()),
            post = post.toPost(),
            event = post.event,
            mode = mode,
            callback = object : IOnSharePost {
                override fun onShareFindGroup() {
                    act.goToGroups()
                }

                override fun onShareFindFriend() {
                    add(
                        SearchMainFragment(),
                        Act.LIGHT_STATUSBAR,
                        Arg(
                            ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                            AmplitudeFindFriendsWhereProperty.SHARE
                        )
                    )
                }

                override fun onShareToGroupSuccess(groupName: String?) {
                    viewModel.repostSuccess(post)
                    showMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                }

                override fun onShareToRoadSuccess() {
                    viewModel.repostSuccess(post)
                    showMessage(getString(R.string.success_repost_to_own_road))
                }

                override fun onShareToChatSuccess(repostTargetCount: Int) {
                    viewModel.repostSuccess(post, repostTargetCount)
                    val strResId = if (post.isEvent()) {
                        R.string.success_event_repost_to_chat
                    } else {
                        R.string.success_repost_to_chat
                    }
                    showMessage(getString(strResId))
                }

                override fun onPostItemUniqnameUserClick(userId: Long?) {
                    act.addFragment(
                        UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                        Arg(ARG_USER_ID, userId),
                        Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.FEED.property)
                    )
                }
            }).show(childFragmentManager)
    }

    private fun meeraOpenRepostMenu(post: PostUIEntity, mode: SharingDialogMode = SharingDialogMode.DEFAULT) {
        if (act.navigatorAdapter.getFragmentsCount() > 1 || parentFragment?.getMainRoadFragment()
                ?.getMode() == MainRoadMode.MAP
        ) return

        MeeraShareSheet().show(
            fm = childFragmentManager,
            data = MeeraShareBottomSheetData(
                postOrigin = DestinationOriginEnum.fromRoadType(getRoadType()),
                post = post.toPost(),
                event = post.event,
                mode = mode,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        act.goToGroups()
                    }

                    override fun onShareFindFriend() {
                        add(
                            SearchMainFragment(),
                            Act.LIGHT_STATUSBAR,
                            Arg(
                                ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                AmplitudeFindFriendsWhereProperty.SHARE
                            )
                        )
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        viewModel.repostSuccess(post)
                        showMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.repostSuccess(post)
                        showMessage(getString(R.string.success_repost_to_own_road))
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.repostSuccess(post, repostTargetCount)
                        val strResId = if (post.isEvent()) {
                            R.string.success_event_repost_to_chat
                        } else {
                            R.string.success_repost_to_chat
                        }
                        showMessage(getString(strResId))
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) {
                        act.addFragment(
                            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                            Arg(ARG_USER_ID, userId),
                            Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.FEED.property)
                        )
                    }
                }

            )
        )

    }

    fun showEmptyPostsPlaceholder(type: RoadTypeEnum) {
        viewModel.clearFeed()
        val placeholder = childView?.findViewById<LinearLayout>(R.id.ll_empty_list_container) ?: return

        placeholder.clearAnimation()

        placeholder
            .animate()
            .setStartDelay(SHOW_PLACE_HOLDER_DELAY)
            .setDuration(SHOW_PLACE_HOLDER_DURATION)
            .alpha(1f)

        when (type) {
            RoadTypeEnum.CUSTOM_ROAD -> customEmptyRoadPlaceholderConfig()
            RoadTypeEnum.SUBSCRIPTIONS_ROAD -> subscriptionsRoadPlaceholderConfig()
            RoadTypeEnum.MAIN_ROAD -> mainRoadPlaceholderConfig()
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

    private fun customEmptyRoadPlaceholderConfig() {
        childView?.findViewById<TextView>(R.id.tv_empty_list)?.text =
            getString(R.string.place_holder_user_post_list)
        childView?.findViewById<TextView>(R.id.tv_button_empty_list)?.apply {
            visible()
            text = getString(R.string.write_post)
            setOnClickListener { gotoAddPost(true, AddMultipleMediaPostFragment.OpenFrom.SelfRoad) }
        }
        childView?.findViewById<ImageView>(R.id.iv_empty_list)?.loadGlide(R.drawable.road_empty)
    }

    private fun subscriptionsRoadPlaceholderConfig() {
        childView?.findViewById<FrameLayout>(R.id.vg_subscription_placeholder_button)?.setThrottledClickListener {
            act.goToPeoples(AmplitudePeopleWhereProperty.SEE_REC_FOLLOW_FEED)
        }
    }

    private fun mainRoadPlaceholderConfig() {
        childView?.findViewById<TextView>(R.id.tv_empty_list)?.text =
            getString(R.string.no_posts_change_filter)
        childView?.findViewById<ImageView>(R.id.iv_empty_list)?.loadGlide(R.drawable.road_empty)
    }

    abstract fun onFeedBtnClicked()

    fun scrollToTop(expandAppBarLayout: Boolean = true) {
        val appBarLayout: AppBarLayout = act.findViewById(R.id.appbar)

        recyclerView?.scrollToPosition(0)

        if (expandAppBarLayout) {
            appBarLayout.setExpanded(true)
        }
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

        refreshTopButton.visible()
        refreshTopButton.let { animView ->
            playLottieAnimation(animView, "scroll_refresh_start_animation.json") {
                animView.visible()
            }
        }
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

            playLottieAnimation(button, "scroll_refresh_end_animation.json") {
                button.gone()
            }
        }
    }

    protected open fun getRefreshTopButtonView(): LottieAnimationView? {
        return null
    }

    protected open fun canShowRefreshTopButtonView(): Boolean {
        return true
    }

    /**
     * Счетчик записывает в RxPref количество просмотренных постов
     **/
    protected fun initPostCounter() = Unit

    fun initAnimViewClickListener(animView: LottieAnimationView) {
        animView.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                playLottieAnimation(animView, "scroll_refresh_push_animation.json") {
                    animView.gone()
                }
                refreshRoadAfterScrollRefresh()
            }
        }
    }

    open fun refreshRoadAfterScrollRefresh() = Unit

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
                completeAnimation.invoke()
                animView.removeAllAnimatorListeners()
            }

            override fun onAnimationCancel(animation: Animator) = Unit

            override fun onAnimationStart(animation: Animator) = Unit
        })
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
        add(
            AddMultipleMediaPostFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(ARG_SHOW_MEDIA_GALLERY, false),
            Arg(ARG_POST, post),
            Arg(ARG_GROUP_ID, post.groupId?.toInt())
        )
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

    private fun showTextError(message: String) {
        view?.let {
            NToast.with(it)
                .text(message)
                .typeError()
                .show()
        }
    }

}
