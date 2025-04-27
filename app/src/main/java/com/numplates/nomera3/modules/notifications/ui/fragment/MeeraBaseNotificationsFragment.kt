package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.addAnimationTransitionByDefault
import com.meera.core.extensions.safeNavigate
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.IPushInfo
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.databinding.MeeraFragmentNotificationsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityTransitFrom
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_COMMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_LAST_REACTION_TYPE
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_PUSH_INFO
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_TARGET_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.notifications.ui.adapter.MeeraNotificationPagingListAdapter
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions
import com.numplates.nomera3.modules.notifications.ui.itemdecorator.makeMeeraDefNotificationDivider
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment.Companion.ARG_EVENT_POST_ID
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment.Companion.ARG_LOG_MAP_OPEN_WHERE
import com.numplates.nomera3.modules.redesign.toolbar.ToolbarStateManager
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment.Companion.USERINFO_OPEN_FROM_NOTIFICATION
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_LAST_REACTION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_LATEST_REACTION_TYPE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_COMMUNITY_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment
import com.numplates.nomera3.presentation.view.utils.SwipeToDeleteUtils
import kotlin.properties.Delegates

private const val DELAY_DELETE_NOTIFICATION_SEC = 5L

abstract class MeeraBaseNotificationsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_notifications,
    behaviourConfigState = ScreenBehaviourState.BottomScreens(percentHeight = 0.95F)
) {

    protected val binding by viewBinding(MeeraFragmentNotificationsBinding::bind)

    protected var pagingAdapter: MeeraNotificationPagingListAdapter by Delegates.notNull()

    private var swipeHandler: SwipeToDeleteUtils? = null
    private var pendingDeleteSnackbar: UiKitSnackBar? = null
    private var pendingDeleteAction: (() -> Unit)? = null

    private val toolbarStateManager by lazy { ToolbarStateManager() }

    abstract fun restoreNotificationIfNotNull(id: String)

    abstract fun deleteNotification(id: String, isGroup: Boolean)

    abstract fun markAsRead(id: String, isGroup: Boolean)

    abstract fun readAll()

    abstract fun deleteAll()

    abstract fun onItemDeleted()

    abstract fun onUserSetCache(user: User?)

    abstract fun logOpenCommunity()

    private val deleteNotificationState = linkedMapOf<String, NotificationCellUiModel>()

    override val containerId: Int
        get() = R.id.fragment_second_container_view

    open fun getDividerDecorator(): RecyclerView.ItemDecoration? = context?.makeMeeraDefNotificationDivider()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        toolbarStateManager.attachToLifecycle(requireContext(), viewLifecycleOwner.lifecycle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NavigationManager.getManager().setContainerViewTransparent()
    }

    protected fun initRecycler(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            getDividerDecorator()?.let { decorator -> addItemDecoration(decorator) }
            pagingAdapter = MeeraNotificationPagingListAdapter(clickNotification)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = pagingAdapter
        }
    }

    fun clearNotificationCounter() {
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().notificationsCount = 0
    }

    fun updateNotificationCounter(count: Int) {
        NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().notificationsCount = count
    }

    open fun setupSwipeDeleteHandler(recyclerView: RecyclerView) {
        swipeHandler = SwipeToDeleteUtils(requireContext(), SwipeToDeleteUtils.SwipeType.MEERAFULL)
            .apply {
                onFullSwiped = { position ->
                    val item = pagingAdapter.getItemForPosition(position)
                    item?.let { notification ->
                        deleteNotificationState[notification.id] = notification
                        handlePreviousDeletedNotifications(notification.id)
                        showDeleteInfoSnackbar(notification)
                    }
                }
            }

        swipeHandler?.let { swipeHelper ->
            ItemTouchHelper(swipeHelper).apply {
                attachToRecyclerView(recyclerView)
            }
        }
    }

    private fun showDeleteInfoSnackbar(notification: NotificationCellUiModel) {
        pendingDeleteAction?.invoke()
        pendingDeleteAction = {
            deleteNotificationState.clear()
            swipeHandler?.isSwipeEnable = true
            actionDeleteNotification(notification)
        }
        pendingDeleteSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.notification_is_deleting),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_NOTIFICATION_SEC,
                        onTimerFinished = {
                            pendingDeleteAction?.invoke()
                        }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        dismissSnackBar(notification)
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        dismissSnackBar(notification)
                    }
                )
            )
        )

        pendingDeleteSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        pendingDeleteSnackbar?.show()
    }

    private fun dismissSnackBar(notification: NotificationCellUiModel) {
        if (pendingDeleteSnackbar == null) return
        pendingDeleteSnackbar?.dismiss()
        deleteNotificationState.clear()
        pendingDeleteSnackbar = null
        pendingDeleteAction = null
        swipeHandler?.isSwipeEnable = true
        restoreNotification(notification)
    }

    private fun restoreNotification(notification: NotificationCellUiModel) {
        restoreNotificationIfNotNull(notification.id)
    }

    open fun onStopNotificationsFragment() {
        swipeHandler?.isSwipeEnable = true
        pendingDeleteSnackbar?.dismiss()
        pendingDeleteSnackbar = null
        pendingDeleteAction?.invoke()
        pendingDeleteAction = null
    }

    fun openChatFragment(userId: String) {
        findNavController().safeNavigate(
            resId = R.id.meeraChatFragment,
            bundle = Bundle().apply {
                putString(ARG_USER_ID, userId)
                putString(IArgContainer.ARG_ROOM_TYPE, ROOM_TYPE_DIALOG)
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
                it
            }
        )
    }

    private val clickNotification: (
        NotificationTransitActions,
        isGroupedNotifications: Boolean
    ) -> Unit = { notification, isGrouped -> handleClickNotificationItem(notification, isGrouped) }

    private fun handlePreviousDeletedNotifications(currentId: String) {
        if (deleteNotificationState.size > 1) {
            deleteNotificationState.forEach { (notificationId, notification) ->
                if (notificationId != currentId) {
                    actionDeleteNotification(notification)
                }
            }
        }
    }

    private fun actionDeleteNotification(notification: NotificationCellUiModel) {
        deleteNotification(notification.id, notification.data.isGroup)
        onItemDeleted()
    }

    private fun handleClickNotificationItem(
        action: NotificationTransitActions,
        isGroupedNotifications: Boolean
    ) {
        when (action) {
            is NotificationTransitActions.OnTransitToMomentScreen -> gotoMoment(
                userId = action.userId,
                openMomentAnimationView = action.momentScreenOpenAnimationView,
                momentId = action.momentId,
                pushInfo = action.pushInfo,
                latestReactionType = action.latestReactionType,
                commentId = action.commentId,
                hasNewMoments = action.hasNewMoments
            )

            is NotificationTransitActions.OnTransitToPostScreen ->
                gotoPost(action.postId, action.pushInfo, action.latestReactionType)

            is NotificationTransitActions.OnTransitToCommentPostScreen ->
                gotoPostWithComment(action.postId, action.commentId)

            is NotificationTransitActions.OnTransitToCommentPostScreenWithReactions -> gotoPostWithCommentWithReactions(
                postId = action.postId,
                commentId = action.commentId,
                latestReactionType = action.latestReactionType
            )

            is NotificationTransitActions.OnTransitToProfileViewScreen -> gotoProfileView(action.postId)
            is NotificationTransitActions.OnTransitToIncomingFriendRequestScreen -> gotoFriendsIncoming()
            is NotificationTransitActions.OnTransitToUserProfileScreen -> gotoUserProfile(action.user)
            is NotificationTransitActions.OnTransitToGiftScreen -> gotoUserGifts(null, null)
            is NotificationTransitActions.OnTransitToGroupChat -> gotoGroupChat(action.roomId)
            is NotificationTransitActions.OnUserAvatarClicked -> gotoUserProfile(action.user)
            is NotificationTransitActions.OnSystemNotification -> handleSystemNotification(action.uri)
            is NotificationTransitActions.OnTransitToPrivateGroupRequest -> goToPrivateGroup(action.groupId)
            is NotificationTransitActions.OnBirthdayUserClicked -> goToUserChat(action.user)

            is NotificationTransitActions.OnCommunityNotificationIconClicked ->
                openCommunityRoadFragment(action.communityId)

            is NotificationTransitActions.OnCreateAvatarClicked -> {
                action.notifId?.let { markAsRead(it, false) }
                goToCreateAvatar()
            }

            is NotificationTransitActions.OnUserSoftBlockClicked -> handleClickUserSoftBloc(action)
            is NotificationTransitActions.OnTransitToChatScreen -> goToUserChat(action.user)
            is NotificationTransitActions.OnTransitToEventView -> goToEventView(action.postId)
            else -> Unit
        }

        if (!isGroupedNotifications) {
            action.notifId?.let { id ->
                if (action.isGroup == false) {
                    markAsRead(id, action.isGroup ?: false)
                }
            }
        }

        if (isGroupedNotifications && action is NotificationTransitActions.OnTransitGroupNotificationsScreen) {
            gotoGroupDetail(action.notificationId, action.type, action.isRead)
        }
    }

    private fun openCommunityRoadFragment(communityId: Int) {
        findNavController().safeNavigate(
            resId = R.id.meeraCommunityRoadFragmentMainFlow,
            bundle = Bundle().apply {
                putInt(ARG_GROUP_ID, communityId)
                putInt(ARG_TRANSIT_COMMUNITY_FROM, CommunityTransitFrom.NOTIFICATIONS.key)
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
        logOpenCommunity()
    }

    private fun goToPrivateGroup(groupId: Long?) {
        groupId?.let {
            findNavController().safeNavigate(
                resId = R.id.meeraCommunityMembersContainerFragment,
                bundle = Bundle().apply {
                    putInt(ARG_GROUP_ID, it.toInt())
                    putBoolean(IArgContainer.ARG_IS_FROM_PUSH, true)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    open fun goToCreateAvatar() = Unit

    @Suppress("UNUSED_PARAMETER")
    private fun handleSystemNotification(uri: String?) {
//        val deeplinkWithOrigin = uri?.let { FeatureDeepLink.addDeeplinkOrigin(it, DeeplinkOrigin.NOTIFICATIONS) }
//        act.openLink(deeplinkWithOrigin)
    }

    private fun handleClickUserSoftBloc(action: NotificationTransitActions.OnUserSoftBlockClicked) {
        if (action.isBlocked) {
            showBlockUserDialog(action.blockReason, action.blockedTo)
        } else {
//            act.goToMainRoad()
        }
    }

    private fun showBlockUserDialog(reason: String, expiredAt: Long) {
        val dialog = BlockUserByAdminDialogFragment()
        dialog.blockReason = reason
        dialog.blockDateUnixtimeSec = expiredAt
        dialog.closeDialogClickListener = { dialog.dismiss() }
        dialog.headerDialogType = HeaderDialogType.MainRoadType
        dialog.show(childFragmentManager, "blocked_user_dialog")
    }

    private fun gotoUserGifts(userId: Long?, userName: String?) {
        if (userId == null) {
            findNavController().safeNavigate(
                resId = R.id.meeraUserGiftsFragment,
                bundle = Bundle().apply {
                    putBoolean(IArgContainer.ARG_IS_WORTH_TO_SHOW_DIALOG, true)
                    putSerializable(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        } else {
            findNavController().safeNavigate(
                resId = R.id.meeraGiftsListFragment,
                bundle = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                    putString(IArgContainer.ARG_USER_NAME, userName)
                    putSerializable(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun gotoMoment(
        userId: Long,
        momentId: Long? = null,
        commentId: Long? = null,
        openMomentAnimationView: View? = null,
        pushInfo: String? = null,
        latestReactionType: ReactionType? = null,
        hasNewMoments: Boolean?
    ) {
        findNavController().safeNavigate(
            resId = R.id.meeraViewMomentFragment,
            bundle = Bundle().apply {
                putLong(KEY_USER_ID, userId)
                momentId?.let { putLong(KEY_MOMENT_TARGET_ID, it) }
                commentId?.let { putLong(KEY_MOMENT_COMMENT_ID, it) }
                latestReactionType?.let { putSerializable(KEY_MOMENT_LAST_REACTION_TYPE, it) }
                pushInfo?.let { putString(KEY_MOMENT_PUSH_INFO, it) }
                putParcelable(KEY_MOMENT_CLICK_ORIGIN, MomentClickOrigin.fromUserAvatar())
            }
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun gotoPost(
        postId: Long?,
        pushInfo: String? = null,
        latestReactionType: ReactionType? = null
    ) {
        val postOriginType = if (pushInfo == IPushInfo.POST_REACTION) {
            DestinationOriginEnum.NOTIFICATIONS_REACTIONS
        } else {
            DestinationOriginEnum.NOTIFICATIONS
        }
        postId?.let { id ->
            findNavController().safeNavigate(
                resId = R.id.meeraPostFragmentV2,
                bundle = Bundle().apply {
                    putLong(ARG_FEED_POST_ID, id)
                    putSerializable(ARG_POST_ORIGIN, postOriginType)
                    putSerializable(ARG_POST_LATEST_REACTION_TYPE, latestReactionType)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun gotoPostWithComment(postId: Long?, commentId: Long?) {
        postId?.let { id ->
            findNavController().safeNavigate(
                resId = R.id.meeraPostFragmentV2,
                bundle = Bundle().apply {
                    putLong(ARG_FEED_POST_ID, id)
                    putSerializable(ARG_COMMENT_ID, commentId)
                    putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun gotoPostWithCommentWithReactions(
        postId: Long?,
        commentId: Long?,
        latestReactionType: ReactionType?
    ) {
        if (postId == null) return
        findNavController().safeNavigate(
            resId = R.id.meeraPostFragmentV2,
            bundle = Bundle().apply {
                putLong(ARG_FEED_POST_ID, postId)
                putSerializable(ARG_COMMENT_ID, commentId)
                putSerializable(ARG_COMMENT_LAST_REACTION, latestReactionType)
                putSerializable(ARG_POST_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    private fun gotoProfileView(postId: Long?) {
        postId?.let {
            findNavController().safeNavigate(
                resId = R.id.meeraProfilePhotoViewerFragment,
                bundle = Bundle().apply {
                    putBoolean(IArgContainer.ARG_IS_PROFILE_PHOTO, false)
                    putBoolean(IArgContainer.ARG_IS_OWN_PROFILE, true)
                    putLong(IArgContainer.ARG_POST_ID, postId)
                    putSerializable(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    private fun gotoFriendsIncoming() {
        findNavController().safeNavigate(
            resId = R.id.meeraFriendsHostFragment,
            bundle = Bundle().apply {
                putBoolean(IArgContainer.ARG_IS_GOTO_INCOMING, true)
                putSerializable(
                    IArgContainer.ARG_TYPE_FOLLOWING,
                    MeeraFriendsHostFragment.SelectedPage.INCOMING_REQUESTS
                )
                putSerializable(
                    IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM,
                    FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_NOTIFICATIONS
                )
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    private fun gotoUserProfile(user: User?) {
        user?.userId?.toLong()?.let { userId ->
            findNavController().safeNavigate(
                resId = R.id.userInfoFragment,
                bundle = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                    putString(ARG_TRANSIT_FROM, AmplitudePropertyWhere.NOTIFICATION.property)
                    putBoolean(USERINFO_OPEN_FROM_NOTIFICATION, true)
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    private fun gotoGroupChat(roomId: Long?) {
        roomId?.let { id ->
            findNavController().safeNavigate(
                resId = R.id.meeraChatFragment,
                bundle = Bundle().apply {
                    putSerializable(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NOTIFICATIONS)
                    putParcelable(
                        IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                            initType = ChatInitType.FROM_LIST_ROOMS,
                            roomId = roomId
                        )
                    )
                },
                navBuilder = {
                    it.addAnimationTransitionByDefault()
                }
            )
        }
    }

    private fun goToUserChat(user: User?) {
        onUserSetCache(user)
        findNavController().safeNavigate(
            resId = R.id.meeraChatFragment,
            bundle = Bundle().apply {
                putParcelable(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = user?.userId?.toLong()
                    )
                )
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun goToEventView(postId: Long) {
        findNavController().safeNavigate(
            resId = R.id.mainMapFragment,
            bundle = Bundle().apply {
                putLong(ARG_EVENT_POST_ID, postId)
                putSerializable(ARG_LOG_MAP_OPEN_WHERE, AmplitudePropertyWhere.OTHER)
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    private fun gotoGroupDetail(notificationId: String, type: String, isRead: Boolean) {
        onStopNotificationsFragment()
        findNavController().safeNavigate(
            resId = R.id.notificationDetailFragment,
            bundle = Bundle().apply {
                putString(MeeraNotificationDetailFragment.NOTIFICATION_ID, notificationId)
                putString(MeeraNotificationDetailFragment.NOTIFICATION_TYPE, type)
                putBoolean(MeeraNotificationDetailFragment.NOTIFICATION_IS_READ, isRead)
            },
            navBuilder = {
                it.addAnimationTransitionByDefault()
            }
        )
    }

    protected fun showConfirmDeleteDialog(onConfirmDeletion: () -> Unit) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.delete_all_notifications)
            .setDescription(R.string.meera_notifications_list_will_be_clean)
            .setTopBtnText(R.string.general_delete)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener { onConfirmDeletion.invoke() }
            .show(childFragmentManager)
    }
}
