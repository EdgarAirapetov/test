package com.numplates.nomera3.modules.chat.toolbar.ui.viewmodel

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.extensions.empty
import com.meera.core.network.websocket.ConnectionStatus
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.timeAgoChatToolbarStatus
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCallSwitcherPosition
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.domain.usecases.ListenSocketStatusUseCase
import com.numplates.nomera3.modules.chat.toolbar.data.entity.UpdatedUserResponse
import com.numplates.nomera3.modules.chat.toolbar.domain.mapper.ChatOnlineStatusMapper
import com.numplates.nomera3.modules.chat.toolbar.domain.mapper.ChatUserTypingMapper
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.ChatOnlineStatusObserverUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.ChatUserTypingObserverUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.GetChatUserInfoUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.MuteNotificationsGroupChatParams
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.MuteNotificationsGroupChatUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.SetCallPrivacyForUserParams
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.SetCallPrivacyForUserUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UnMuteNotificationsGroupChatParams
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UnMuteNotificationsGroupChatUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateCompanionNotificationsNetworkParams
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateCompanionNotificationsNetworkUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateCompanionUserDbParams
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateCompanionUserDbUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateUserDataObserverUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UserChatPreferencesListener
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.CallSwitchState
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatOnlineStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.NetworkChatStatus
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ToolbarEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.UpdatedChatData
import com.numplates.nomera3.modules.chat.toolbar.ui.utils.ChatTypingCountdownTimer
import com.numplates.nomera3.modules.chat.toolbar.ui.viewstate.ChatToolbarViewState
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.properties.Delegates


class ChatToolbarViewModelDelegate @Inject constructor(
    private val context: Context,
    private val appSettings: AppSettings,
    private val updateUserObserverUseCase: UpdateUserDataObserverUseCase,
    private val getUserInfoUseCase: GetChatUserInfoUseCase,
    private val updateCompanionUserDbUseCase: UpdateCompanionUserDbUseCase,
    private val onlineStatusObserverUseCase: ChatOnlineStatusObserverUseCase,
    private val userTypingObserverUseCase: ChatUserTypingObserverUseCase,
    private val updateCompanionNotificationsNetworkUseCase: UpdateCompanionNotificationsNetworkUseCase,
    private val muteNotificationsGroupChatUseCase: MuteNotificationsGroupChatUseCase,
    private val unMuteNotificationsGroupChatUseCase: UnMuteNotificationsGroupChatUseCase,
    private val setCallPrivacyForUserUseCase: SetCallPrivacyForUserUseCase,
    private val socket: WebSocketMainChannel,
    private val listenSocketStatusUseCase: ListenSocketStatusUseCase,
    private val userChatPreferencesListener: UserChatPreferencesListener,
    private val momentsObserverUseCase: SubscribeMomentsEventsUseCase,
    private val amplitudeHelper: AnalyticsInteractor
){
    private var scope: CoroutineScope by Delegates.notNull()

    private val _viewEvent = MutableSharedFlow<ChatToolbarViewState>()
    val viewEvent: SharedFlow<ChatToolbarViewState> = _viewEvent

    private val disposables = CompositeDisposable()
    private var socketListener: WebSocketMainChannel.WebSocketConnectionListener? = null
    private var currentOnlineStatus: ChatOnlineStatusEntity? = null
    private var isObserveTypingStarted = false
    private var isJoined = false

    fun init(
        scope: CoroutineScope
    ) {
        this.scope = scope
        subscribeToolBarEvent()
    }

    fun clear() {
        socket.removeWebSocketConnectionListener(socketListener)
        disposables.dispose()
    }

    private fun subscribeToolBarEvent() {
        userChatPreferencesListener.subscribeChatState()
            .onEach { event ->
                when (event) {
                    is UserSettingsEffect.EnabledChatEffect -> {
                        Timber.d("User chat state changed: ${event.isChatEnabled}")
                        submitViewEvent(ChatToolbarViewState.OnUpdateChatInputState(event.isChatEnabled))
                    }
                    else -> Unit
                }
            }
            .launchIn(scope)
    }

    fun observeMoments() {
        momentsObserverUseCase.invoke()
            .onEach { momentEvent ->
                if (momentEvent is MomentRepositoryEvent.UserMomentsStateUpdated) {
                    submitViewEvent(ChatToolbarViewState.OnUpdateAvatarMomentsState(
                        hasMoments = momentEvent.userMomentsStateUpdate.hasMoments,
                        hasNewMoments = momentEvent.userMomentsStateUpdate.hasNewMoments
                        )
                    )
                }
            }
            .launchIn(scope)
    }

    fun observeOnlineTypingStates(entryData: ChatEntryData) {
        if (isObserveTypingStarted || entryData.roomId == null) return
        isObserveTypingStarted = true
        val onlineStatusMapper = ChatOnlineStatusMapper(context)
        val countdownTimer = ChatTypingCountdownTimer {
            currentOnlineStatus?.let { onlineStatus ->
                submitViewEvent(ChatToolbarViewState.OnUpdateOnlineStatus(onlineStatus))
            }
        }
        isSocketJoined {
            streamOnlineStatus(entryData, onlineStatusMapper)
            streamUserTyping(entryData, countdownTimer)
        }
    }

    private fun isSocketJoined(block: () -> Unit) {
        disposables.add(
            listenSocketStatusUseCase.invoke().subscribe { socketStatus ->
                if (socketStatus is ConnectionStatus.OnChannelJoined && socketStatus.isJoined && isJoined.not()) {
                    isJoined = true
                    block.invoke()
                } else {
                    isJoined = false
                }
            }
        )
    }

    private fun streamOnlineStatus(entryData: ChatEntryData, onlineStatusMapper: ChatOnlineStatusMapper) {
        onlineStatusObserverUseCase.observe(entryData.roomId)
            .catch { exception -> Timber.e("TOOLBAR_LOG Chat ONLINE status flow collect exception$exception") }
            .onEach { status ->
                val mappedStatus = onlineStatusMapper.map(entryData, status)
                currentOnlineStatus = mappedStatus
                submitViewEvent(ChatToolbarViewState.OnUpdateOnlineStatus(mappedStatus))
            }
            .launchIn(scope)
    }

    private fun streamUserTyping(entryData: ChatEntryData, timer: ChatTypingCountdownTimer) {
        val mapper = ChatUserTypingMapper(context)
        userTypingObserverUseCase.observe()
            .onEach { typing ->
                val typingText = mapper.map(entryData, typing)
                if (timer.isStarted.not()) {
                    timer.startTimer()
                    submitViewEvent(ChatToolbarViewState.OnUpdateTyping(typingText))
                }
            }
            .launchIn(scope)
    }

    fun socketConnectionHandler(entryData: ChatEntryData) {
        socketConnectionListener { isConnected ->
            if (isConnected.get() == true) {
                observeRefreshToolbarWhenUserUpdated(entryData)
            }
        }
    }

    fun updateChatUsers(entryData: ChatEntryData) {
        if (entryData.roomType == ChatRoomType.DIALOG) {
            updateDialogUser(entryData)
        }
    }

    private fun socketConnectionListener(connected: (WeakReference<Boolean>) -> Unit) {
        socketListener = object : WebSocketMainChannel.WebSocketConnectionListener {
            override fun connectionStatus(isConnected: Boolean) {
                connected.invoke(WeakReference(isConnected))
            }
        }
        socket.addWebSocketConnectionListener(socketListener)
        connected.invoke(WeakReference(socket.isConnected()))
    }

    /**
     * Реактивно слушаем, обновление данных юзера и при необходимости
     * обновляем тулбар новыми данными
     */
    private fun observeRefreshToolbarWhenUserUpdated(entryData: ChatEntryData) {
        disposables.add(
            updateUserObserverUseCase
                .observeUserData()
                .distinct()
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    compareCurrentUserDataWhenObserve(response, entryData)
                }, { Timber.e(it) })
        )
    }

    private fun updateDialogUser(entryData: ChatEntryData) {
        entryData.companion?.userId?.let { uid ->
            scope.launch(Dispatchers.IO) {
                getChatUsers(uid) { response ->
                    if (response.isEmpty()) {
                        logNonFatalUpdateDialogUserEmpty(uid)
                    } else {
                        val companion = response[0]
                        val moments = companion.moments ?: UserMomentsDto.emptyMoments()
                        val toolbarEntity = ToolbarEntity(
                            avatar = companion.avatarSmall ?: String.empty(),
                            title = companion.name ?: String.empty(),
                            onlineStatus = getUserOnlineStatus(companion),
                            callSwitchState = CallSwitchState(
                                blacklistedByMe = companion.blacklistedByMe,
                                blacklistedMe = companion.blacklistedMe,
                                iCanCall = companion.settingsFlags?.iCanCall,
                                userCanCallMe = companion.settingsFlags?.userCanCallMe
                            ),
                            updatedChatData = UpdatedChatData(
                                companion = companion
                            ),
                            accountType = companion.accountType,
                            approved = companion.approved == 1,
                            topContentMaker = companion.topContentMaker == 1,
                            moments = UserMomentsMapper.mapUserMomentsModel(moments)
                        )
                        submitViewEvent(ChatToolbarViewState.OnUpdateData(entryData, toolbarEntity))
                    }
                }
            }
        }
    }

    private fun getUserOnlineStatus(user: UserChat): ChatOnlineStatusEntity {
        val onlineStatus = user.onlineStatus
        val isOnline = onlineStatus?.online == true
        val ts = onlineStatus?.lastActive
        val message = if (ts != null) timeAgoChatToolbarStatus(context, ts.toLong() * 1000L) else String.empty()
        return ChatOnlineStatusEntity(
            isShowStatus = true,
            networkStatus = if (isOnline) NetworkChatStatus.ONLINE else NetworkChatStatus.OFFLINE,
            isShowDotIndicator = true,
            message = if (isOnline) context.getString(R.string.online_status_txt) else message
        )
    }

    private suspend fun getChatUsers(userId: Long, onResult: (List<UserChat>) -> Unit) {
        runCatching { getUserInfoUseCase.invokeRest(userId) }
            .onSuccess { users -> onResult.invoke(users) }
            .onFailure { error -> Timber.e("CHAT_USER_LOG error getting user data E:$error") }
    }

    private fun compareCurrentUserDataWhenObserve(
        response: UpdatedUserResponse,
        entryData: ChatEntryData
    ) {
        val isValidRoomOrUid = if (entryData.roomId != 0L) {
            response.roomId == entryData.roomId
        } else {
            response.userId == entryData.companion?.userId
        }

        val isCurrentCompanion = isValidRoomOrUid && response.userId == entryData.companion?.userId
        if (entryData.roomType == ChatRoomType.DIALOG && isCurrentCompanion) {
            updateChatUsers(entryData)
        }
    }

    private fun logNonFatalUpdateDialogUserEmpty(userId: Long) {
        FirebaseCrashlytics.getInstance().recordException(
            IllegalArgumentException("Received empty chat users for $userId")
        )
    }

    fun setCallPrivacyForUser(userId: Long?, isSet: Boolean) {
        amplitudeHelper.logTogglePress(
            userId = appSettings.readUID(),
            companionUserId = userId ?: 0,
            getAmplitudeSwitcherPosition(isSet)
        )
        userId?.let {
            scope.launch(Dispatchers.IO) {
                setCallPrivacyForUserUseCase.execute(
                    params = SetCallPrivacyForUserParams(userId, isSet),
                    success = { Timber.d("SET Call privacy for user success") },
                    fail = { exception -> Timber.d("SET Call privacy for user FAIL:$exception") }
                )
            }
        }
    }

    private fun getAmplitudeSwitcherPosition(isSet: Boolean) = if (isSet) {
        AmplitudePropertyChatCallSwitcherPosition.ON
    } else {
        AmplitudePropertyChatCallSwitcherPosition.OFF
    }

    fun updateDialogNotificationStatus(
        isSetPrivacyNotifications: Boolean,
        entryData: ChatEntryData
    ) {
        entryData.companion?.userId?.let { id ->
            scope.launch(Dispatchers.IO) {
                updateCompanionNotificationsNetworkUseCase.execute(
                    params = UpdateCompanionNotificationsNetworkParams(
                        userId = id,
                        isMuted = isSetPrivacyNotifications
                    ),
                    success = {
                        submitViewEvent(ChatToolbarViewState.OnUpdateNotificationStatus(
                            entryData = entryData,
                            isSetNotifications = isSetPrivacyNotifications
                        ))

                        updateDialogNotificationStatusDb(
                            roomId = entryData.roomId,
                            companion = entryData.companion
                        )
                    },
                    fail = {
                        Timber.e(it)
                        submitViewEvent(ChatToolbarViewState.OnErrorUpdateNotificationStatus)
                    }
                )
            }
        }
    }

    private fun updateDialogNotificationStatusDb(roomId: Long?, companion: UserChat) {
        roomId?.let { id ->
            companion.settingsFlags?.notificationsOff =
                when (companion.settingsFlags?.notificationsOff) {
                    1 -> 0
                    0 -> 1
                    else -> 0
                }
            updateCompanionIntoDb(id, companion)
        }
    }

    private fun updateCompanionIntoDb(roomId: Long, companion: UserChat) {
        scope.launch(Dispatchers.IO) {
            updateCompanionUserDbUseCase.execute(
                params = UpdateCompanionUserDbParams(roomId, companion),
                success = { Timber.d("Chat Successfully updated companion user into Db") },
                fail = { Timber.e("Internal Db error when update companion user") }
            )
        }
    }

    fun updateCompanion(roomId: Long, userId: Long) {
        scope.launch(Dispatchers.IO) {
            getChatUsers(userId) { response ->
                val companion = response[0]
                updateCompanionIntoDb(roomId, companion)
            }
        }
    }

    fun updateGroupNotificationStatus(isMuted: Boolean, entryData: ChatEntryData) {
        scope.launch {
            if (isMuted) {
                unMuteNotificationsGroupChat(entryData)
            } else {
                muteNotificationsGroupChat(entryData)
            }
        }
    }

    private suspend fun muteNotificationsGroupChat(entryData: ChatEntryData) = withContext(Dispatchers.IO) {
        muteNotificationsGroupChatUseCase.execute(
            params = MuteNotificationsGroupChatParams(entryData.roomId ?: return@withContext),
            success = {
                submitViewEvent(ChatToolbarViewState.OnUpdateNotificationStatus(entryData, true))
            },
            fail = {
                Timber.e(it)
                submitViewEvent(ChatToolbarViewState.OnErrorUpdateNotificationStatus)
            }
        )
    }

    private suspend fun unMuteNotificationsGroupChat(entryData: ChatEntryData) = withContext(Dispatchers.IO) {
        unMuteNotificationsGroupChatUseCase.execute(
            params = UnMuteNotificationsGroupChatParams(entryData.roomId ?: return@withContext),
            success = {
                submitViewEvent(ChatToolbarViewState.OnUpdateNotificationStatus(entryData, false))
            },
            fail = {
                Timber.e(it)
                submitViewEvent(ChatToolbarViewState.OnErrorUpdateNotificationStatus)
            }
        )
    }

    private fun submitViewEvent(event: ChatToolbarViewState) {
        scope.launch { _viewEvent.emit(event) }
    }

}
