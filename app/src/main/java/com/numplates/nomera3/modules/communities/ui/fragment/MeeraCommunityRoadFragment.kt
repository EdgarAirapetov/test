package com.numplates.nomera3.modules.communities.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.getToolbarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.readmore.UiKitReadMoreTextView
import com.meera.uikit.widgets.setMargins
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentShowCommunityBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.MeeraCommunityRoadDotsMenuDialog
import com.numplates.nomera3.modules.communities.ui.MeeraCommunityRoadDotsMenuDialogClick
import com.numplates.nomera3.modules.communities.ui.MeeraCommunityRoadDotsMenuDialogData
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraBaseCommunityDashboardFragment
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraCommunityCreatorDashboardFragment
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.MeeraCommunityModeratorDashboardFragment
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewevent.GetCommunityLinkAction
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityRoadViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.fragment.MeeraBaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.registration.ui.phoneemail.MeeraUserBlockedByAdminDialog
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs


const val IS_NEW_GROUP = "IS_NEW_GROUP"
private const val TAG_USER_BLOCKED_DIALOG = "USER_BLOCKED_DIALOG"


class MeeraCommunityRoadFragment : MeeraBaseFeedFragment(
    layout = R.layout.meera_fragment_show_community,
    behaviourConfigState = ScreenBehaviourState.CommunitiesTransparent
), SwipeRefreshLayout.OnRefreshListener,
    AppBarLayout.OnOffsetChangedListener,
    ScreenshotTakenListener {

    private var popupData: ScreenshotPopupData? = null
    private var userPermissions: UserPermissions? = null
    private val eventViewModel by viewModels<CommunityRoadViewModel>()
    private val subscriptionViewModel by viewModels<CommunitySubscriptionViewModel>(
        factoryProducer = { App.component.getViewModelFactory() }
    )
    private var subscriptionCallback: SubscriptionCallback? = null
    private var editCallback: EditCallback? = null
    private var isExpandedToolbar = false
    private var isExtraContainerShown = true
    private var groupId: Int? = null
    private var transitFrom: Int? = null
    private var isNewGroup = false
    private var isScreenshotPopupShown = false

    private var infoSnackbar: UiKitSnackBar? = null
    private var errorSnackbar: UiKitSnackBar? = null
    private val staticLayoutChangeListener = StaticLayoutChangeListenerImpl()

    private val binding by viewBinding(MeeraFragmentShowCommunityBinding::bind)


    override val containerId: Int
        get() = R.id.fragment_first_container_view

    fun setSubscriptionCallback(callback: SubscriptionCallback) {
        subscriptionCallback = callback
    }

    fun setEditCallback(callback: EditCallback) {
        editCallback = callback
    }

    override fun isNotCommunityScreen() = false

    override val needToShowProfile: Boolean
        get() = true

    override fun getAnalyticPostOriginEnum() = DestinationOriginEnum.COMMUNITY

    override fun getFormatter(): AllRemoteStyleFormatter {
        return AllRemoteStyleFormatter(feedViewModel.getSettings())
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Community(groupId ?: 0)
    }

    override fun onClickScrollUpButton() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.appbar.setExpanded(true, true)
            binding.rvCommunityPosts.scrollToPosition(0)
            onRefresh()
        }
    }

    override fun getAmplitudeWhereMomentOpened() = AmplitudePropertyMomentScreenOpenWhere.OTHER

    override fun showEmptyFeedPlaceholder() {
        super.showEmptyFeedPlaceholder()
        eventViewModel.getCommunityInfo(groupId)
        binding.appbar.setExpanded(true)
        showRoadPlaceholder(
            R.drawable.ic_community_empty_post,
            getString(R.string.placeholder_posts_community)
        )
    }

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.COMMUNITY

    override fun getAmplitudeWhereProfileFromOpened() = AmplitudePropertyWhere.COMMUNITY

    override fun getWhereFromHashTagPressed() = AmplitudePropertyWhere.COMMUNITY

    override fun getCommunityId() = groupId?.toLong() ?: -1

    override fun onScreenshotTaken() {
        resetAllZoomViews()
        if (isSavingFeedPhoto) return
        eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SCREENSHOT_POPUP)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = arguments?.getInt(IArgContainer.ARG_GROUP_ID)
        transitFrom = arguments?.getInt(IArgContainer.ARG_TRANSIT_COMMUNITY_FROM)
        isNewGroup = arguments?.getBoolean(IS_NEW_GROUP) == true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViews()
        initLiveObservables()
        initPostsLiveObservable()
        feedViewModel.onTriggerAction(FeedViewActions.CheckIfInitialOpen)
    }

    override fun onStop() {
        super.onStop()
        infoSnackbar?.dismiss()
        errorSnackbar?.dismiss()
    }

    override fun onDestroyView() {
        binding.rvCommunityPosts.release()
        binding.rvCommunityPosts.adapter = null
        editCallback = null
        subscriptionCallback = null
        eventViewModel.clearEvents()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        hideToolbar()
    }

    override fun onRefresh() {
        binding.vgSwipeLayout.isRefreshing = false
        eventViewModel.getCommunityInfo(groupId)
        initHeightTitle()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (verticalOffset == 0) {
            binding.vgSwipeLayout.isEnabled = true
        } else {
            binding.vgSwipeLayout.isRefreshing = false
            binding.vgSwipeLayout.isEnabled = false
        }

        val maxScroll = appBarLayout.totalScrollRange.toFloat()
        val percentage = abs(verticalOffset) / maxScroll

        if (percentage >= APP_BAR_OFFSET_PERCENTAGE && !isExpandedToolbar) {
            binding.ivBack.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_1000))
            binding.ivNotificationBell.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_1000))
            binding.ivDotsMenu.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black_1000))
            isExpandedToolbar = true
        } else if (percentage < APP_BAR_OFFSET_PERCENTAGE && isExpandedToolbar) {
            binding.ivBack.colorFilter = null
            binding.ivNotificationBell.colorFilter = null
            binding.ivDotsMenu.colorFilter = null
            isExpandedToolbar = false
        }

        if (percentage >= COLLAPSING_PROGRESS_RUN_ANIM && isExtraContainerShown) {
            hideExtraContainer()
            isExtraContainerShown = false
        } else if (percentage < COLLAPSING_PROGRESS_RUN_ANIM && !isExtraContainerShown) {
            showExtraContainer()
            isExtraContainerShown = true
        }
    }

    override fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String?) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCreatePostFragment,
            bundle = bundleOf(
                IArgContainer.ARG_GROUP_ID to groupId,
                ARG_SHOW_MEDIA_GALLERY to false,
                IArgContainer.ARG_POST to post,
                UPLOAD_BUNDLE_KEY to postStringEntity
            )
        )
    }

    override fun getParentContainer(): ViewGroup? = binding.flContainer

    override fun getRefreshTopButtonView() = binding.btnScrollRefreshCommunityRoad

    private fun hideExtraContainer() {
        binding.clDescriptionContainer
            .animate()
            .translationY(dpToPx(DESCRIPTION_TRANSLATION_Y).toFloat())
            .setListener(onAnimationEnd = {
                binding.flAddPostContainer
                    .animate()
                    ?.translationY(dpToPx(ADD_POST_TRANSLATION_Y).toFloat())
                    ?.setInterpolator(DecelerateInterpolator())
                    ?.duration = DURATION_ADD_POST_CONTAINER_ANIM
            })
            .setInterpolator(DecelerateInterpolator())
            .duration = DURATION_DESCRIPTION_CONTAINER_ANIM
    }

    private fun showExtraContainer() {
        binding.clDescriptionContainer
            .animate()
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator())
            .setListener(onAnimationEnd = {
                binding.flAddPostContainer
                    .animate()
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator())
                    .duration = DURATION_ADD_POST_CONTAINER_ANIM
            })
            .duration = DURATION_DESCRIPTION_CONTAINER_ANIM
    }

    private fun initToolbar() {
        val layoutParams = binding.toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        val statusBarHeight = context.getStatusBarHeight()
        val toolbarHeight = context.getToolbarHeight()
        layoutParams.height = toolbarHeight + statusBarHeight
        binding.toolbar.layoutParams = layoutParams
        binding.ivBack.setMargins(top = statusBarHeight)
        binding.ivDotsMenu.setMargins(top = statusBarHeight)
        binding.ivNotificationBell.setMargins(top = statusBarHeight)
        binding.appbar.addOnOffsetChangedListener(this)
        binding.ivBack.setThrottledClickListener {
            backPressed()
        }
    }

    private fun backPressed() {
        if (isNewGroup) {
            findNavController().popBackStack(R.id.meeraCommunitiesListsContainerFragment, false)
        } else {
            findNavController().popBackStack()
        }
    }

    private fun initViews() {
        binding.vgSwipeLayout.setOnRefreshListener(this)
        binding.appbar.addOnOffsetChangedListener(this)
        binding.tvDescriptionText.setOnStaticLayoutChangeListener(staticLayoutChangeListener)
        binding.addGroupPost.addPostClickContainer.setThrottledClickListener {
            gotoAddPost(groupId, false)
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })
        initRoadAdapter()
    }

    private fun gotoAddPost(groupId: Int?, isShowGallery: Boolean = false) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCreatePostFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, groupId ?: 0)
                putBoolean(ARG_SHOW_MEDIA_GALLERY, isShowGallery)
                putString(
                    AddMultipleMediaPostFragment.OpenFrom.EXTRA_KEY,
                    AddMultipleMediaPostFragment.OpenFrom.Community.toString()
                )
            }
        )
    }

    private fun initLiveObservables() {
        eventViewModel.communityInfoLiveEvent.observe(viewLifecycleOwner, ::handleCommunityInfo)
        eventViewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleRoadViewEvents)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscriptionViewModel.viewEvent.collect(::handleRoadViewEvents)
            }
        }
    }

    private fun handleCommunityInfo(event: CommunityViewEvent?) {
        when (event) {
            is CommunityViewEvent.CommunityData -> {
                showGroupData(event.community)
                configureCreatePostBtn(event.permission, event.community.private)
                hideToolbar()
            }

            is CommunityViewEvent.CommunityDataProgress -> {
                showCommunityDataProgress(event.inProgress)
            }

            is CommunityViewEvent.FailureGetCommunityInfo -> {
                showErrorMessage(R.string.group_error_load_group_data)
            }

            is CommunityViewEvent.FailureCommunityNotFound -> {
                showErrorMessage(R.string.community_unavailable)
            }

            else -> Unit
        }
    }

    private fun handleRoadViewEvents(event: CommunityViewEvent?) {
        hideToolbar()
        when (event) {
            is CommunityViewEvent.SuccessSubscribeCommunity -> {
                setJoinButton(true)
                showSuccessMessage(R.string.subscribe_post)
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(true, it) }
                eventViewModel.getCommunity()?.let {
                    it.isSubscribed = 1
                    it.subscribedNotifications = 1
                    initNotificationsAndSettings(it)
                    initAddPostView(it)
                    initHeightTitle()
                }
            }

            is CommunityViewEvent.SuccessUnsubscribeCommunity -> {
                setJoinButton(false)
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(false, it) }
                eventViewModel.getCommunity()?.let {
                    it.isSubscribed = 0
                    initNotificationsAndSettings(it)
                    initAddPostView(it)
                    initHeightTitle()
                }
            }

            is CommunityViewEvent.SuccessUnsubscribePrivateCommunity -> {
                setJoinPrivateButton(false)
                refreshGroupInfo(event.groupId)
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(false, it) }
            }

            is CommunityViewEvent.SuccessSubscribePrivateCommunity -> {
                setJoinPrivateButton()
                showSuccessMessage(R.string.group_private_subscription_success)
                refreshGroupInfo(event.groupId)
                initHeightTitle()
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(true, it) }
            }

            is CommunityViewEvent.SuccessSubscribedToNotifications -> {
                setNotificationsAllowed(event.groupId)
                showSuccessMessage(R.string.community_subscribed_to_notifications_message)
            }

            is CommunityViewEvent.SuccessUnsubscribedFromNotifications -> {
                setNotificationsNotAllowed(event.groupId)
                showSuccessMessage(R.string.community_unsubscribed_from_notifications_message)
            }

            is CommunityViewEvent.FailedSubscribeToNotifications -> {
                setNotificationsNotAllowed(event.groupId)
                showErrorMessage(R.string.community_subscribed_to_notifications_error)
            }

            is CommunityViewEvent.FailedUnsubscribeFromNotifications -> {
                setNotificationsAllowed(event.groupId)
                showErrorMessage(R.string.community_unsubscribed_from_notifications_error)
            }

            is CommunityViewEvent.FailureSubscribeCommunity -> {
                showErrorMessage(R.string.group_error_subscribe_group)
            }

            is CommunityViewEvent.FailureUnsubscribeCommunity -> {
                showErrorMessage(R.string.group_error_unsubscribe_group)
            }

            is CommunityViewEvent.SubscribeCommunityProgress -> {
                showSubscribeCommunityProgress(event.inProgress)
            }

            is CommunityViewEvent.CommunityNotificationsProgress -> {
                showNotificationBellProgress(event.inProgress)
            }

            is CommunityViewEvent.SuccessGetCommunityLink -> {
                handleGetCommunityLink(event)
            }

            is CommunityViewEvent.FailGetCommunityLink -> {
                showErrorMessage(R.string.share_community_link_error)
            }

            is CommunityViewEvent.OpenSupportAdminChat -> {
                openAdminChat(event.adminId)
            }

            CommunityViewEvent.RefreshCommunityRoad -> {
                onRefresh()
            }

            CommunityViewEvent.BaseLoadPosts -> {
                loadBasePosts()
            }

            else -> Unit
        }
    }

    private fun openAdminChat(adminId: Long) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraChatFragment,
            bundle = Bundle().apply {
                putParcelable(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = adminId
                    )
                )
            }
        )
    }

    override fun loadInitialPostAction() = eventViewModel.getCommunityInfo(groupId)

    override fun scrollUpCommunityPosts() {
        if (!binding.appbar.isLifted) {
            feedRecycler?.post {
                feedRecycler?.smoothScrollToPosition(0)
            }
        }
    }

    private fun configureCreatePostBtn(permission: UserPermissions?, private: Int) {
        permission?.communities?.let { communitiesPermissions ->
            this.userPermissions = permission
            if (private.isTrue() && !communitiesPermissions.canCreatePostInPrivateCommunity) {
                initClickForNotPermittedUser()
            } else if (private.isFalse() && !communitiesPermissions.canCreatePostInOpenCommunity) {
                initClickForNotPermittedUser()
            }
        }
    }

    private fun initClickForNotPermittedUser() {
        binding.addGroupPost.root.setThrottledClickListener {
            showDialogNoPermissionToCreatePostInCommunity()
        }
        binding.placeholder.btnNoGroupPostsEmpty.setThrottledClickListener {
            showDialogNoPermissionToCreatePostInCommunity()
        }
    }

    private fun showDialogNoPermissionToCreatePostInCommunity() {
        MeeraUserBlockedByAdminDialog.newInstance(
            headerDialogType = HeaderDialogType.GroupRoadType,
            blockReason = this.userPermissions?.userBlockInfo?.blockReasonText.orEmpty(),
            blockDate = this.userPermissions?.userBlockInfo?.blockedUntil ?: 0
        ).show(childFragmentManager, TAG_USER_BLOCKED_DIALOG)
    }

    private fun handleGetCommunityLink(event: CommunityViewEvent.SuccessGetCommunityLink) {
        when (event.action) {
            GetCommunityLinkAction.SHARE_LOCAL -> {
                meeraShowShareCommunityDialog(event.link)
                eventViewModel.logCommunityShare(AmplitudePropertyCommunityWhere.INSIDE)
            }

            GetCommunityLinkAction.SHARE_OUTSIDE -> {
                copyCommunityLink(context, event.link) {
                    showSuccessMessage(R.string.copy_link_success)
                }
                eventViewModel.logCommunityShare(AmplitudePropertyCommunityWhere.LINK)
            }

            GetCommunityLinkAction.SCREENSHOT_POPUP -> {
                showScreenshotPopup(event.link)
            }
        }
    }

    private fun showScreenshotPopup(communityLink: String) {
        if (isScreenshotPopupShown) return
        isScreenshotPopupShown = true
        this.popupData = this.popupData.apply { this?.link = communityLink }
        popupData?.let { ScreenshotPopupController.show(this, it) }
    }

    private fun setToolbarTitleExpandedMarginBottom(marginDp: Int) {
        binding.collapsingToolbarCommunityInfo.expandedTitleMarginBottom = marginDp
    }

    private fun showGroupData(community: CommunityEntity?) {
        community?.let {
            initUserRoleForRoad(it)
            initCollapsingToolbar(it)
            initAvatar(it)
            initPrivateIcon(it)
            initNotificationsAndSettings(it)
            initJoinButton(it)
            initSubscribersCount(it)
            initDescription(it)
            initAddPostView(it)
            initPosts(it)
            savePopupData(it)
            initHeightTitle()
        }
    }

    private fun initUserRoleForRoad(community: CommunityEntity) {
        communityUserRole = when {
            community.isAuthor.isTrue() -> CommunityUserRole.AUTHOR
            community.isModerator.isTrue() -> CommunityUserRole.MODERATOR
            else -> CommunityUserRole.REGULAR
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initCollapsingToolbar(community: CommunityEntity) {
        with(binding) {
            collapsingToolbarCommunityInfo.setExpandedTitleColor(
                ContextCompat.getColor(requireContext(), R.color.uiKitColorBackgroundPrimary)
            )
            collapsingToolbarCommunityInfo.setCollapsedTitleTextColor(
                ContextCompat.getColor(requireContext(), R.color.uiKitColorForegroundPrimary)
            )

            val params = collapsingToolbarCommunityInfo.layoutParams
                as? AppBarLayout.LayoutParams

            params?.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

            collapsingToolbarCommunityInfo.layoutParams = params
            collapsingToolbarCommunityInfo.title = community.name
            collapsingToolbarCommunityInfo.maxLines = COMMUNITY_TITLE_MAX_LINES
            collapsingToolbarCommunityInfo.invalidate()
        }
    }

    private fun initAvatar(community: CommunityEntity) {
        community.avatarBig?.let { avatar ->
            binding.communityCoverImage.let { imageView ->
                Glide.with(binding.root)
                    .load(avatar)
                    .into(binding.communityCoverImage)

                if (avatar.isNotEmpty()) {
                    binding.communityCoverImageGradient.gone()
                    imageView.setThrottledClickListener {
                        findNavController().safeNavigate(
                            resId = R.id.action_meeraCommunityRoadFragment_to_meeraProfilePhotoViewerFragment,
                            bundle = Bundle().apply {
                                putString(IArgContainer.ARG_IMAGE_URL, avatar)
                                putSerializable(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.COMMUNITY)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun initNotificationsAndSettings(community: CommunityEntity) {
        if (community.isSubscribed.isTrue() &&
            community.userStatus != CommunityEntity.USER_STATUS_BANNED &&
            community.userStatus != CommunityEntity.USER_STATUS_NOT_YET_APPROVED
        ) {
            community.subscribedNotifications?.let { subscribedNotifications ->
                if (subscribedNotifications.isTrue()) setNotificationsAllowed(community.groupId)
                else setNotificationsNotAllowed(community.groupId)
            }
            binding.ivNotificationBell.visible()
        } else if (community.isSubscribed.isFalse()) {
            binding.ivNotificationBell.gone()
        }

        val isCreator = community.isAuthor.isTrue()
        val isModerator = community.isModerator.isTrue()

        binding.ivDotsMenu.setThrottledClickListener {
            if (isCreator || isModerator) {
                showDotsMenu(isCreator, isSettingsAvailable = true)
            } else {
                showDotsMenu(isCreator, isSettingsAvailable = false)
            }
        }
    }

    private fun showDotsMenu(isCreator: Boolean, isSettingsAvailable: Boolean) {
        MeeraCommunityRoadDotsMenuDialog().show(
            fm = childFragmentManager,
            data = MeeraCommunityRoadDotsMenuDialogData(isShowSettingsItem = isSettingsAvailable),
            clickAction = { action ->
                when (action) {
                    is MeeraCommunityRoadDotsMenuDialogClick.OnClickShare ->
                        eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SHARE_LOCAL)

                    is MeeraCommunityRoadDotsMenuDialogClick.OnClickCopy ->
                        eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SHARE_OUTSIDE)

                    is MeeraCommunityRoadDotsMenuDialogClick.OnClickSettings -> {
                        if (isCreator) {
                            openCreatorCommunityDashboard()
                        } else {
                            openModeratorCommunityDashboard()
                        }
                    }
                }
            }
        )
    }

    private fun meeraShowShareCommunityDialog(link: String) {
        groupId?.let { id ->
            MeeraShareSheet().showByType(
                fm = childFragmentManager,
                shareType = ShareDialogType.ShareCommunity(id),
                event = { shareEvent ->
                    when (shareEvent) {
                        is ShareBottomSheetEvent.OnSuccessShareCommunity -> {
                            showSuccessMessage(R.string.share_community_success)
                        }

                        is ShareBottomSheetEvent.OnErrorShareCommunity -> {
                            showErrorMessage(R.string.share_community_error)
                        }

                        is ShareBottomSheetEvent.OnMoreShareButtonClick -> {
                            shareLinkOutside(context, link)
                            eventViewModel.logCommunityShare(AmplitudePropertyCommunityWhere.OUTSIDE)
                        }

                        is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                            showErrorMessage(R.string.no_user_selected)
                        }

                        is ShareBottomSheetEvent.OnClickFindFriendButton -> gotoSearchFragment()

                        else -> Unit
                    }
                }
            )
        }
    }

    private fun gotoSearchFragment() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraSearchFragment,
            bundle = Bundle().apply {
                putSerializable(ARG_FIND_FRIENDS_OPENED_FROM_WHERE, AmplitudeFindFriendsWhereProperty.SHARE)
            }
        )
    }

    private fun setNotificationsAllowed(groupId: Int?) {
        binding.ivNotificationBell.isEnabled = true
        binding.ivNotificationBell.setImageResource(R.drawable.ic_outlined_bell_m)
        binding.ivNotificationBell.setThrottledClickListener {
            binding.ivNotificationBell.isEnabled = false
            eventViewModel.unsubscribeNotifications(groupId)
        }
    }

    private fun setNotificationsNotAllowed(groupId: Int?) {
        binding.ivNotificationBell.isEnabled = true
        binding.ivNotificationBell.setImageResource(R.drawable.ic_outlined_bell_off_m)
        binding.ivNotificationBell.setThrottledClickListener {
            binding.ivNotificationBell.isEnabled = false
            eventViewModel.subscribeNotifications(groupId)
        }
    }

    private fun initSubscribersCount(community: CommunityEntity) {
        community.users.let { count ->
            binding.tvUserCount.text = count.toString()
        }
        binding.tvUserCount.setThrottledClickListener {

        }
    }

    private fun initDescription(community: CommunityEntity) {
        community.description?.let { description ->
            binding.tvDescriptionText.text = description
        }

        binding.tvMore.setThrottledClickListener {
            showCommunityDetails(community)
        }
    }

    private fun showCommunityDetails(community: CommunityEntity) {
        MeeraCommunityDetailsBottomSheet().show(
            fm = childFragmentManager,
            data = MeeraCommunityDetailsBottomSheetData(community),
            clickAction = { action ->
                when (action) {
                    is MeeraCommunityDetailsBottomSheetClick.OpenEditor -> {
                        findNavController().safeNavigate(
                            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCommunityEditFragment,
                            bundle = Bundle().apply {
                                putInt(IArgContainer.ARG_GROUP_ID, community.groupId)
                                putBoolean(IArgContainer.ARG_IS_GROUP_CREATOR, community.isAuthor == 1)
                            }
                        )
                    }

                    is MeeraCommunityDetailsBottomSheetClick.OpenUsers -> {
                        val role = when {
                            community.isAuthor.isTrue() -> CommunityUserRole.AUTHOR
                            community.isModerator.isTrue() -> CommunityUserRole.MODERATOR
                            else -> CommunityUserRole.REGULAR
                        }
                        findNavController().safeNavigate(
                            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCommunityMembersContainerFragment,
                            bundle = Bundle().apply {
                                putInt(IArgContainer.ARG_GROUP_ID, community.groupId)
                                putInt(IArgContainer.ARG_COMMUNITY_USER_ROLE, role)
                                putInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, community.private)
                            }
                        )
                    }
                }
            }
        )
    }

    private fun initAddPostView(community: CommunityEntity) {
        val visible = when {
            community.isSubscribed.isFalse() -> false
            community.private.isTrue() && community.isSubscribed.isFalse() -> false
            community.royalty.isTrue() && community.isAuthor.isFalse() &&
                community.isModerator.isFalse() -> false

            community.royalty.isTrue() && community.isModerator.isFalse() -> false
            else -> true
        }
        if (visible) {
            binding.flAddPostContainer.visible()
            binding.placeholder.btnNoGroupPostsEmpty.setThrottledClickListener {
                gotoAddPost(community.groupId)
            }
            binding.placeholder.btnNoGroupPostsEmpty.visible()
        } else {
            binding.flAddPostContainer.gone()
            binding.placeholder.btnNoGroupPostsEmpty.gone()
        }
    }

    private fun initHeightTitle() {
        val descriptionHeight = binding.tvDescriptionText.lineCount
        when {
            descriptionHeight == DESCRIPTION_ONE_LINE &&
                !eventViewModel.getCommunity()?.isSubscribed.toBoolean() &&
                eventViewModel.getCommunity()?.private.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
            }

            descriptionHeight > DESCRIPTION_ONE_LINE &&
                !eventViewModel.getCommunity()?.isSubscribed.toBoolean() &&
                eventViewModel.getCommunity()?.private.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
            }

            descriptionHeight == DESCRIPTION_ONE_LINE && !eventViewModel.getCommunity()?.isSubscribed.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
            }

            descriptionHeight > DESCRIPTION_ONE_LINE && !eventViewModel.getCommunity()?.isSubscribed.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
            }

            descriptionHeight == DESCRIPTION_ONE_LINE &&
                eventViewModel.getCommunity()?.isSubscribed.toBoolean() &&
                !eventViewModel.getCommunity()?.isAuthor.toBoolean() &&
                !eventViewModel.getCommunity()?.private.toBoolean() -> {
                if (eventViewModel.getCommunity()?.royalty.toBoolean() && !eventViewModel.getCommunity()?.isModerator.toBoolean()){
                    setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
                } else {
                    setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_AUTHOR.dp)
                }
            }

            descriptionHeight > DESCRIPTION_ONE_LINE &&
                eventViewModel.getCommunity()?.isSubscribed.toBoolean() &&
                !eventViewModel.getCommunity()?.isAuthor.toBoolean() &&
                !eventViewModel.getCommunity()?.private.toBoolean() -> {
                if (eventViewModel.getCommunity()?.royalty.toBoolean() && !eventViewModel.getCommunity()?.isModerator.toBoolean()){
                    setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_SUBSCRIBED.dp)
                } else {
                    setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_AUTHOR.dp)
                }
            }

            descriptionHeight == DESCRIPTION_ONE_LINE && !eventViewModel.getCommunity()?.isAuthor.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_AUTHOR.dp)
            }

            descriptionHeight > DESCRIPTION_ONE_LINE && !eventViewModel.getCommunity()?.isAuthor.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_AUTHOR.dp)
            }

            descriptionHeight == DESCRIPTION_ONE_LINE && eventViewModel.getCommunity()?.isAuthor.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_AUTHOR.dp)
            }

            descriptionHeight > DESCRIPTION_ONE_LINE && eventViewModel.getCommunity()?.isAuthor.toBoolean() -> {
                setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_AUTHOR.dp)
            }
        }
    }

    private fun initPosts(community: CommunityEntity) {
        when {
            community.userStatus == CommunityEntity.USER_STATUS_BANNED -> {
                showRoadPlaceholder(
                    R.drawable.meera_group_posts_closed,
                    getString(R.string.community_user_blocked_road_placeholder_text)
                )
            }

            community.private.isTrue() &&
                community.isSubscribed.isFalse() -> {
                showRoadPlaceholder(
                    R.drawable.ic_community_empty_post,
                    getString(R.string.community_private_road_placeholder_text)
                )
            }

            community.posts.isFalse() &&
                community.userStatus != CommunityEntity.USER_STATUS_BANNED -> {
                showRoadPlaceholder(
                    R.drawable.ic_community_empty_post,
                    getString(R.string.placeholder_posts_community)
                )
            }

            community.private.isFalse() ||
                community.isAuthor.isTrue() ||
                community.isSubscribed.isTrue() -> {

                initRoadTypeAndViewModel(
                    NetworkRoadType.COMMUNITY(
                        community.groupId,
                        community.private.isTrue(),
                        REQUEST_ROAD_TYPE_GROUP
                    )
                )

                binding.placeholder.root.gone()
                binding.rvCommunityPosts.visible()
                binding.vgSwipeLayout.isEnabled = true
            }

            else -> {
                binding.vgSwipeLayout.isRefreshing = false
                binding.vgSwipeLayout.isEnabled = false
                binding.placeholder.root.visible()
                binding.rvCommunityPosts.gone()
            }
        }
    }

    private fun savePopupData(community: CommunityEntity) {
        this.popupData = ScreenshotPopupData(
            title = community.name ?: String.empty(),
            description = getString(R.string.general_community),
            additionalInfo = community.description,
            buttonTextStringRes = R.string.share_community,
            imageLink = community.avatar,
            communityId = community.groupId.toLong(),
            screenshotPlace = ScreenshotPlace.COMMUNITY
        )
    }

    private fun showRoadPlaceholder(iconResource: Int, text: String) {
        binding.placeholder.ivNoGroupPostsEmpty.setImageResource(iconResource)
        binding.placeholder.tvNoGroupPostsEmpty.text = text
        binding.placeholder.root.visible()
        binding.rvCommunityPosts.gone()
    }

    private fun initRoadAdapter() {
        binding.rvCommunityPosts.layoutManager = LinearLayoutManager(context)
        initPostsAdapter(
            recyclerView = binding.rvCommunityPosts,
            scrollToTopView = binding.btnScrollRefreshCommunityRoad
        )
        binding.rvCommunityPosts.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
        binding.rvCommunityPosts.adapter = getAdapterPosts()
        initPostsLoadScrollListener()
    }

    private fun initJoinButton(community: CommunityEntity) {
        if (community.isAuthor.isTrue() ||
            community.userStatus == CommunityEntity.USER_STATUS_BANNED
        ) {
            binding.btnCommunityJoin.gone()
        } else {
            binding.btnCommunityJoin.visible()
            when {
                community.private.isFalse() -> setJoinButton(community.isSubscribed.isTrue())
                community.userStatus == CommunityEntity.USER_STATUS_NOT_YET_APPROVED ->
                    setJoinPrivateButton()

                else -> setJoinPrivateButton(community.isSubscribed.isTrue())
            }
        }
    }

    private fun setJoinButton(joined: Boolean) {
        when {
            joined -> {
                binding.btnCommunityJoin.apply {
                    text = getString(R.string.group_joined)
                    buttonType = ButtonType.OUTLINE
                    setThrottledClickListener {
                        unsubscribeClicked()
                    }
                }
            }

            else -> {
                binding.btnCommunityJoin.apply {
                    text = getString(R.string.group_join)
                    buttonType = ButtonType.FILLED
                    setThrottledClickListener {
                        val uiModel = eventViewModel.getCommunityListItemUIModel()
                        uiModel.id?.let { communityId ->
                            subscriptionViewModel.subscribeCommunity(
                                uiModel,
                                AmplitudePropertyWhereCommunityFollow.COMMUNITY
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setJoinPrivateButton(joined: Boolean? = null) {
        when {
            eventViewModel.getCommunity()?.userStatus == CommunityEntity.USER_STATUS_NOT_YET_APPROVED -> {
                binding.btnCommunityJoin.apply {
                    text = getString(R.string.group_join_request_sent)
                    buttonType = ButtonType.OUTLINE
                    setThrottledClickListener {
                        unsubscribeClicked()
                    }
                }
            }

            joined == true -> {
                binding.btnCommunityJoin.apply {
                    text = getString(R.string.group_joined)
                    buttonType = ButtonType.OUTLINE
                    setThrottledClickListener {
                        unsubscribeClicked()
                    }
                }
            }

            joined == false -> {
                binding.btnCommunityJoin.apply {
                    text = getString(R.string.groups_request)
                    buttonType = ButtonType.FILLED
                    setThrottledClickListener {
                        subscriptionViewModel.subscribeCommunity(
                            eventViewModel.getCommunityListItemUIModel(),
                            AmplitudePropertyWhereCommunityFollow.COMMUNITY
                        )
                    }
                }
            }
        }
    }

    private fun unsubscribeClicked() {
        when {
            eventViewModel.getCommunity()?.isModerator == 1 -> {
                showUnsubscribeCommunityDialog(R.string.meeera_community_unsubscribe_admin_text) {
                    subscriptionViewModel.unsubscribeCommunity(eventViewModel.getCommunity())
                }
            }

            eventViewModel.getCommunity()?.private == 1 &&
                eventViewModel.getCommunity()?.userStatus != CommunityEntity.USER_STATUS_NOT_YET_APPROVED -> {
                showUnsubscribeCommunityDialog(R.string.community_unsubscribe_private_text) {
                    subscriptionViewModel.unsubscribeCommunity(eventViewModel.getCommunity())
                }
            }

            else -> subscriptionViewModel.unsubscribeCommunity(eventViewModel.getCommunity())
        }
    }

    private fun initPrivateIcon(community: CommunityEntity) {
        if (community.private.isTrue()) {
            binding.ivPrivateIcon.visible()
            binding.ivPrivateDivider.visible()
        } else {
            binding.ivPrivateIcon.gone()
            binding.ivPrivateDivider.gone()
        }
    }

    private fun refreshGroupInfo(groupId: Int?) {
        eventViewModel.getCommunityInfo(groupId)
    }

    private fun showCommunityDataProgress(inProgress: Boolean) {
        if (inProgress) binding.loadingCircle.visible()
        else binding.loadingCircle.gone()
    }

    private fun showSubscribeCommunityProgress(inProgress: Boolean) {
        binding.btnCommunityJoin.isEnabled = !inProgress
        if (inProgress) {
            binding.pbSubscribe.visible()
            binding.btnCommunityJoin.invisible()
        } else {
            binding.pbSubscribe.gone()
            binding.btnCommunityJoin.visible()
        }
    }

    private fun showNotificationBellProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.ivNotificationBell.gone()
            binding.pbNotifications.visible()
        } else {
            binding.ivNotificationBell.visible()
            binding.pbNotifications.gone()
        }
    }

    private fun showSuccessMessage(@StringRes messageRes: Int) {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(messageRes),
                    avatarUiState = AvatarUiState.SuccessIconState,
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun showErrorMessage(@StringRes messageRes: Int) {
        errorSnackbar = UiKitSnackBar.makeError(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(messageRes)
                )
            )
        )
        errorSnackbar?.show()
    }

    private fun showUnsubscribeCommunityDialog(
        @StringRes textRes: Int,
        unsubscribe: () -> Unit
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.community_unsubscribe_dialog_title))
            .setDescription(getString(textRes))
            .setTopBtnText(getString(R.string.unsubscribe))
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(getString(R.string.cancel))
            .setTopClickListener { unsubscribe() }
            .show(childFragmentManager)
    }

    private fun hideToolbar() {
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state = UiKitToolbarViewState.COLLAPSED
    }

    private fun openCreatorCommunityDashboard() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCommunityCreatorDashboardFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, eventViewModel.getCommunity()?.groupId ?: 0)
                putInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, eventViewModel.getCommunity()?.private ?: 0)
            }
        )
        MeeraCommunityCreatorDashboardFragment().apply {
            dashboardCallback = object : MeeraBaseCommunityDashboardFragment.Callback {
                override fun onCommunityInfoChanged() {
                    eventViewModel.getCommunityInfo(groupId)
                    editCallback?.onCommunityEdited()
                }
            }
        }
    }

    // Экран "Управление сообществом" для модератора/админа сообщества
    private fun openModeratorCommunityDashboard() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraCommunityRoadFragment_to_meeraCommunityModeratorDashboardFragment,
            bundle = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, eventViewModel.getCommunity()?.groupId ?: 0)
                putInt(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, eventViewModel.getCommunity()?.private ?: 0)
            }
        )

        MeeraCommunityModeratorDashboardFragment().apply {
            dashboardCallback = object : MeeraBaseCommunityDashboardFragment.Callback {
                override fun onCommunityInfoChanged() {
                    eventViewModel.getCommunityInfo(groupId)
                    editCallback?.onCommunityEdited()
                }
            }
        }
    }

    inner class StaticLayoutChangeListenerImpl : UiKitReadMoreTextView.StaticLayoutChangeListener {
        override fun onCountUntilMaxLine(countUntilMaxLine: Int) = Unit

        override fun onLineCountChanged(lineCount: Int) {
            initHeightTitle()
        }
    }

    interface SubscriptionCallback {
        fun onCommunitySubscribed(subscribed: Boolean, groupId: Int)
    }

    interface EditCallback {
        fun onCommunityEdited()
    }

    companion object {
        private const val TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_AUTHOR = 212
        private const val TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_AUTHOR = 195

        private const val TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_AUTHOR = 278
        private const val TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_AUTHOR = 260

        private const val TITLE_MARGIN_BOTTOM_WITH_BIG_DESCRIPTION_AND_NOT_SUBSCRIBED = 199
        private const val TITLE_MARGIN_BOTTOM_WITH_SMALL_DESCRIPTION_AND_NOT_SUBSCRIBED = 182

        private const val DURATION_DESCRIPTION_CONTAINER_ANIM = 200L
        private const val DURATION_ADD_POST_CONTAINER_ANIM = 100L

        private const val DESCRIPTION_TRANSLATION_Y = 200
        private const val ADD_POST_TRANSLATION_Y = 100

        private const val COLLAPSING_PROGRESS_RUN_ANIM = 0.25
        private const val COMMUNITY_TITLE_MAX_LINES = 2
        private const val APP_BAR_OFFSET_PERCENTAGE = 0.7f
        private const val DESCRIPTION_ONE_LINE = 1
    }
}
