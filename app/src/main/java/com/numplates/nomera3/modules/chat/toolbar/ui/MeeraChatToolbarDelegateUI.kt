package com.numplates.nomera3.modules.chat.toolbar.ui

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.fadeIn
import com.meera.core.extensions.fadeOut
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.dialog.userRole
import com.meera.db.models.userprofile.UserRole
import com.meera.uikit.widgets.UiKitCallSwitchView
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.databinding.MeeraChatToolbarBinding
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatOnlineStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.NetworkChatStatus
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ToolbarEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MENU_ANIMATE_DURATION_MS
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MENU_ANIMATE_HEIGHT
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.MIN_MENU_HEIGHT
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ONLINE_STATUS_DURATION_MS
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ToolbarEntityMapper
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ToolbarInteractionCallback
import com.numplates.nomera3.modules.chat.toolbar.ui.viewmodel.ChatToolbarViewModelDelegate
import com.numplates.nomera3.modules.chat.toolbar.ui.viewstate.ChatToolbarViewState
import com.numplates.nomera3.modules.chat.views.MeeraChatToolbarRequestClickType
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_ALLOWED
import com.numplates.nomera3.presentation.view.ui.customView.SWITCH_CALL_STATE_LOCKED
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val CALL_SWITCH_SET_CHECKED_DELAY = 500L

class MeeraChatToolbarDelegateUI(
    private val fragment: Fragment,
    private val binding: MeeraChatToolbarBinding,
    private val networkStatusProvider: NetworkStatusProvider,
    private val viewModelDelegate: ChatToolbarViewModelDelegate,
    private val actionCallback: ToolbarInteractionCallback
) : ChatToolbarDelegate {

    private val toolbarEntityMapper = ToolbarEntityMapper(fragment.requireContext())
    private var isSubscribeDisabled = false

    init {
        viewModelDelegate.observeMoments()
        observeViewEvents()
    }

    override fun handleAction(action: ChatToolbarActionsUI) {
        when (action) {
            is ChatToolbarActionsUI.SetupToolbar -> setupToolbar(action.entryData)
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

    private fun observeViewEvents() {
        viewModelDelegate.viewEvent
            .flowWithLifecycle(fragment.viewLifecycleOwner.lifecycle)
            .onEach(::handleViewEvent)
            .launchIn(fragment.viewLifecycleOwner.lifecycleScope)
    }

    /**
     * Первоначальная инициализация тулбара
     */
    private fun setupToolbar(
        entryData: ChatEntryData
    ) {
        isSubscribeDisabled = entryData.wasSubscriptionDismissedEarlier
            || entryData.companion?.userRole == UserRole.ANNOUNCE_USER
            || entryData.companion?.userRole == UserRole.SUPPORT_USER
        setupToolbarMenu(entryData)
        setupToolbarUI(
            entryData = entryData,
            toolbar = toolbarEntityMapper.map(entryData)
        )
        viewModelDelegate.observeOnlineTypingStates(entryData)
        updateChatToolbarFromNetwork(entryData)
    }

    private fun handleViewEvent(state: ChatToolbarViewState) {
        when (state) {
            is ChatToolbarViewState.OnUpdateData -> redrawToolbarWhenUpdated(
                entryData = state.entryData,
                toolbar = state.toolbar
            )

            is ChatToolbarViewState.OnHideStatus -> hideToolbarOnlineStatus()
            is ChatToolbarViewState.OnUpdateOnlineStatus -> renderToolbarOnlineStatus(state.status)
            is ChatToolbarViewState.OnUpdateTyping -> renderToolbarTypingStatus(state)
            is ChatToolbarViewState.OnUpdateNotificationStatus -> redrawNotificationSoundIcon(
                entryData = state.entryData,
                isSetNotifications = state.isSetNotifications
            )

            is ChatToolbarViewState.OnErrorUpdateNotificationStatus -> actionCallback.errorWhenUpdatedNotification()
            is ChatToolbarViewState.OnUpdateChatInputState -> actionCallback.updateChatInputEnabled(state.chatEnabled)
            is ChatToolbarViewState.OnUpdateAvatarMomentsState -> updateAvatarMomentsState(
                hasMoments = state.hasMoments,
                hasNewMoments = state.hasNewMoments
            )
        }
    }

    private fun updateChatToolbarFromNetwork(entryData: ChatEntryData) {
        viewModelDelegate.updateChatUsers(entryData)
        initConnectionHandler { isOnline ->
            if (isOnline) {
                viewModelDelegate.socketConnectionHandler(entryData)
            } else {
                Timber.e("Unable update toolbar data. App is offline")
            }
        }
    }

    private fun setAvailabilityGroupMenuAbout(isEnabled: Boolean = true) {
        binding.apply {
            chatToolbarDialogMenu.isEnabledLeftItem(isEnabled = isEnabled)
            chatRoomAvatar.isEnabled = isEnabled
        }
    }

    private fun setupClickBackButton() {
        binding.chatToolbarNav.backButtonClickListener = { actionCallback.onClickMenuBackArrow() }
    }

    private fun setupToolbarUI(entryData: ChatEntryData, toolbar: ToolbarEntity) {
        binding.apply {
            setupClickBackButton()
            when {
                isServiceRoom(entryData) -> {
                    configRoomAvatar(
                        config = UserpicUiModel(userAvatarUrl = toolbar.avatar),
                    )
                    chatRoomAvatar.setThrottledClickListener {
                        actionCallback.onClickDialogAvatar(entryData.companion)
                    }
                    chatToolbarDialogMenu.setLeftItemClick {
                        actionCallback.onClickDialogAvatar(entryData.companion)
                    }
                    chatToolbarDialogMenu.isVisibleRightItem(isVisible = false)
                    chatCallSwitch.gone()
                    tvChatRoomDescription.gone()
                    actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                    updateExtraIcons(toolbar)
                }

                isDialogRoom(entryData) -> {
                    setToolbarAvatarWithMoments(toolbar = toolbar)
                    chatRoomAvatar.setThrottledClickListener {
                        actionCallback.onClickDialogAvatar(
                            userChat = entryData.companion,
                            hasMoments = hasUserMoments(toolbar),
                            hasNewMoments = hasUserNewMoments(toolbar),
                            view = chatRoomAvatar
                        )
                    }
                    chatToolbarDialogMenu.setLeftItemClick {
                        actionCallback.onClickDialogAvatar(entryData.companion)
                    }
                    chatToolbarDialogMenu.setRightItemClick {
                        if (entryData.room?.approved.toBoolean()) {
                            actionCallback.onClickDialogMoreItem()
                        } else {
                            actionCallback.onBlockChatRequestClicked()
                        }
                    }
                    setupCallSwitch(chatCallSwitch, entryData.companion)
                    actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                    updateExtraIcons(toolbar)
                    toolbar.onlineStatus?.let { renderToolbarOnlineStatus(it) }
                }

                isGroupRoom(entryData) -> {
                    val groupAvatar = if (toolbar.avatar.isNotEmpty()) {
                        UserpicUiModel(userAvatarUrl = toolbar.avatar)
                    } else {
                        UserpicUiModel(userAvatarRes = R.drawable.meera_group_chat_avatar_placeholder)
                    }
                    configRoomAvatar(
                        config = groupAvatar,
                    )
                    chatRoomAvatar.setThrottledClickListener { handleToolbarMenuOpen(entryData) }
                    chatCallSwitch.gone()
                    chatToolbarGroupMenu.setLeftItemClick { actionCallback.onClickMenuGroupChatDetail(entryData.roomId) }
                    chatToolbarGroupMenu.setRightItemClick { actionCallback.onClickMenuGroupChatMore(entryData.roomId) }
                    actionCallback.setChatBackground(null, RoomType.GROUP)
                }
            }
            tvChatRoomName.text = toolbar.title
            tileTouchArea.setThrottledClickListener { handleToolbarMenuOpen(entryData) }
            entryData.room?.let { handleChatMenuRequest(it) }
        }
    }

    private fun redrawToolbarWhenUpdated(
        entryData: ChatEntryData,
        toolbar: ToolbarEntity
    ) {
        when {
            isServiceRoom(entryData) -> {
                configRoomAvatar(
                    config = UserpicUiModel(userAvatarUrl = toolbar.avatar),
                )
                binding.chatRoomAvatar.setThrottledClickListener {
                    actionCallback.onClickDialogAvatar(entryData.companion)
                }
                binding.chatToolbarDialogMenu.setLeftItemClick { actionCallback.onClickDialogAvatar(entryData.companion) }
                binding.chatToolbarDialogMenu.isVisibleRightItem(isVisible = false)
                binding.chatCallSwitch.gone()
                binding.tvChatRoomDescription.gone()
                actionCallback.setChatBackground(entryData.companion, RoomType.REGULAR)
                actionCallback.updatedChatData(toolbar.updatedChatData, ChatRoomType.DIALOG)
                updateExtraIcons(toolbar)
            }

            isDialogRoom(entryData) -> {
                setToolbarAvatarWithMoments(toolbar = toolbar)
                binding.chatRoomAvatar.setThrottledClickListener {
                    actionCallback.onClickDialogAvatar(
                        userChat = entryData.companion,
                        hasMoments = hasUserMoments(toolbar),
                        hasNewMoments = hasUserNewMoments(toolbar),
                        view = binding.chatRoomAvatar
                    )
                }
                binding.tvChatRoomName.text = toolbar.title
                updateExtraIcons(toolbar)
                redrawCallSwitch(binding.chatCallSwitch, toolbar)
                actionCallback.setChatBackground(
                    toolbar.updatedChatData?.companion,
                    RoomType.REGULAR
                )
                actionCallback.updatedChatData(toolbar.updatedChatData, ChatRoomType.DIALOG)
                entryData.room?.let { handleChatMenuRequest(it) }
                toolbar.onlineStatus?.let { renderToolbarOnlineStatus(it) }

            }

            isGroupRoom(entryData) -> {
                binding.chatRoomAvatar.setThrottledClickListener { handleToolbarMenuOpen(entryData) }
                actionCallback.updatedChatData(null, ChatRoomType.GROUP)
            }
        }
        binding.tileTouchArea.setThrottledClickListener { handleToolbarMenuOpen(entryData) }
    }

    private fun updateExtraIcons(toolbar: ToolbarEntity) {
        binding.ivChatCellVerified.isVisible = toolbar.approved
        binding.ivChatTopContentMaker.isVisible = toolbar.topContentMaker.takeUnless { toolbar.approved } ?: false
    }

    private fun setupCallSwitch(switch: UiKitCallSwitchView, companion: UserChat?) {
        switch.visible()
        companion?.settingsFlags?.let { settings ->
            updateCallSwitchState(
                switch = switch,
                blacklistedByMe = companion.blacklistedByMe,
                blacklistedMe = companion.blacklistedMe,
                iCanCall = settings.iCanCall,
                userCanCallMe = settings.userCanCallMe
            )

            switch.onClick = { state, isChecked ->
                if (state == SWITCH_CALL_STATE_ALLOWED) {
                    actionCallback.startCallWithCompanion(companion)
                    fragment.requireContext().vibrate()
                } else if (state == SWITCH_CALL_STATE_LOCKED) {
                    actionCallback.showCallNotAllowedTooltip()
                }
            }

            switch.onCheckedChangeListener = { isChecked ->
                viewModelDelegate.setCallPrivacyForUser(companion.userId, isChecked)
            }

            setCallVariableSettings(
                userCanCallMe = settings.userCanCallMe,
                iCanCall = settings.iCanCall
            )
        }
    }

    private fun redrawCallSwitch(switch: UiKitCallSwitchView, toolbar: ToolbarEntity) {
        val state = toolbar.callSwitchState
        updateCallSwitchState(
            switch = switch,
            blacklistedByMe = state?.blacklistedByMe,
            blacklistedMe = state?.blacklistedMe,
            iCanCall = state?.iCanCall,
            userCanCallMe = state?.userCanCallMe
        )

        setCallVariableSettings(
            userCanCallMe = state?.userCanCallMe,
            iCanCall = state?.iCanCall
        )
    }

    private fun updateCallSwitchState(
        switch: UiKitCallSwitchView,
        blacklistedByMe: Int?,
        blacklistedMe: Int?,
        iCanCall: Int?,
        userCanCallMe: Int?
    ) {
        if (blacklistedByMe == 1 || blacklistedMe == 1) {
            switch.updateState(SWITCH_CALL_STATE_LOCKED)
        } else {
            updateCallSwitchStateDependsOnICanCall(switch, iCanCall)
        }

        fragment.doDelayed(CALL_SWITCH_SET_CHECKED_DELAY) {
            if (userCanCallMe == 1) {
                switch.setCheckedState(true)
            } else {
                switch.setCheckedState(false)
            }
        }
    }

    private fun updateCallSwitchStateDependsOnICanCall(switch: UiKitCallSwitchView, iCanCall: Int?) {
        if (iCanCall == 1) {
            switch.updateState(SWITCH_CALL_STATE_ALLOWED)
        } else {
            switch.updateState(SWITCH_CALL_STATE_LOCKED)
        }
    }

    private fun setCallVariableSettings(userCanCallMe: Int?, iCanCall: Int?) {
        val isMeAvailableForCalls = if (userCanCallMe == 1 && iCanCall == 1) {
            SWITCH_CALL_STATE_ALLOWED
        } else {
            SWITCH_CALL_STATE_LOCKED
        }
        actionCallback.setCallVariableSettings(
            isCallToggleVisible = true,
            isMeAvailableForCalls = isMeAvailableForCalls
        )
    }

    private fun renderToolbarOnlineStatus(status: ChatOnlineStatusEntity) {
        if (status.isShowStatus) {
            binding.apply {
                if (status.isShowDotIndicator) {
                    handleOnlineStatusIndicator(status)
                } else {
                    configRoomAvatar(UserpicUiModel(online = false))
                }
                tvChatRoomDescription.text = status.message
            }
        }
    }

    private fun handleOnlineStatusIndicator(status: ChatOnlineStatusEntity) {
        val isOnline = status.networkStatus == NetworkChatStatus.ONLINE
        val descriptionColorRes = when {
            isOnline -> R.color.uiKitColorForegroundLink
            else -> R.color.uiKitColorForegroundSecondary
        }
        configRoomAvatar(UserpicUiModel(online = isOnline))
        binding.tvChatRoomDescription.setTextColor(
            ContextCompat.getColor(
                fragment.requireContext(),
                descriptionColorRes
            )
        )
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun renderToolbarTypingStatus(
        state: ChatToolbarViewState.OnUpdateTyping
    ) = with(binding.tvChatRoomDescription) {
        visible()
        fadeOut(ONLINE_STATUS_DURATION_MS)
        setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.uiKitColorForegroundSecondary))
        setText(state.typingText)
        fadeIn(ONLINE_STATUS_DURATION_MS)
    }

    private fun setToolbarAvatarWithMoments(toolbar: ToolbarEntity) {
        if (hasUserMoments(toolbar)) {
            configRoomAvatar(
                UserpicUiModel(
                    userAvatarUrl = toolbar.avatar,
                    storiesState = getMomentsState(toolbar)
                )
            )
        } else {
            configRoomAvatar(UserpicUiModel(userAvatarUrl = toolbar.avatar))
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

    private fun updateAvatarMomentsState(
        hasMoments: Boolean,
        hasNewMoments: Boolean
    ) {
        configRoomAvatar(
            UserpicUiModel(
                storiesState = getMomentsState(
                    hasMoments = hasMoments,
                    hasNewMoments = hasNewMoments
                )
            )
        )
    }

    private fun getMomentsState(toolbar: ToolbarEntity): UserpicStoriesStateEnum {
        val hasMoments = hasUserMoments(toolbar)
        val hasNewMoments = hasUserNewMoments(toolbar)
        return getMomentsState(hasMoments = hasMoments, hasNewMoments = hasNewMoments)
    }

    private fun getMomentsState(
        hasMoments: Boolean,
        hasNewMoments: Boolean
    ): UserpicStoriesStateEnum {
        return if (hasNewMoments) {
            UserpicStoriesStateEnum.NEW
        } else if (hasMoments) {
            UserpicStoriesStateEnum.VIEWED
        } else {
            UserpicStoriesStateEnum.NO_STORIES
        }
    }

    private fun configRoomAvatar(config: UserpicUiModel) {
        binding.chatRoomAvatar.setConfig(config)
    }

    private fun hideToolbarOnlineStatus() {
        binding.tvChatRoomDescription.gone()
    }

    private fun changeChatRequestMenuVisibility(isVisible: Boolean) {
        binding.chatToolbarRequestMenu.isVisible = isVisible
    }

    private fun handleChatMenuRequest(room: DialogEntity?) {
        room?.let {
            val isChatRequest = room.approved == DialogApproved.NOT_DEFINED.key
            if (isChatRequest) {
                setupChatRequestMenu()
            } else {
                setupSubscribeMenu(room)
            }
        }
    }

    private fun setupChatRequestMenu() {
        binding.apply {
            chatToolbarRequestMenu.clickButtonListener = { type ->
                when (type) {
                    MeeraChatToolbarRequestClickType.ALLOW -> actionCallback.allowSendMessageChatRequest()
                    MeeraChatToolbarRequestClickType.FORBID -> actionCallback.onBlockChatRequestClicked()
                }
            }
        }
        actionCallback.chatRequestStatus(isRoomChatRequest = true)
    }

    private fun setupSubscribeMenu(room: DialogEntity) {
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
        binding.apply {
            if (isChatRequest || isSubscribed || isBlocked ||
                isGroupChat || !hasConversationStarted || isSubscribeDisabled
            ) {
                chatToolbarSubscribeButton.gone()
            } else {
                chatToolbarSubscribeButton.apply {
                    visible()
                    itemClickListener = { actionCallback.subscribeToUserClicked() }
                    itemCloseListener = { actionCallback.dismissSubscriptionClicked() }
                }
            }
        }
    }

    private fun handleToolbarMenuOpen(entryData: ChatEntryData) {
        val menu = when (entryData.roomType) {
            ChatRoomType.DIALOG -> binding.chatToolbarDialogMenu
            ChatRoomType.GROUP -> binding.chatToolbarGroupMenu
        }
        if (menu.height <= MIN_MENU_HEIGHT.dp) openToolbarMenu(menu) else closeToolbarMenu(menu)
    }

    private fun openToolbarMenu(menu: ViewGroup) {
        menu.visible()
        menu.animateHeight(MENU_ANIMATE_HEIGHT.dp, MENU_ANIMATE_DURATION_MS)
        fragment.requireContext().vibrate()
    }

    private fun closeToolbarMenu(menu: ViewGroup) {
        menu.animateHeight(0, MENU_ANIMATE_DURATION_MS)
    }

    private fun closeToolbarMenu() {
        binding.apply {
            if (chatToolbarDialogMenu.isVisible) closeToolbarMenu(chatToolbarDialogMenu)
            if (chatToolbarGroupMenu.isVisible) closeToolbarMenu(chatToolbarGroupMenu)
        }
    }

    private fun setupToolbarMenu(entryData: ChatEntryData) {
        when {
            isDialogRoom(entryData) -> {
                val isMuted = entryData.companion?.settingsFlags?.notificationsOff == 1
                setupNotificationSoundIcon(isMuted = isMuted, isGroupChat = false)
                binding.chatToolbarDialogMenu.setCenterItemClick {
                    clickDialogNotificationSoundIcon(isMuted, entryData)
                }
            }

            isGroupRoom(entryData) -> {
                val isMuted = entryData.room?.isMuted ?: false
                setupNotificationSoundIcon(isMuted = isMuted, isGroupChat = true)
                binding.chatToolbarGroupMenu.setLeftItemTitle(R.string.about_chat)
                binding.chatToolbarGroupMenu.setCenterItemClick {
                    clickGroupNotificationSoundIcon(isMuted, entryData)
                }
            }
        }
    }

    private fun setupNotificationSoundIcon(isMuted: Boolean, isGroupChat: Boolean) {
        val dialogMenu = if (isGroupChat.not()) binding.chatToolbarDialogMenu else binding.chatToolbarGroupMenu
        if (isMuted) {
            dialogMenu.setCenterImageIcon(R.drawable.ic_outlined_bell_off_m)
        } else {
            dialogMenu.setCenterImageIcon(R.drawable.ic_outlined_bell_on_m)
        }
    }

    private fun redrawNotificationSoundIcon(
        entryData: ChatEntryData,
        isSetNotifications: Boolean
    ) {
        when {
            isDialogRoom(entryData) -> {
                entryData.companion?.settingsFlags?.notificationsOff = isSetNotifications.toInt()
                setupToolbarMenu(entryData.copy(companion = entryData.companion))
                setupNotificationSoundIcon(isMuted = isSetNotifications, isGroupChat = false)
                showToastAboutNotificationStatus(isSetNotifications)
            }

            isGroupRoom(entryData) -> {
                entryData.room?.isMuted = isSetNotifications
                setupToolbarMenu(entryData.copy(room = entryData.room))
                setupNotificationSoundIcon(isMuted = isSetNotifications, isGroupChat = true)
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

    private fun isServiceRoom(entryData: ChatEntryData) =
        entryData.companion?.userRole == UserRole.ANNOUNCE_USER
            || entryData.companion?.userRole == UserRole.SUPPORT_USER

    private fun isDialogRoom(entryData: ChatEntryData) = entryData.roomType == ChatRoomType.DIALOG

    private fun isGroupRoom(entryData: ChatEntryData) = entryData.roomType == ChatRoomType.GROUP

}
