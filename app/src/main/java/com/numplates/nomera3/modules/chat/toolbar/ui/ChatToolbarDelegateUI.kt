package com.numplates.nomera3.modules.chat.toolbar.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.fadeIn
import com.meera.core.extensions.fadeOut
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.utils.isBirthdayToday
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.dialog.userRole
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.databinding.ChatToolbarV2Binding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.NetworkChatStatus
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ToolbarEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.AVATAR_GLIDE_TRANSITIONS_DURATION_MS
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MENU_ANIMATE_DURATION_MS
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MENU_CLOSE_ANIMATE_ROTATION_ANGLE
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MENU_OPEN_ANIMATE_ROTATION_ANGLE
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MIN_MENU_HEIGHT
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ONLINE_STATUS_DURATION_MS
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ToolbarEntityMapper
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ToolbarInteractionCallback
import com.numplates.nomera3.modules.chat.toolbar.ui.viewmodel.ChatToolbarViewModelDelegate
import com.numplates.nomera3.modules.chat.toolbar.ui.viewstate.ChatToolbarViewState
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.ui.customView.CallSwitchView
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_ALLOWED
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_LOCKED
import com.numplates.nomera3.presentation.view.widgets.VipView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

typealias NavSwipeDirection = NavigatorViewPager.SwipeDirection

class ChatToolbarDelegateUI(
    private val fragment: Fragment,
    private val binding: ChatToolbarV2Binding?,
    private val networkStatusProvider: NetworkStatusProvider,
    private val viewModelDelegate: ChatToolbarViewModelDelegate,
    private val actionCallback: ToolbarInteractionCallback
) : ChatToolbarDelegate, LifecycleObserver {

    private val toolbarEntityMapper = ToolbarEntityMapper(fragment.requireContext())
    private var isSubscribeDisabled = false

    init {
        viewModelDelegate.observeMoments()
    }

    override fun handleAction(action: ChatToolbarActionsUI) {
        Timber.d("CHAT_TOOLBAR_DELEGATE UIAction:$action")
        when(action) {
            is ChatToolbarActionsUI.SetupToolbar -> updateToolbar(action.entryData)
            is ChatToolbarActionsUI.SetAvailabilityGroupMenuAbout -> setAvailabilityGroupMenuAbout(action.isEnabled)
            is ChatToolbarActionsUI.ChangeChatRequestMenuVisibility -> changeChatRequestMenuVisibility(action.isVisible)
            is ChatToolbarActionsUI.ChangeChatRequestMenuStatus -> changeChatRequestMenuStatus(
                isChatRequest = action.isChatRequest,
                isGroupChat = action.isGroupChat,
                isDialogAllowed = action.isDialogAllowed,
                isSubscribed = action.isSubscribed,
                isBlocked = action.isBlocked,
                hasConversationStarted = action.hasConversationStarted
            )
            ChatToolbarActionsUI.CloseToolbarMenu -> closeToolbarMenu()
            is ChatToolbarActionsUI.UpdateCompanion -> updateCompanion(action.roomId, action.userId)
        }
    }

    private fun updateToolbar(
        entryData: ChatEntryData
    ) {
        this.isSubscribeDisabled = entryData.wasSubscriptionDismissedEarlier ||
            entryData.companion?.userRole == UserRole.ANNOUNCE_USER ||
            entryData.companion?.userRole == UserRole.SUPPORT_USER
        setupToolbarMenu(entryData)
        renderToolbarUI(
            entryData = entryData,
            toolbar = toolbarEntityMapper.map(entryData)
        )
        viewModelDelegate.observeOnlineTypingStates(entryData)
        updateChatToolbarFromNetwork(entryData)
        handleViewEvent(entryData)
    }

    @Deprecated("Some changes made for back support but still bugged. Meera version of this class is correct.")
    private fun handleViewEvent(entryData: ChatEntryData) {
        viewModelDelegate.viewEvent
            .flowWithLifecycle(fragment.viewLifecycleOwner.lifecycle)
            .onEach { state ->
                when (state) {
                    is ChatToolbarViewState.OnUpdateData -> redrawToolbarDataWhenUpdated(entryData, state.toolbar)
                    is ChatToolbarViewState.OnHideStatus -> hideToolbarOnlineStatus()
                    is ChatToolbarViewState.OnUpdateOnlineStatus -> renderToolbarOnlineStatus(state)
                    is ChatToolbarViewState.OnUpdateTyping -> renderToolbarTypingStatus(state)
                    is ChatToolbarViewState.OnUpdateNotificationStatus -> redrawNotificationSoundIcon(
                        entryData,
                        state.isSetNotifications
                    )

                    is ChatToolbarViewState.OnErrorUpdateNotificationStatus -> actionCallback.errorWhenUpdatedNotification()
                    is ChatToolbarViewState.OnUpdateChatInputState -> actionCallback.updateChatInputEnabled(state.chatEnabled)
                    is ChatToolbarViewState.OnUpdateAvatarMomentsState ->
                        updateCurrentVipView(
                            binding,
                            hasMoments = state.hasMoments,
                            hasNewMoments = state.hasNewMoments
                        )
                }
            }
            .launchIn(fragment.viewLifecycleOwner.lifecycleScope)
    }

    private fun updateChatToolbarFromNetwork(entryData: ChatEntryData) {
        initConnectionHandler { isOnline ->
            if (isOnline) {
                viewModelDelegate.socketConnectionHandler(entryData)
            } else {
                Timber.e("Unable update toolbar data. App is offline")
            }
        }
    }

    private fun setAvailabilityGroupMenuAbout(isEnabled: Boolean = true) {
        binding?.llGroupMenuAbout?.isEnabled = isEnabled
        binding?.ivToolbarAvatar?.isEnabled = isEnabled
    }

    private fun renderToolbarUI(entryData: ChatEntryData, toolbar: ToolbarEntity) {
        binding?.apply {
            btnBack.click { actionCallback.onClickMenuBackArrow() }

            when {
                entryData.companion?.userRole == UserRole.ANNOUNCE_USER || entryData.companion?.userRole == UserRole.SUPPORT_USER -> {
                    setToolbarDialogAvatar(ivToolbarAvatar, toolbar.avatar)
                    ivToolbarAvatar.click { actionCallback.onClickDialogAvatar(entryData.companion) }
                    vgDialogMenuProfile.click { actionCallback.onClickDialogAvatar(entryData.companion) }
                    vgDialogMenuMore.gone()
                    toolbarCallSwitch.gone()
                    actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                    showCheckmark(toolbar)
                }
                entryData.roomType == ChatRoomType.DIALOG -> {
                    setToolbarAvatarWithMoments(binding = this, toolbar = toolbar)
                    handleClickDialogAvatar(
                        binding = this,
                        user = entryData.companion,
                        hasMoments = hasUserMoments(toolbar),
                        hasNewMoments = hasUserNewMoments(toolbar)
                    )
                    vgDialogMenuProfile.click { actionCallback.onClickDialogAvatar(entryData.companion) }
                    if (entryData.room?.approved.toBoolean()) {
                        vgDialogMenuMore.click { actionCallback.onClickDialogMoreItem() }
                    } else {
                        vgDialogMenuMore.click { actionCallback.onBlockChatRequestClicked() }
                    }
                    setupCallSwitchForDialogChat(toolbarCallSwitch, entryData.companion)
                    if (isBirthdayToday(entryData.companion?.birthDate)) {
                        showAvatarRoundBadge(R.drawable.ic_birthday_fg, false)
                    } else {
                        hideAvatarRoundBadge()
                    }
                    actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                    showCheckmark(toolbar)
                }
                entryData.roomType == ChatRoomType.GROUP -> {
                    setToolbarGroupAvatar(ivToolbarAvatar, toolbar.avatar)
                    toolbarCallSwitch.gone()
                    val roomId = entryData.roomId
                    ivToolbarAvatar.click { handleToolbarMenuOpen(entryData) }
                    llGroupMenuAbout.click { actionCallback.onClickMenuGroupChatDetail(roomId) }
                    llGroupMenuMore.click { actionCallback.onClickMenuGroupChatMore(roomId) }
                    showAvatarRoundBadge(R.drawable.ic_group_fg, true)
                    actionCallback.setChatBackground(null, RoomType.GROUP)
                }
            }

            tvToolbarTitle.text = toolbar.title
            titleContainer.click { handleToolbarMenuOpen(entryData) }
            llContainerChatTitleSubtitle.click { handleToolbarMenuOpen(entryData) }
            entryData.room?.let { handleChatMenuRequest(it) }
        }
    }

    private fun handleClickDialogAvatar(
        binding: ChatToolbarV2Binding,
        user: UserChat?,
        hasMoments: Boolean,
        hasNewMoments: Boolean = false
    ) {
        if (hasMoments) {
            clickAvatar(binding.vvToolbarAvatar,
                user,
                hasMoments = true,
                hasNewMoments = hasNewMoments)
        } else {
            clickAvatar(
                binding.ivToolbarAvatar,
                user,
                hasMoments = false,
                hasNewMoments = false
            )
        }
    }

    private fun clickAvatar(
        view: View,
        user: UserChat?,
        hasMoments: Boolean,
        hasNewMoments: Boolean
    ) {
        view.click {
            actionCallback.onClickDialogAvatar(
                userChat = user,
                hasMoments = hasMoments,
                hasNewMoments = hasNewMoments,
                view = view
            )
        }
    }

    private fun redrawToolbarDataWhenUpdated(
        entryData: ChatEntryData,
        toolbar: ToolbarEntity
    ) {
        binding?.apply {
            when {
                entryData.companion?.userRole == UserRole.ANNOUNCE_USER || entryData.companion?.userRole == UserRole.SUPPORT_USER -> {
                    setToolbarDialogAvatar(ivToolbarAvatar, toolbar.avatar)
                    ivToolbarAvatar.click { actionCallback.onClickDialogAvatar(entryData.companion) }
                    vgDialogMenuProfile.click { actionCallback.onClickDialogAvatar(entryData.companion) }
                    vgDialogMenuMore.gone()
                    toolbarCallSwitch.gone()
                    actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                    actionCallback.updatedChatData(toolbar.updatedChatData, ChatRoomType.DIALOG)
                    showCheckmark(toolbar)
                }
                entryData.roomType == ChatRoomType.DIALOG -> {
                    setToolbarAvatarWithMoments(binding = this, toolbar = toolbar)
                    tvToolbarTitle.text = toolbar.title
                    showCheckmark(toolbar)
                    redrawCallSwitch(toolbarCallSwitch, toolbar)
                    actionCallback.setChatBackground(
                        toolbar.updatedChatData?.companion,
                        RoomType.REGULAR
                    )
                    actionCallback?.updatedChatData(toolbar.updatedChatData, ChatRoomType.DIALOG)
                    entryData.room?.let { handleChatMenuRequest(it) }
                }
                entryData.roomType == ChatRoomType.GROUP -> {
                    actionCallback?.updatedChatData(null, ChatRoomType.GROUP)
                }
            }
        }
    }

    private fun showCheckmark(toolbar: ToolbarEntity) {
        val accountType = createAccountTypeEnum(toolbar.accountType)
        val isVip = accountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM
            || accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP
        val iconEnabled = toolbar.approved
        binding?.tvToolbarTitle?.enableApprovedIcon(
            enabled = iconEnabled,
            isVip = isVip,
            padding = 2.dp
        )
    }

    private fun ChatToolbarV2Binding.showAvatarRoundBadge(
        @DrawableRes imageIcon: Int,
        isShowBackgroundGradient: Boolean
    ) {
        cvToolbarIconBadge.visible()
        if (isShowBackgroundGradient) ivToolbarGradientBg.visible() else ivToolbarGradientBg.gone()
        ivToolbarIconImage.setImageResource(imageIcon)
    }

    private fun ChatToolbarV2Binding.hideAvatarRoundBadge() {
        cvToolbarIconBadge.gone()
        ivToolbarGradientBg.gone()
        ivToolbarIconImage.setImageDrawable(null)
    }

    private fun setupCallSwitchForDialogChat(callSwitch: CallSwitchView, companion: UserChat?) {
        callSwitch.visible()
        companion?.settingsFlags?.let { settings ->
            if (companion.blacklistedByMe == 1 || companion.blacklistedMe == 1) {
                callSwitch.state = SWITCH_CALL_STATE_LOCKED
            } else {
                if (settings.iCanCall == 1) {
                    callSwitch.state = SWITCH_CALL_STATE_ALLOWED
                } else {
                    callSwitch.state = SWITCH_CALL_STATE_LOCKED
                }
            }

            if (settings.userCanCallMe == 1) {
                callSwitch.setChecked(true)
            } else {
                callSwitch.setChecked(false)
            }
            callSwitch.onClick = { state, isChecked ->
                if (state == SWITCH_CALL_STATE_ALLOWED && isChecked) {
                    actionCallback.startCallWithCompanion(companion)
                    fragment.requireContext().vibrate()
                } else if (state == SWITCH_CALL_STATE_LOCKED && isChecked) {
                    actionCallback.showCallNotAllowedTooltip()
                }
            }
            callSwitch.onCheckedChangeListener = { _, isChecked ->
                viewModelDelegate.setCallPrivacyForUser(companion.userId, isChecked)
            }
            callSwitch.shouldInterceptTouch = { isShould ->
                if (isShould) {
                    actionCallback.allowSwipeDirectionNavigator(NavSwipeDirection.NONE)
                } else {
                    actionCallback.allowSwipeDirectionNavigator(NavSwipeDirection.LEFT)
                }
            }

            val isCallToggleVisible = true
            val isMeAvailableForCalls = if (settings.userCanCallMe == 1 && settings.iCanCall == 1) {
                SWITCH_CALL_STATE_ALLOWED
            } else {
                SWITCH_CALL_STATE_LOCKED
            }
            actionCallback.setCallVariableSettings(isCallToggleVisible, isMeAvailableForCalls)
        }
    }

    private fun redrawCallSwitch(callSwitch: CallSwitchView, toolbar: ToolbarEntity) {
        val state = toolbar.callSwitchState

        if (state?.blacklistedByMe == 1 || state?.blacklistedMe == 1) {
            callSwitch.state = SWITCH_CALL_STATE_LOCKED
        } else {
            if (state?.iCanCall == 1) {
                callSwitch.state = SWITCH_CALL_STATE_ALLOWED
            } else {
                callSwitch.state = SWITCH_CALL_STATE_LOCKED
            }
        }

        if (state?.userCanCallMe == 1) {
            callSwitch.setChecked(true)
        } else {
            callSwitch.setChecked(false)
        }

        val isCallToggleVisible = true
        val isMeAvailableForCalls = if (state?.userCanCallMe == 1 && state.iCanCall == 1) {
            SWITCH_CALL_STATE_ALLOWED
        } else {
            SWITCH_CALL_STATE_LOCKED
        }
        actionCallback.setCallVariableSettings(isCallToggleVisible, isMeAvailableForCalls)
    }

    private fun renderToolbarOnlineStatus(state: ChatToolbarViewState.OnUpdateOnlineStatus) {
        if (state.status.isShowStatus) {
            binding?.apply {
                toolbarStatusContainer.visible()
                if (state.status.isShowDotIndicator) {
                    ivToolbarStatusIndicator.visible()
                    setColorOnlineStatusDotIndicator(state.status.networkStatus)
                } else {
                    ivToolbarStatusIndicator.gone()
                }

                tvToolbarStatus.text = state.status.message
            }
        }
    }

    private fun setColorOnlineStatusDotIndicator(status: NetworkChatStatus) {
        if (status == NetworkChatStatus.ONLINE) {
            binding?.ivToolbarStatusIndicator?.loadGlide(R.drawable.green_dot_shape)
        } else {
            binding?.ivToolbarStatusIndicator?.loadGlide(R.drawable.grey_dot_shape)
        }
    }

    private fun renderToolbarTypingStatus(state: ChatToolbarViewState.OnUpdateTyping) {
        binding?.apply {
            toolbarStatusContainer.visible()
            ivToolbarStatusIndicator.gone()
            tvToolbarStatus.fadeOut(ONLINE_STATUS_DURATION_MS)
            tvToolbarStatus.text = state.typingText
            tvToolbarStatus.fadeIn(ONLINE_STATUS_DURATION_MS)
        }
    }

    private fun setToolbarAvatarWithMoments(binding: ChatToolbarV2Binding, toolbar: ToolbarEntity) {
        if (hasUserMoments(toolbar)) {
            binding.ivToolbarAvatar.gone()
            setToolbarVipViewAvatar(
                ctx = fragment.requireContext(),
                vipView = binding.vvToolbarAvatar,
                toolbar = toolbar
            )
        } else {
            setToolbarDialogAvatar(binding.ivToolbarAvatar, toolbar.avatar)
        }
    }

    private fun hasUserMoments(toolbar: ToolbarEntity): Boolean {
        val m = toolbar.moments
        return m != null && m.hasMoments
    }

    private fun hasUserNewMoments(toolbar: ToolbarEntity): Boolean {
        val m = toolbar.moments
        return m != null && m.hasNewMoments
    }

    private fun setToolbarVipViewAvatar(ctx: Context, vipView: VipView, toolbar: ToolbarEntity) {
        vipView.visible()
        vipView.setUp(
            context = ctx,
            avatarLink = toolbar.avatar,
            accountType = null,
            frameColor = 0,
            hasShadow = false,
            hasMoments =  toolbar.moments?.hasMoments ?: false,
            hasNewMoments = toolbar.moments?.hasNewMoments ?: false
        )
    }

    private fun updateCurrentVipView(
        binding: ChatToolbarV2Binding?,
        hasMoments: Boolean,
        hasNewMoments: Boolean
    ) {
        val currentUrl = binding?.vvToolbarAvatar?.currentAvatarLink as? String
        currentUrl?.let { url ->
            binding.vvToolbarAvatar.setUp(
                context = fragment.requireContext(),
                avatarLink = url,
                accountType = null,
                frameColor = 0,
                hasShadow = false,
                hasMoments =  hasMoments,
                hasNewMoments = hasNewMoments
            )
        }
    }

    private fun setToolbarDialogAvatar(imageView: ImageView, url: String) {
        imageView.visible()
        loadAvatarByUrl(imageView, url, R.drawable.fill_8_round)
    }

    private fun setToolbarGroupAvatar(imageView: ImageView, url: String) {
        imageView.visible()
        loadAvatarByUrl(imageView, url, R.drawable.group_chat_avatar_circle)
    }

    private fun getGlideRequestOptions(@DrawableRes placeholder: Int) = RequestOptions
        .circleCropTransform()
        .placeholder(placeholder)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

    private val glideTransitions = DrawableTransitionOptions
        .withCrossFade(AVATAR_GLIDE_TRANSITIONS_DURATION_MS)

    private fun loadAvatarByUrl(
        imageView: ImageView,
        url: String,
        @DrawableRes placeholder: Int
    ) {
        val context = fragment.requireContext().applicationContext
        if (isValidContextForGlide(context)) {
            Glide.with(context)
                .load(url)
                .apply(getGlideRequestOptions(placeholder))
                .transition(glideTransitions)
                .into(imageView)
        }
    }

    private fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) return false
        if (context is Activity) {
            if (context.isDestroyed || context.isFinishing) {
                return false
            }
        }
        return true
    }

    private fun hideToolbarOnlineStatus() {
        binding?.toolbarStatusContainer?.gone()
    }

    private fun changeChatRequestMenuVisibility(isVisible: Boolean) {
        binding?.menuChatRequest?.isVisible = isVisible
    }

    private fun handleChatMenuRequest(room: DialogEntity?) {
        room?.let {
            val isChatRequest = room.approved == DialogApproved.NOT_DEFINED.key
            if (isChatRequest) {
                binding?.apply {
                    tvForbidSendMessage.click { actionCallback.onBlockChatRequestClicked() }
                    tvAllowSendMessage.click { actionCallback.allowSendMessageChatRequest() }
                }
                actionCallback.chatRequestStatus(isRoomChatRequest = true)
            } else {
                if (room.approved == DialogApproved.ALLOW.key) {
                    val isGroupChat = room.type == ROOM_TYPE_GROUP
                    val isSubscribed = room.companion.settingsFlags?.subscription_on.toBoolean()
                    val isBlockedByMe = room.companion.blacklistedByMe.toBoolean()
                    val blockedMe = room.companion.blacklistedMe.toBoolean()
                    setupSubscribeBlock(
                        isChatRequest = room.isChatRequest(),
                        isGroupChat = isGroupChat,
                        isSubscribed = isSubscribed,
                        isBlocked = isBlockedByMe || blockedMe,
                        hasConversationStarted = room.lastMessage != null
                    )
                }
                actionCallback.chatRequestStatus(isRoomChatRequest = false)
            }
        }
    }

    private fun changeChatRequestMenuStatus(
        isChatRequest: Boolean,
        isGroupChat: Boolean,
        isDialogAllowed: Boolean,
        isSubscribed: Boolean,
        isBlocked: Boolean,
        hasConversationStarted: Boolean,
    ) {
        if (isDialogAllowed) {
            setupSubscribeBlock(
                isChatRequest = isChatRequest,
                isGroupChat = isGroupChat,
                isSubscribed = isSubscribed,
                isBlocked = isBlocked,
                hasConversationStarted = hasConversationStarted,
            )
        }
    }

    private fun setupSubscribeBlock(
        isChatRequest: Boolean,
        isGroupChat: Boolean,
        isSubscribed: Boolean,
        isBlocked: Boolean,
        hasConversationStarted: Boolean,
    ) {
        if (isChatRequest || isSubscribed || isBlocked ||
            isGroupChat || !hasConversationStarted || isSubscribeDisabled) {
            binding?.vgSubscribe?.gone()
        } else {
            binding?.vgSubscribe?.visible()
            binding?.tvSubscribe?.click {
                actionCallback.subscribeToUserClicked()
                binding.vgSubscribe.gone()
            }
            binding?.btnCloseSubscribe?.click {
                actionCallback.dismissSubscriptionClicked()
                binding.vgSubscribe.gone()
            }
        }
    }

    private fun handleToolbarMenuOpen(entryData: ChatEntryData) {
        val menu = when (entryData.roomType) {
            ChatRoomType.DIALOG -> binding?.dialogMenu
            ChatRoomType.GROUP -> binding?.toolbarGroupMenu
        }

        menu?.let {
            if (menu.height <= MIN_MENU_HEIGHT.dp) openToolbarMenu(it) else closeToolbarMenu(it)
        }
    }

    private fun openToolbarMenu(menu: ViewGroup) {
        menu.visible()
        menu.measure(MATCH_PARENT, WRAP_CONTENT)
        menu.animateHeight(menu.measuredHeight, MENU_ANIMATE_DURATION_MS)
        binding?.toolbarSeparator?.visible()
        binding?.ivToolbarArrowMenu
            ?.animate()
            ?.rotation(MENU_OPEN_ANIMATE_ROTATION_ANGLE)
            ?.setDuration(MENU_ANIMATE_DURATION_MS)
            ?.start()
        fragment.requireContext().vibrate()
    }

    private fun closeToolbarMenu(menu: ViewGroup) {
        menu.visible()
        menu.animateHeight(0, MENU_ANIMATE_DURATION_MS) {
            binding?.toolbarSeparator?.gone()
        }
        binding?.ivToolbarArrowMenu
            ?.animate()
            ?.rotation(MENU_CLOSE_ANIMATE_ROTATION_ANGLE)
            ?.setDuration(MENU_ANIMATE_DURATION_MS)
            ?.start()
    }

    private fun closeToolbarMenu() {
        binding?.apply {
            if (dialogMenu.isVisible) closeToolbarMenu(dialogMenu)
            if (toolbarGroupMenu.isVisible) closeToolbarMenu(toolbarGroupMenu)
        }
    }

    private fun setupToolbarMenu(entryData: ChatEntryData) {
        when (entryData.roomType) {
            ChatRoomType.DIALOG -> {
                val isMuted = entryData.companion?.settingsFlags?.notificationsOff == 1
                binding?.ivDialogMenuSound?.let { imageView ->
                    setupNotificationSoundIcon(imageView, isMuted)
                    binding.vgDialogMenuSound.click { clickDialogNotificationSoundIcon(isMuted, entryData) }
                }

            }
            ChatRoomType.GROUP -> {
                val isMuted = entryData.room?.isMuted ?: false
                binding?.ivGroupMenuSound?.let { imageView ->
                    setupNotificationSoundIcon(imageView, isMuted)
                    binding.llGroupMenuSound.click {
                        clickGroupNotificationSoundIcon(
                            isMuted,
                            entryData
                        )
                    }
                }
            }
        }
    }

    private fun setupNotificationSoundIcon(imageView: ImageView?, isMuted: Boolean) {
        if (isMuted) {
            imageView?.setImageDrawable(fragment, R.drawable.icons_notification_no_sound)
        } else {
            imageView?.setImageDrawable(fragment, R.drawable.icons_notification)
        }
    }

    private fun redrawNotificationSoundIcon(
        entryData: ChatEntryData,
        isSetNotifications: Boolean
    ) {
        when (entryData.roomType) {
            ChatRoomType.DIALOG -> {
                entryData.companion?.settingsFlags?.notificationsOff = isSetNotifications.toInt()
                setupToolbarMenu(
                    entryData.copy(
                        companion = entryData.companion
                    )
                )
                setupNotificationSoundIcon(binding?.ivDialogMenuSound, isSetNotifications)
                showToastAboutNotificationStatus(isSetNotifications)
            }
            ChatRoomType.GROUP -> {
                entryData.room?.isMuted = isSetNotifications
                setupToolbarMenu(
                    entryData.copy(
                        room = entryData.room
                    )
                )
                setupNotificationSoundIcon(binding?.ivGroupMenuSound, isSetNotifications)
                showToastAboutNotificationStatus(isSetNotifications)
            }
        }
    }

    private fun showToastAboutNotificationStatus(isMuted: Boolean) {
        if (isMuted) {
            actionCallback.showDisableNotificationsMessage()
        } else {
            actionCallback.showEnableNotificationsMessage()
        }
    }

    private fun clickDialogNotificationSoundIcon(isMuted: Boolean, entryData: ChatEntryData) {
        if (isMuted) {
            viewModelDelegate.updateDialogNotificationStatus(
                isSetPrivacyNotifications = false,
                entryData = entryData
            )
        } else {
            viewModelDelegate.updateDialogNotificationStatus(
                isSetPrivacyNotifications = true,
                entryData = entryData
            )
        }
    }

    private fun clickGroupNotificationSoundIcon(isMuted: Boolean, entryData: ChatEntryData) {
        viewModelDelegate.updateGroupNotificationStatus(isMuted, entryData)
    }

    private fun updateCompanion(roomId: Long, userId: Long) {
        viewModelDelegate.updateCompanion(roomId, userId)
    }

    private fun initConnectionHandler(isOnline: (Boolean) -> Unit) {
        networkStatusProvider.getNetworkStatusLiveData()
            .observe(fragment.viewLifecycleOwner) { networkStatus ->
                if (networkStatus.isConnected) {
                    isOnline.invoke(true)
                } else {
                    isOnline.invoke(false)
                }
            }
    }

}

fun DialogEntity?.isChatRequest(): Boolean = this?.approved == DialogApproved.NOT_DEFINED.key
