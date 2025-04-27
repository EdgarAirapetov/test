package com.numplates.nomera3.modules.communities.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.getToolbarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGroupRoadBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommunityWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.BaseCommunityDashboardFragment
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.CommunityCreatorDashboardFragment
import com.numplates.nomera3.modules.communities.ui.fragment.dashboard.CommunityModeratorDashboardFragment
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import com.numplates.nomera3.modules.communities.ui.viewevent.GetCommunityLinkAction
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityRoadViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunitySubscriptionViewModel
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.communities.utils.shareLinkOutside
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.fragment.BaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_COMMUNITY_FROM
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class CommunityRoadFragment :
    BaseFeedFragment<FragmentGroupRoadBinding>(),
    SwipeRefreshLayout.OnRefreshListener, AppBarLayout.OnOffsetChangedListener,
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
    var groupId: Int? = null
    var transitFrom: Int? = null
    private var infoTooltip: NSnackbar? = null
    private var isScreenshotPopupShown = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGroupRoadBinding
        get() = FragmentGroupRoadBinding::inflate

    override var needToShowProfile: Boolean
        get() = true
        set(value) {}

    fun setSubscriptionCallback(callback: SubscriptionCallback) {
        subscriptionCallback = callback
    }

    fun setEditCallback(callback: EditCallback) {
        editCallback = callback
    }

    override fun getAnalyticPostOriginEnum() = DestinationOriginEnum.COMMUNITY

    override fun getAmplitudeWhereMomentOpened() = AmplitudePropertyMomentScreenOpenWhere.OTHER

    override fun showEmptyFeedPlaceholder() {
        super.showEmptyFeedPlaceholder()

        eventViewModel.getCommunityInfo(groupId)
        binding?.appbar?.setExpanded(true)

        showRoadPlaceholder(
            R.drawable.ic_no_posts_placeholder,
            getString(R.string.placeholder_posts_community)
        )
    }

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.COMMUNITY

    override fun getAmplitudeWhereProfileFromOpened() = AmplitudePropertyWhere.COMMUNITY

    override fun getCommunityId() = groupId?.toLong() ?: -1

    override fun getFormatter(): AllRemoteStyleFormatter {
        return AllRemoteStyleFormatter(feedViewModel.getSettings())
    }

    override fun onScreenshotTaken() {
        resetAllZoomViews()
        if (isSavingFeedPhoto) return
        eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SCREENSHOT_POPUP)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = arguments?.getInt(ARG_GROUP_ID)
        transitFrom = arguments?.getInt(ARG_TRANSIT_COMMUNITY_FROM)
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Community(groupId ?: 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViews()
        initLiveObservables()
        initPostsLiveObservable()
        eventViewModel.getCommunityInfo(groupId)
    }

    override fun getWhereFromHashTagPressed() = AmplitudePropertyWhere.COMMUNITY

    override fun onStop() {
        super.onStop()
        infoTooltip?.dismiss()
    }

    override fun onClickScrollUpButton() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding?.appbar?.setExpanded(true, true)
            delay(300)
            binding?.recyclerCommunityPosts?.scrollToPosition(0)
            onRefresh()
        }
    }

    override fun onRefresh() {
        binding?.vgSwipeLayout?.isRefreshing = false
        eventViewModel.getCommunityInfo(groupId)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (verticalOffset == 0) {
            binding?.vgSwipeLayout?.isEnabled = true
        } else {
            binding?.vgSwipeLayout?.isRefreshing = false
            binding?.vgSwipeLayout?.isEnabled = false
        }

        val maxScroll = appBarLayout.totalScrollRange.toFloat()
        val percentage = abs(verticalOffset) / maxScroll

        if (percentage >= 0.7f && !isExpandedToolbar) {
            act.setLightStatusBar()
            act.changeStatusBarState(Act.LIGHT_STATUSBAR)

            binding?.ivBack?.setColorFilter(getColor(act, R.color.black_1000))
            binding?.ivNotificationBell?.setColorFilter(getColor(act, R.color.black_1000))
            binding?.ivDotsMenu?.setColorFilter(getColor(act, R.color.black_1000))
            isExpandedToolbar = true
        } else if (percentage < 0.7f && isExpandedToolbar) {
            act.setColorStatusBarNavLight()
            act.changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)

            binding?.ivBack?.colorFilter = null
            binding?.ivNotificationBell?.colorFilter = null
            binding?.ivDotsMenu?.colorFilter = null
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

    private fun hideExtraContainer() {
        binding?.clDescriptionContainer
            ?.animate()
            ?.translationY(dpToPx(DESCRIPTION_TRANSLATION_Y).toFloat())
            ?.setListener(onAnimationEnd = {
                binding?.flAddPostContainer
                    ?.animate()
                    ?.translationY(dpToPx(ADD_POST_TRANSLATION_Y).toFloat())
                    ?.setInterpolator(DecelerateInterpolator())
                    ?.duration = DURATION_ADD_POST_CONTAINER_ANIM
            })
            ?.setInterpolator(DecelerateInterpolator())
            ?.duration = DURATION_DESCRIPTION_CONTAINER_ANIM
    }

    private fun showExtraContainer() {
        binding?.clDescriptionContainer
            ?.animate()
            ?.translationY(0f)
            ?.setInterpolator(DecelerateInterpolator())
            ?.setListener(onAnimationEnd = {
                binding?.flAddPostContainer
                    ?.animate()
                    ?.translationY(0f)
                    ?.setInterpolator(DecelerateInterpolator())
                    ?.duration = DURATION_ADD_POST_CONTAINER_ANIM
            })
            ?.duration = DURATION_DESCRIPTION_CONTAINER_ANIM
    }

    private fun initToolbar() {
        val layoutParams = binding?.toolbar?.layoutParams as CollapsingToolbarLayout.LayoutParams
        val statusBarHeight = context.getStatusBarHeight()
        val toolbarHeight = context.getToolbarHeight()
        layoutParams.height = toolbarHeight + statusBarHeight
        binding?.toolbar?.layoutParams = layoutParams
        binding?.ivBack?.setMargins(top = statusBarHeight)
        binding?.ivDotsMenu?.setMargins(top = statusBarHeight)
        binding?.ivNotificationBell?.setMargins(top = statusBarHeight)
        binding?.appbar?.addOnOffsetChangedListener(this)
        binding?.ivBack?.click {
            act.onBackPressed()
        }
    }

    private fun initViews() {
        binding?.addGroupPost?.separatorTwo?.gone()
        binding?.vgSwipeLayout?.setOnRefreshListener(this)
        binding?.appbar?.addOnOffsetChangedListener(this)

        binding?.addGroupPost?.tvFieldAddPost?.click {
            gotoAddPost(groupId, false)
        }
    }

    private fun gotoAddPost(groupId: Int?, isShowGallery: Boolean = false) {
        add(
            AddMultipleMediaPostFragment(), Act.LIGHT_STATUSBAR,
            Arg(ARG_GROUP_ID, groupId),
            Arg(ARG_SHOW_MEDIA_GALLERY, isShowGallery),
            Arg(AddMultipleMediaPostFragment.OpenFrom.EXTRA_KEY, AddMultipleMediaPostFragment.OpenFrom.Community)
        )
    }

    private fun initLiveObservables() {
        eventViewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleRoadViewEvents)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                subscriptionViewModel.viewEvent.collect(::handleRoadViewEvents)
            }
        }
    }

    private fun handleRoadViewEvents(event: CommunityViewEvent?) {
        when (event) {
            is CommunityViewEvent.SuccessSubscribeCommunity -> {
                setJoinButton(true)
                showSuccessMessage(R.string.group_subscription_success)
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(true, it) }
                eventViewModel.getCommunity()?.let {
                    it.isSubscribed = 1
                    it.subscribedNotifications = 1
                    initNotificationsAndSettings(it)
                    initAddPostView(it)
                }
            }

            is CommunityViewEvent.SuccessUnsubscribeCommunity -> {
                setJoinButton(false)
                event.groupId?.let { subscriptionCallback?.onCommunitySubscribed(false, it) }
                eventViewModel.getCommunity()?.let {
                    it.isSubscribed = 0
                    initNotificationsAndSettings(it)
                    initAddPostView(it)
                }
            }

            is CommunityViewEvent.CommunityData -> {
                showGroupData(event.community)
                configureCreatePostBtn(event.permission, event.community.private)
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

            is CommunityViewEvent.FailureGetCommunityInfo -> {
                showErrorMessage(R.string.group_error_load_group_data)
            }

            is CommunityViewEvent.FailureCommunityNotFound -> {
                showErrorMessage(R.string.community_unavailable)
            }

            is CommunityViewEvent.FailureSubscribeCommunity -> {
                showErrorMessage(R.string.group_error_subscribe_group)
            }

            is CommunityViewEvent.FailureUnsubscribeCommunity -> {
                showErrorMessage(R.string.group_error_unsubscribe_group)
            }

            is CommunityViewEvent.CommunityDataProgress -> {
                showCommunityDataProgress(event.inProgress)
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
            is CommunityViewEvent.RefreshCommunityRoad -> onRefresh()

            else -> Unit
        }
    }

    private fun openAdminChat(adminId: Long) {
        act?.addFragment(
            ChatFragmentNew(), Act.LIGHT_STATUSBAR,
            Arg(
                IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                    initType = ChatInitType.FROM_PROFILE,
                    userId = adminId
                )
            )
        )
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
        binding?.addGroupPost?.tvFieldAddPost?.click {
            showDialogNoPermissionToCreatePostInCommunity()
        }

        binding?.placeholder?.tvClickableText?.click {
            showDialogNoPermissionToCreatePostInCommunity()
        }
    }

    private fun showDialogNoPermissionToCreatePostInCommunity() {
        val dialog = BlockUserByAdminDialogFragment()
        dialog.blockReason = this.userPermissions?.userBlockInfo?.blockReasonText
        dialog.blockDateUnixtimeSec = this.userPermissions?.userBlockInfo?.blockedUntil
        dialog.closeDialogClickListener = { dialog.dismiss() }
        dialog.writeSupportClickListener = {
            // eventViewModel.onWriteToTechSupportClicked() // Отключил "Написать в чат техподдержке"
            act?.navigateToTechSupport()
        }
        dialog.show(childFragmentManager, "blocked_user_dialog")
    }

    private fun handleGetCommunityLink(event: CommunityViewEvent.SuccessGetCommunityLink) {
        when (event.action) {
            GetCommunityLinkAction.SHARE_LOCAL -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraShowShareCommunityDialog(event.link)
                    },
                    isNotRedesigned = {
                        showShareCommunityDialog(event.link)
                    }
                )
                eventViewModel.logCommunityShare(AmplitudePropertyCommunityWhere.INSIDE)
            }
            GetCommunityLinkAction.SHARE_OUTSIDE -> {
                copyCommunityLink(context, event.link) {
                    showInfoTooltip(R.string.copy_link_success)
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
        binding?.collapsingToolbarCommunityInfo?.expandedTitleMarginBottom = marginDp
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
        binding?.collapsingToolbarCommunityInfo?.setCollapsedTitleTypeface(
            ResourcesCompat.getFont(act, R.font.source_sanspro_bold)
        )
        binding?.collapsingToolbarCommunityInfo?.setExpandedTitleTypeface(
            ResourcesCompat.getFont(act, R.font.source_sanspro_bold)
        )
        binding?.collapsingToolbarCommunityInfo?.setExpandedTitleColor(
            getColor(act, R.color.ui_white)
        )
        binding?.collapsingToolbarCommunityInfo?.setCollapsedTitleTextColor(
            getColor(act, R.color.black_85)
        )

        val params = binding?.collapsingToolbarCommunityInfo?.layoutParams
            as? AppBarLayout.LayoutParams

        params?.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
            AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

        binding?.collapsingToolbarCommunityInfo?.layoutParams = params
        binding?.collapsingToolbarCommunityInfo?.title = community.name
        binding?.collapsingToolbarCommunityInfo?.maxLines = 2

    }

    private fun initAvatar(community: CommunityEntity) {
        community.avatarBig?.let { avatar ->
            binding?.communityCoverImage?.let { imageView ->
                Glide.with(act.applicationContext)
                    .load(avatar)
                    .placeholder(R.drawable.community_cover_image_placeholder_big)
                    .into(imageView)

                if (avatar.isNotEmpty()) {
                    imageView.click {
                        checkAppRedesigned(
                            isRedesigned = {
//                                add(
//                                    MeeraProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
//                                    Arg(IArgContainer.ARG_IMAGE_URL, avatar),
//                                    Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.COMMUNITY)
//                                )
                            },
                            isNotRedesigned = {
                                add(
                                    ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
                                    Arg(IArgContainer.ARG_IMAGE_URL, avatar),
                                    Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.COMMUNITY)
                                )
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
            binding?.ivNotificationBell?.visible()
        } else if (community.isSubscribed.isFalse()) {
            binding?.ivNotificationBell?.gone()
        }

        val isCreator = community.isAuthor.isTrue()
        val isModerator = community.isModerator.isTrue()

        binding?.ivDotsMenu?.click {
            if (isCreator || isModerator) {
                showDotsMenu(isCreator, isSettingsAvailable = true)
            } else {
                showDotsMenu(isCreator, isSettingsAvailable = false)
            }
        }
    }

    private fun showDotsMenu(isCreator: Boolean, isSettingsAvailable: Boolean) {
        val menu = MeeraMenuBottomSheet(context)

        menu.addItem(R.string.share_community, R.drawable.ic_share_purple_new) {
            eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SHARE_LOCAL)
        }

        menu.addItem(R.string.copy_link, R.drawable.ic_chat_copy_message) {
            eventViewModel.getCommunityLink(groupId, GetCommunityLinkAction.SHARE_OUTSIDE)
        }

        if (isSettingsAvailable) {
            menu.addItem(R.string.profile_settings, R.drawable.ic_settings_purple_new) {
                if (isCreator) {
                    openCreatorCommunityDashboard()
                } else {
                    openModeratorCommunityDashboard()
                }
            }
        }

        menu.show(childFragmentManager)
    }

    private fun showShareCommunityDialog(link: String) {
        groupId?.let { id ->
            SharePostBottomSheet(ShareDialogType.ShareCommunity(id)) { shareEvent ->
                when (shareEvent) {
                    is ShareBottomSheetEvent.OnSuccessShareCommunity -> {
                        showInfoTooltip(R.string.share_community_success)
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
                    is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                        add(
                            SearchMainFragment(),
                            Act.LIGHT_STATUSBAR,
                            Arg(
                                IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                AmplitudeFindFriendsWhereProperty.SHARE
                            )
                        )
                    }
                    else -> {}
                }
            }.show(childFragmentManager)
        }
    }

    private fun meeraShowShareCommunityDialog(link: String) {
        groupId?.let { id ->
            MeeraShareSheet().showByType(
                fm = childFragmentManager,
                shareType = ShareDialogType.ShareCommunity(id),
                event = { shareEvent ->
                    when (shareEvent) {
                        is ShareBottomSheetEvent.OnSuccessShareCommunity -> {
                            showInfoTooltip(R.string.share_community_success)
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
                        is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                            add(
                                SearchMainFragment(),
                                Act.LIGHT_STATUSBAR,
                                Arg(
                                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                    AmplitudeFindFriendsWhereProperty.SHARE
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            )
        }
    }

    private fun showInfoTooltip(@StringRes text: Int) {
        val navBarHeightDp = pxToDp(context.getNavigationBarHeight())
        infoTooltip = NSnackbar.with(requireView())
            .typeSuccess()
            .marginBottom(navBarHeightDp + INFO_TOOLTIP_MARGIN_BOTTOM)
            .text(getString(text))
            .durationLong()
            .show()
    }

    private fun setNotificationsAllowed(groupId: Int?) {
        binding?.ivNotificationBell?.isEnabled = true
        binding?.ivNotificationBell?.setImageResource(R.drawable.ic_profile_notification_on)
        binding?.ivNotificationBell?.click {
            it.isEnabled = false
            eventViewModel.unsubscribeNotifications(groupId)
        }
    }

    private fun setNotificationsNotAllowed(groupId: Int?) {
        binding?.ivNotificationBell?.isEnabled = true
        binding?.ivNotificationBell?.setImageResource(R.drawable.ic_profile_notification_off)
        binding?.ivNotificationBell?.click {
            it.isEnabled = false
            eventViewModel.subscribeNotifications(groupId)
        }
    }

    private fun initSubscribersCount(community: CommunityEntity) {
        community.users.let {
            binding?.tvUserCount?.text = context?.pluralString(R.plurals.group_members_plural, it)
        }
    }

    private fun initDescription(community: CommunityEntity) {
        binding?.tvDescriptionText?.text = community.description
        binding?.tvDescriptionDetails?.click {
            showCommunityDetails(community)
        }
    }

    private fun showCommunityDetails(community: CommunityEntity) {
        CommunityDetailsBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putInt(IArgContainer.ARG_GROUP_ID, community.groupId)
                putBoolean(IArgContainer.ARG_COMMUNITY_SHOW_MEMBERS, showMembers(community))
            }
            show(this@CommunityRoadFragment.childFragmentManager, tag)
        }
    }

    private fun showMembers(community: CommunityEntity): Boolean {
        return when {
            community.userStatus == CommunityEntity.USER_STATUS_BANNED -> false
            community.private.isTrue() && community.isSubscribed.isFalse() -> false
            else -> true
        }
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
            binding?.flAddPostContainer?.visible()
            setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITH_ADD_POST.dp)
            binding?.placeholder?.tvClickableText?.click {
                gotoAddPost(community.groupId)
            }
            binding?.placeholder?.tvClickableText?.visible()
        } else {
            binding?.flAddPostContainer?.gone()
            setToolbarTitleExpandedMarginBottom(TITLE_MARGIN_BOTTOM_WITHOUT_ADD_POST.dp)
            binding?.placeholder?.tvClickableText?.gone()
        }
    }

    private fun initPosts(community: CommunityEntity) {
        when {
            community.userStatus == CommunityEntity.USER_STATUS_BANNED -> {
                showRoadPlaceholder(
                    R.drawable.ic_user_blocked_in_community,
                    getString(R.string.community_user_blocked_road_placeholder_text)
                )
            }
            community.private.isTrue() &&
                community.isSubscribed.isFalse() -> {
                showRoadPlaceholder(
                    R.drawable.ic_closed_community,
                    getString(R.string.community_private_road_placeholder_text)
                )
            }
            community.posts.isFalse() &&
                community.userStatus != CommunityEntity.USER_STATUS_BANNED -> {
                showRoadPlaceholder(
                    R.drawable.ic_no_posts_placeholder,
                    getString(R.string.placeholder_posts_community)
                )
            }
            community.private.isFalse() ||
                community.isAuthor.isTrue() ||
                community.isSubscribed.isTrue() -> {
                binding?.placeholder?.root?.gone()
                initRoad(community)
                binding?.recyclerCommunityPosts?.visible()
                binding?.vgSwipeLayout?.isEnabled = true
            }
            else -> {
                binding?.vgSwipeLayout?.isRefreshing = false
                binding?.vgSwipeLayout?.isEnabled = false
                binding?.placeholder?.root?.visible()
                binding?.recyclerCommunityPosts?.gone()
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
        binding?.placeholder?.ivIcon?.setImageResource(iconResource)
        binding?.placeholder?.tvCaption?.text = text
        binding?.placeholder?.root?.visible()
        binding?.recyclerCommunityPosts?.gone()
    }

    private fun initRoad(community: CommunityEntity) {
        binding?.recyclerCommunityPosts?.layoutManager = LinearLayoutManager(context)
        initPostsAdapter(
            roadType = NetworkRoadType
                .COMMUNITY(community.groupId, community.private.isTrue(), REQUEST_ROAD_TYPE_GROUP),
            recyclerView = binding?.recyclerCommunityPosts,
            lottieAnimationView = binding?.btnScrollRefreshCommunityRoad
        )
        binding?.recyclerCommunityPosts?.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
        binding?.recyclerCommunityPosts?.adapter = getAdapterPosts()
        startLoadPosts()
    }

    private fun initJoinButton(community: CommunityEntity) {
        // Private settings
        if (community.isAuthor.isTrue() ||
            community.userStatus == CommunityEntity.USER_STATUS_BANNED
        ) {
            binding?.tvJoin?.gone()
        } else {
            binding?.tvJoin?.visible()
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
                binding?.tvJoin?.setText(R.string.group_joined)
                binding?.tvJoin?.setBackgroundResource(R.drawable.community_subscribed_border)
                binding?.tvJoin?.click { unsubscribeClicked() }
            }
            else -> {
                binding?.tvJoin?.setText(R.string.group_join)
                binding?.tvJoin?.setBackgroundResource(R.drawable.community_subscribe_gradient)
                binding?.tvJoin?.click {
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

    private fun setJoinPrivateButton(joined: Boolean? = null) {
        when {
            eventViewModel.getCommunity()?.userStatus == CommunityEntity.USER_STATUS_NOT_YET_APPROVED -> {
                binding?.tvJoin?.setText(R.string.group_join_request_sent)
                binding?.tvJoin?.setBackgroundResource(R.drawable.community_subscribed_border)
                binding?.tvJoin?.click { unsubscribeClicked() }
            }
            joined == true -> {
                binding?.tvJoin?.setText(R.string.group_joined)
                binding?.tvJoin?.setBackgroundResource(R.drawable.community_subscribed_border)
                binding?.tvJoin?.click { unsubscribeClicked() }
            }
            joined == false -> {
                binding?.tvJoin?.setText(R.string.groups_request)
                binding?.tvJoin?.setBackgroundResource(R.drawable.community_subscribe_gradient)
                binding?.tvJoin?.click {
                    subscriptionViewModel.subscribeCommunity(
                        eventViewModel.getCommunityListItemUIModel(),
                        AmplitudePropertyWhereCommunityFollow.COMMUNITY
                    )
                }
            }
        }
    }

    private fun unsubscribeClicked() {
        when {
            eventViewModel.getCommunity()?.isModerator == 1 -> {
                showUnsubscribeCommunityDialog(R.string.community_unsubscribe_admin_text) {
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
            binding?.ivPrivateIcon?.visible()
            binding?.ivPrivateDivider?.visible()
        } else {
            binding?.ivPrivateIcon?.gone()
            binding?.ivPrivateDivider?.gone()
        }
    }

    private fun refreshGroupInfo(groupId: Int?) {
        eventViewModel.getCommunityInfo(groupId)
    }

    private fun showCommunityDataProgress(inProgress: Boolean) {
        if (inProgress) binding?.loadingCircle?.visible()
        else binding?.loadingCircle?.gone()
    }

    private fun showSubscribeCommunityProgress(inProgress: Boolean) {
        binding?.tvJoin?.isEnabled = !inProgress
        if (inProgress) {
            binding?.pbSubscribe?.visible()
            binding?.tvJoin?.invisible()
        } else {
            binding?.pbSubscribe?.gone()
            binding?.tvJoin?.visible()
        }
    }

    private fun showNotificationBellProgress(inProgress: Boolean) {
        if (inProgress) {
            binding?.ivNotificationBell?.gone()
            binding?.pbNotifications?.visible()
        } else {
            binding?.ivNotificationBell?.visible()
            binding?.pbNotifications?.gone()
        }
    }

    private fun showSuccessMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeSuccess()
            .text(getString(messageRes))
            .show()
    }

    private fun showErrorMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeError()
            .text(getString(messageRes))
            .show()
    }

    private fun showUnsubscribeCommunityDialog(
        @StringRes textRes: Int,
        unsubscribe: () -> Unit
    ) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.community_unsubscribe_dialog_title))
            .setDescription(getString(textRes))
            .setLeftBtnText(getString(R.string.community_unsubscribe))
            .setRightBtnText(getString(R.string.unsubscribe_dialog_close))
            .setLeftClickListener { unsubscribe() }
            .show(childFragmentManager)
    }

    private fun openCreatorCommunityDashboard() {
        add(
            CommunityCreatorDashboardFragment().apply {
                dashboardCallback = object : BaseCommunityDashboardFragment.Callback {
                    override fun onCommunityInfoChanged() {
                        eventViewModel.getCommunityInfo(groupId)
                        editCallback?.onCommunityEdited()
                    }
                }
            },Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, eventViewModel.getCommunity()?.groupId),
            Arg(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, eventViewModel.getCommunity()?.private)
        )
    }

    private fun openModeratorCommunityDashboard() {
        add(
            CommunityModeratorDashboardFragment().apply {
                dashboardCallback = object : BaseCommunityDashboardFragment.Callback {
                    override fun onCommunityInfoChanged() {
                        eventViewModel.getCommunityInfo(groupId)
                        editCallback?.onCommunityEdited()
                    }
                }
            }, Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, eventViewModel.getCommunity()?.groupId),
            Arg(IArgContainer.ARG_COMMUNITY_IS_PRIVATE, eventViewModel.getCommunity()?.private)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerCommunityPosts?.onDestroyView()
        binding?.recyclerCommunityPosts?.adapter = null
        editCallback = null
        subscriptionCallback = null
    }

    interface SubscriptionCallback {
        fun onCommunitySubscribed(subscribed: Boolean, groupId: Int)
    }

    interface EditCallback {
        fun onCommunityEdited()
    }

    companion object {
        private const val TITLE_MARGIN_BOTTOM_WITH_ADD_POST = 230
        private const val TITLE_MARGIN_BOTTOM_WITHOUT_ADD_POST = 150

        private const val DURATION_DESCRIPTION_CONTAINER_ANIM = 200L
        private const val DURATION_ADD_POST_CONTAINER_ANIM = 100L

        private const val DESCRIPTION_TRANSLATION_Y = 200
        private const val ADD_POST_TRANSLATION_Y = 100

        private const val COLLAPSING_PROGRESS_RUN_ANIM = 0.25

        private const val INFO_TOOLTIP_MARGIN_BOTTOM = 32
    }
}

enum class CommunityTransitFrom(val key: Int) {
    FEED(1),
    OWN_PROFILE(2),         // not implemented
    ALL_COMMUNITY(3),       // +
    NOTIFICATIONS(5);       // +
}
