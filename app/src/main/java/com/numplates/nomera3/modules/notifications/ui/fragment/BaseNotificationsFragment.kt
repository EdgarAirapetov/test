package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.meera.core.extensions.stringNullable
import com.meera.core.utils.NSnackbar
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.IPushInfo
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityTransitFrom
import com.numplates.nomera3.modules.communities.ui.fragment.members.CommunityMembersContainerFragment
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.FeatureDeepLink
import com.numplates.nomera3.modules.notifications.ui.adapter.NotificationListAdapter
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions
import com.numplates.nomera3.modules.notifications.ui.fragment.NotificationDetailFragment.Companion.NOTIFICATION_ID
import com.numplates.nomera3.modules.notifications.ui.fragment.NotificationDetailFragment.Companion.NOTIFICATION_IS_READ
import com.numplates.nomera3.modules.notifications.ui.fragment.NotificationDetailFragment.Companion.NOTIFICATION_TYPE
import com.numplates.nomera3.modules.notifications.ui.itemdecorator.makeDefNotificationDivider
import com.numplates.nomera3.modules.notifications.ui.viewholder.NotificationViewHolder
import com.numplates.nomera3.modules.notifications.ui.basefragment.BaseNotificationFragmentEffect
import com.numplates.nomera3.modules.notifications.ui.basefragment.BaseNotificationViewModel
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListFragmentNew
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.FriendsHostFragmentNew
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.utils.SwipeToDeleteUtils
import com.numplates.nomera3.presentation.view.widgets.anim.UnblinkingItemAnimator
import kotlinx.coroutines.launch

private const val ANIMATION_DURATION = 100L
private const val DELAY_DELETE_NOTIFICATION_SEC = 4

abstract class BaseNotificationsFragment<T : ViewBinding> : BaseFragmentNew<T>() {

    private val viewModel by viewModels<BaseNotificationViewModel> { App.component.getViewModelFactory() }

    internal lateinit var pagingAdapter: NotificationListAdapter

    private var llManager: LinearLayoutManager? = null

    lateinit var swipeHandler: SwipeToDeleteUtils

    private var pendingDeleteSnackbar: NSnackbar? = null
    private var pendingDeleteAction: (() -> Unit)? = null

    abstract fun restoreNotificationIfNotNull(id: String)

    abstract fun deleteNotification(id: String, isGroup: Boolean)

    abstract fun markAsRead(id: String, isGroup: Boolean)

    abstract fun readAll()

    abstract fun deleteAll()

    abstract fun onItemDeleted()

    abstract fun onProfileClick(userId: Long)

    abstract fun logOpenCommunity()

    // notificationId / Notification
    private val deleteNotificationState = linkedMapOf<String, NotificationUiModel>()

    open fun getDividerDecorator(): RecyclerView.ItemDecoration? = context?.makeDefNotificationDivider()

    open fun getAbsoluteAdapterPosition(pagingAdapterPosition: Int) = pagingAdapterPosition

    open fun initRecycler(recyclerView: RecyclerView, refreshLayout: SwipeRefreshLayout? = null) {
        recyclerView.apply {
            setHasFixedSize(true)
            llManager = LinearLayoutManager(act, LinearLayoutManager.VERTICAL, false)
            layoutManager = llManager
            itemAnimator = UnblinkingItemAnimator()
                .apply {
                    changeDuration = ANIMATION_DURATION
                    addDuration = ANIMATION_DURATION
                    removeDuration = ANIMATION_DURATION
                    moveDuration = ANIMATION_DURATION
                }


            getDividerDecorator()?.let { decorator -> addItemDecoration(decorator) }

            val isMomentsEnabled = (activity?.application
                as? com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled
                ?: true

            pagingAdapter = NotificationListAdapter(
                notificationListener = clickNotification,
                separatorListener = clickSeparator,
                isMomentsEnabled = isMomentsEnabled
            )

            adapter = pagingAdapter
        }

        swipeHandler = SwipeToDeleteUtils(act, SwipeToDeleteUtils.SwipeType.FULL)
            .apply {
                onFullSwiped = { position ->
                    val absolutePosition = getAbsoluteAdapterPosition(position)
                    val holder = recyclerView.findViewHolderForAdapterPosition(absolutePosition)
                    if (holder is NotificationViewHolder) {
                        holder.makeDeleteContainerVisible()
                    }
                    val item = pagingAdapter.getItemForPosition(position)
                    item?.id?.let { pagingAdapter.setDeleting(item.id) }
                    item?.let { notification ->
                        deleteNotificationState[notification.id] = notification
                        handlePreviousDeletedNotifications(notification.id)
                        // Показ плашки
                        pendingDeleteSnackbar?.dismissNoCallbacks()
                        pendingDeleteAction?.invoke()
                        pendingDeleteAction = {
                            deleteNotificationState.clear()
                            swipeHandler.isSwipeEnable = true
                            notification.id.let { pagingAdapter.removeDeleting(item.id) }
                            actionDeleteNotification(notification)
                        }
                        pendingDeleteSnackbar = NSnackbar.with(view)
                            .inView(view)
                            .text(context.stringNullable(R.string.notification_is_deleting))
                            .description(context.stringNullable(R.string.touch_to_delete))
                            .durationIndefinite()
                            .button(context.stringNullable(R.string.general_cancel))
                            .dismissManualListener {
                                dismissSnackBar(notification)
                            }
                            .dismissSwipeListener {
                                dismissSnackBar(notification)
                            }
                            .timer(DELAY_DELETE_NOTIFICATION_SEC, pendingDeleteAction)
                            .show()
                    }
                }

                onSwipeProgress = { isSwipeNow ->
                    refreshLayout?.isEnabled = !isSwipeNow
                }
            }

        ItemTouchHelper(swipeHandler).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    private fun dismissSnackBar(notification: NotificationUiModel) {
        deleteNotificationState.clear()
        pendingDeleteSnackbar = null
        pendingDeleteAction = null
        swipeHandler.isSwipeEnable = true
        restoreNotification(notification)
    }

    private fun restoreNotification(notification: NotificationUiModel) {
        notification.id.let { pagingAdapter.removeDeleting(notification.id) }
        restoreNotificationIfNotNull(notification.id)
    }

    open fun onStartNotificationsFragment() = Unit

    open fun onStopNotificationsFragment() {
        if (::swipeHandler.isInitialized)
            swipeHandler.isSwipeEnable = true
        pendingDeleteSnackbar?.dismissNoCallbacks()
        pendingDeleteSnackbar = null
        pendingDeleteAction?.invoke()
        pendingDeleteAction = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationClickEffect.collect { event ->
                handleUiEffect(event)
            }
        }
    }

    private fun handleUiEffect(effect: BaseNotificationFragmentEffect) {
        when (effect) {
            is BaseNotificationFragmentEffect.OpenGroupChatFragment -> gotoGroupChat(effect.roomId)
        }
    }

    private fun handlePreviousDeletedNotifications(currentId: String) {
        if (deleteNotificationState.size > 1) {
            deleteNotificationState.forEach { (notificationId, notification) ->
                if (notificationId != currentId) {
                    actionDeleteNotification(notification)
                }
            }
        }
    }

    private fun actionDeleteNotification(notification: NotificationUiModel) {
        deleteNotification(notification.id, notification.isGroup)         // delete from Db
        onItemDeleted()                                                   // handle placeholder
    }

    private fun handleClickNotificationItem(action: NotificationTransitActions, isGroupedNotifications: Boolean) {
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
            is NotificationTransitActions.OnTransitToGroupChat -> viewModel.onGroupChatNotificationClicked(action.roomId)
            is NotificationTransitActions.OnUserAvatarClicked -> gotoUserProfile(action.user)
            is NotificationTransitActions.OnSystemNotification -> handleSystemNotification(action.uri)
            is NotificationTransitActions.OnTransitToPrivateGroupRequest -> goToPrivateGroup(action.groupId)
            is NotificationTransitActions.OnBirthdayFriendClicked ->
                goToUserBirthdayGifts(action.userId, action.userName, action.birthday)

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

        // mark as read when not a group
        if (!isGroupedNotifications) {
            action.notifId?.let { id ->
                if (action.isGroup == false) {
                    markAsRead(id, action.isGroup ?: false)
                }
            }
        }

        // If grouped screen
        if (isGroupedNotifications && action is NotificationTransitActions.OnTransitGroupNotificationsScreen) {
            gotoGroupDetail(action.notificationId, action.type, action.isRead)
        }
    }

    private fun openCommunityRoadFragment(communityId: Int) {
        add(
            CommunityRoadFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_GROUP_ID, communityId),
            Arg(IArgContainer.ARG_TRANSIT_COMMUNITY_FROM, CommunityTransitFrom.NOTIFICATIONS.key)
        )
        logOpenCommunity()
    }

    private fun goToPrivateGroup(groupId: Long?) {
        groupId?.let {
            add(
                CommunityMembersContainerFragment(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_GROUP_ID, it.toInt()),
                Arg(IArgContainer.ARG_IS_FROM_PUSH, true)
            )
        }
    }

    open fun goToCreateAvatar() = Unit

    private fun goToUserBirthdayGifts(userId: Long?, userName: String?, dateOfBirth: Long) {
        userId?.let {
            add(
                GiftsListFragmentNew(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_USER_ID, it),
                Arg(IArgContainer.ARG_USER_NAME, userName),
                Arg(IArgContainer.ARG_SCROLL_TO_BOTTOM, true),
                Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth),
                Arg(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
            )
        }
    }

    private fun handleSystemNotification(uri: String?) {
        val deeplinkWithOrigin = uri?.let { FeatureDeepLink.addDeeplinkOrigin(it, DeeplinkOrigin.NOTIFICATIONS) }
        act.openLink(deeplinkWithOrigin)
    }

    private fun handleClickUserSoftBloc(action: NotificationTransitActions.OnUserSoftBlockClicked) {
        if (action.isBlocked) {
            showBlockUserDialog(action.blockReason, action.blockedTo)
        } else {
            act.goToMainRoad()
        }
    }

    private fun showBlockUserDialog(reason: String, expiredAt: Long) {
        val dialog = BlockUserByAdminDialogFragment()
        dialog.blockReason = reason
        dialog.blockDateUnixtimeSec = expiredAt
        dialog.closeDialogClickListener = { dialog.dismiss() }
        dialog.writeSupportClickListener = {
            act?.navigateToTechSupport()
        }
        dialog.headerDialogType = HeaderDialogType.MainRoadType
        dialog.show(childFragmentManager, "blocked_user_dialog")
    }

    private val clickNotification: (
        NotificationTransitActions,
        isGroupedNotifications: Boolean
    ) -> Unit = { notification, isGrouped -> handleClickNotificationItem(notification, isGrouped) }

    private val clickSeparator: (InfoSection) -> Unit = {
        when (it.action) {
            ACTION_TYPE_DELETE_ALL -> showConfirmDeleteDialog { deleteAll() }
            ACTION_TYPE_READ_ALL -> readAll()
        }
    }

    private fun gotoUserGifts(userId: Long?, userName: String?) {
        if (userId == null) {
            add(
                UserGiftsFragment(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_IS_WORTH_TO_SHOW_DIALOG, true),
                Arg(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
            )
        } else {
            add(
                GiftsListFragmentNew(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_USER_NAME, userName),
                Arg(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
            )
        }
    }

    private fun gotoMoment(
        userId: Long,
        momentId: Long? = null,
        commentId: Long? = null,
        openMomentAnimationView: View? = null,
        pushInfo: String? = null,
        latestReactionType: ReactionType? = null,
        hasNewMoments: Boolean?
    ) {
        act.openUserMoments(
            userId = userId,
            fromView = openMomentAnimationView,
            targetMomentId = momentId,
            lastReactionType = latestReactionType,
            pushInfo = pushInfo,
            commentID = commentId,
            openedWhere = AmplitudePropertyMomentScreenOpenWhere.NOTIFICATIONS,
            viewedEarly = hasNewMoments?.not()
        )
    }

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
        add(
            PostFragmentV2(null),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_FEED_POST_ID, postId),
            Arg(IArgContainer.ARG_POST_ORIGIN, postOriginType),
            Arg(IArgContainer.ARG_POST_LATEST_REACTION_TYPE, latestReactionType)
        )
    }

    private fun gotoPostWithComment(postId: Long?, commentId: Long?) {
        add(
            PostFragmentV2(null),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_FEED_POST_ID, postId),
            Arg(IArgContainer.ARG_COMMENT_ID, commentId),
            Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
        )
    }

    private fun gotoPostWithCommentWithReactions(
        postId: Long?,
        commentId: Long?,
        latestReactionType: ReactionType?
    ) {
        add(
            PostFragmentV2(null),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_FEED_POST_ID, postId),
            Arg(IArgContainer.ARG_COMMENT_ID, commentId),
            Arg(IArgContainer.ARG_COMMENT_LAST_REACTION, latestReactionType),
            Arg(IArgContainer.ARG_POST_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
        )
    }

    private fun gotoProfileView(postId: Long?) {
        add(
            ProfilePhotoViewerFragment(), Act.COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
            Arg(IArgContainer.ARG_IS_OWN_PROFILE, true),
            Arg(IArgContainer.ARG_POST_ID, postId),
            Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.NOTIFICATIONS)
        )
    }

    private fun gotoFriendsIncoming() {
        add(
            FriendsHostFragmentNew(), Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_IS_GOTO_INCOMING, true),
            Arg(
                IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM,
                FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_NOTIFICATIONS
            )
        )
    }

    private fun gotoUserProfile(user: User?) {
        val userId = user?.userId?.toLong() ?: 0L
        onProfileClick(userId)
        add(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, userId),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.NOTIFICATION.property)
        )
    }

    private fun gotoGroupChat(roomId: Long?) {
        roomId?.let { id ->
            act.addFragment(
                ChatFragmentNew(),
                Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.NOTIFICATIONS),
                Arg(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_LIST_ROOMS,
                        roomId = roomId
                    )
                )
            )
        }
    }

    private fun goToUserChat(user: User?) {
        viewModel.cacheUserProfileForChat(user)
        act.addFragment(
            ChatFragmentNew(), Act.LIGHT_STATUSBAR,
            Arg(
                IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                    initType = ChatInitType.FROM_PROFILE,
                    userId = user?.userId?.toLong()
                )
            )
        )
    }

    private fun goToEventView(postId: Long) {
        act.addFragment(
            fragment = MapFragment(),
            isLightStatusBar = Act.LIGHT_STATUSBAR,
            args = arrayOf(
                Arg(MapFragment.ARG_EVENT_POST_ID, postId),
                Arg(MapFragment.ARG_LOG_MAP_OPEN_WHERE, AmplitudePropertyWhere.OTHER)
            )
        )
    }

    private fun gotoGroupDetail(notificationId: String, type: String, isRead: Boolean) {
        onStopNotificationsFragment()
        add(
            NotificationDetailFragment(), Act.LIGHT_STATUSBAR,
            Arg(NOTIFICATION_ID, notificationId),
            Arg(NOTIFICATION_TYPE, type),
            Arg(NOTIFICATION_IS_READ, isRead)
        )
    }

    protected fun showConfirmDeleteDialog(onConfirmDeletion: () -> Unit) {
        ConfirmDialogBuilder()
            .setHeader(context.stringNullable(R.string.delete_all_notifications))
            .setDescription(context.stringNullable(R.string.notifications_list_will_be_clean))
            .setLeftBtnText(context.stringNullable(R.string.general_cancel))
            .setRightBtnText(context.stringNullable(R.string.general_delete))
            .setRightClickListener { onConfirmDeletion.invoke() }
            .show(childFragmentManager)
    }

}
