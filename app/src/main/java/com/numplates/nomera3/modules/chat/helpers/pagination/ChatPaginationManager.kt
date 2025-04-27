package com.numplates.nomera3.modules.chat.helpers.pagination

import com.google.gson.Gson
import com.meera.db.DataStore
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.domain.usecases.MessagesPaginationUseCase
import com.numplates.nomera3.modules.chat.helpers.isEdited
import com.numplates.nomera3.modules.chat.helpers.isMomentDeleted
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.MessageSendSuccessfullyEvent
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class ChatPaginationManager @Inject constructor(
    private val dataStore: DataStore,
    private val getMessagesUseCase: MessagesPaginationUseCase
) : PaginationCallback {

    private var isTopPage = false
    private var isBottomPage = false
    private var isLoadingBefore = false
    private var isLoadingAfter = false

    private var roomId = INVALID_ROOM_ID
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _messagesFlow = MutableStateFlow<List<MessageEntity>?>(null)
    private val _messagesProgressFlow = MutableStateFlow<MessagesPaginationProgress?>(null)

    private var needUnreadSeparator = false
    private var messageWithSeparator: MessageEntity? = null

    /*
    * Данное поле используется для синхронизации базы данных и сообщений отправленных от юзера
    * Кейс заключается в следующем:
    * - Юзер отправляет быстро в чате несколько сообщений
    * - Сообщения пишутся в базу и одновременно сразу отправляются в адаптер
    * - Так как сообщения отрисовываются мнгновенно, а для базы + AsyncListDiffer нужно время
    * - Происходит рассинхрон при котором обсервер с базы затирает новые сообщения и происходят скачки
    * Новые сообщения складываются в этот список и затем синхронизируются с базой
    * В качестве source of truth можно считать messagesFlow
    * */
    private val forceInsertedMessages = mutableListOf<MessageEntity>()
    private var unreadMessageTimeStamp = 0L
    private var isChatRoomRequest = false
    private val messageDao = dataStore.messageDao()

    val messagesFlow: StateFlow<List<MessageEntity>?>
        get() = _messagesFlow

    val messagesProgressFlow: StateFlow<MessagesPaginationProgress?>
        get() = _messagesProgressFlow

    override fun loadBefore() {
        requestMessagesBefore()
    }

    override fun loadAfter() {
        requestMessagesAfter(needUnreadSeparator)
    }

    override fun isTopPage() = isTopPage

    override fun isBottomPage() = isBottomPage

    override fun isLoadingAfter() = isLoadingAfter

    override fun isLoadingBefore() = isLoadingBefore

    fun initRoomPagination(
        roomId: Long,
        unreadMessageTimeStamp: Long,
        isChatRoomRequest: Boolean,
        eventsFlow: SharedFlow<SendMessageEvent>
    ) {
        if (this.roomId != INVALID_ROOM_ID) return
        this.roomId = roomId
        this.unreadMessageTimeStamp = unreadMessageTimeStamp
        this.isChatRoomRequest = isChatRoomRequest
        startPagination()
        initEventsObserver(eventsFlow)
    }

    fun scrollDownWithRefresh() = doIfValidRoomId {
        coroutineScope.launch(Dispatchers.IO) {
            messageDao.deleteMessagesByRoomId(roomId)
            _messagesFlow.emit(emptyList())
            resetPaginationFlag()
            requestInitial(roomId)
        }
    }

    fun requestRemainMessages() {
        messageWithSeparator = null
        requestMessagesAfter(needUnreadSeparator)
    }

    fun resetPaginationFlag() {
        isTopPage = false
        isBottomPage = false
        isLoadingBefore = false
        isLoadingAfter = false
    }

    fun release() {
        coroutineScope.launch {
            reduceDbSize()
            coroutineScope.cancel()
        }
    }

    private fun initEventsObserver(flow: SharedFlow<SendMessageEvent>) {
        coroutineScope.launch {
            flow.collect { event ->
                if (event is MessageSendSuccessfullyEvent && event.message.roomId == roomId) {
                    if (!forceInsertedMessages.contains(event.message)) {
                        forceInsertedMessages.add(event.message)
                    }
                }
            }
        }
    }

    private fun startPagination() = doIfValidRoomId {
        coroutineScope.launch {
            if (unreadMessageTimeStamp > 0) {
                unreadMessagesFlowStart()
            } else if (getMessagesCount(roomId) == 0) {
                requestInitial(roomId)
            }
            messageDao
                .messagesByRoomFlow(roomId)
                .distinctUntilChanged { old, new -> sameMessagesCondition(old, new) }
                .map { list ->
                    val newList = mutableListOf<MessageEntity>()
                    newList.addAll(list)
                    forceInsertedMessages.forEach { forceInsertedMessage ->
                        if (!newList.any { it.msgId == forceInsertedMessage.msgId }) {
                            newList.add(0, forceInsertedMessage)
                        }
                    }
                    newList.sortByDescending { it.createdAt }
                    submitPaginationProgress(MessagesPaginationProgress.NONE)
                    newList
                }
                .collect(_messagesFlow::emit)
        }
    }

    private fun sameMessagesCondition(old: List<MessageEntity>, new: List<MessageEntity>): Boolean {
        return old.size == new.size
            && old.indices.all {
            val oldMsg = old[it]
            val newMsg = new[it]
            oldMsg.msgId == newMsg.msgId
                && oldMsg.sent == newMsg.sent
                && oldMsg.isResendProgress == newMsg.isResendProgress
                && oldMsg.deleted == newMsg.deleted
                && oldMsg.delivered == newMsg.delivered
                && oldMsg.readed == newMsg.readed
                && oldMsg.editedAt == newMsg.editedAt
                && oldMsg.attachment.audioRecognizedText == newMsg.attachment.audioRecognizedText
                && isRepostDataSame(oldMsg, newMsg)
                && oldMsg.isShowImageBlurChatRequest == newMsg.isShowImageBlurChatRequest
                && oldMsg.refreshMessageItem == newMsg.refreshMessageItem
                && oldMsg.isShowLoadingProgress == newMsg.isShowLoadingProgress
                && !newMsg.isEdited()
                && !newMsg.isMomentDeleted()
        }
    }

    private suspend fun reduceDbSize() = doIfValidRoomId {
        withContext(Dispatchers.IO) {
            val messageCount = messageDao.getMessageCount(roomId)
            if (messageCount > MAX_DB_MESSAGE_COUNT) {
                val lastMessage = messageDao
                    .getMessagesLimitOffset(roomId, 1, MESSAGE_LAST_POSITION)
                    .firstOrNull()
                lastMessage?.createdAt?.let {
                    messageDao.deleteMessagesByCreatedAtDESC(roomId, it)
                }
            }
        }
    }

    private suspend fun unreadMessagesFlowStart() {
        deleteMessagesForRoomId(roomId)
        requestMessagesBefore(unreadMessageTimeStamp) {
            requestMessagesAfter(true)
        }
    }

    private suspend fun deleteMessagesForRoomId(roomId: Long) = withContext(Dispatchers.IO) {
        messageDao.deleteMessagesByRoomId(roomId)
    }

    private suspend fun getMessagesCount(roomId: Long) = withContext(Dispatchers.IO) {
        return@withContext messageDao.getMessagesCountForRoomId(roomId)
    }

    private suspend fun requestInitial(roomId: Long) = withContext(Dispatchers.IO) {
        runCatching {
            getMessagesUseCase.invoke(
                roomId = roomId,
                timeStamp = 0,
                direction = MessagePaginationDirection.INITIAL,
                isChatRoomRequest = isChatRoomRequest,
                isInitialRequest = true
            )
        }
    }

    private fun requestMessagesBefore(
        timeStamp: Long? = null,
        successCallback: () -> Unit = {}
    ) = doIfValidRoomId {
        if (isLoadingBefore || isTopPage) return@doIfValidRoomId
        coroutineScope.launch(Dispatchers.IO) {
            val ts = timeStamp ?: _messagesFlow.value?.lastOrNull()?.createdAt ?: return@launch
            runCatching {
                isLoadingBefore = true
                submitPaginationProgress(MessagesPaginationProgress.BEFORE)
                val messages = getMessagesUseCase.invoke(
                    roomId = roomId,
                    timeStamp = ts,
                    direction = MessagePaginationDirection.BEFORE,
                    isChatRoomRequest = isChatRoomRequest,
                    isInitialRequest = false
                )
                isTopPage = messages.isEmpty()
                isLoadingBefore = false
                submitPaginationProgress(MessagesPaginationProgress.NONE)
                successCallback()
            }.onFailure {
                submitPaginationProgress(MessagesPaginationProgress.NONE)
                isLoadingBefore = false
                Timber.e(it)
            }
        }
    }

    private fun requestMessagesAfter(needUnreadSeparator: Boolean = false) = doIfValidRoomId {
        if (messageWithSeparator != null) return

        if (isLoadingAfter || isBottomPage) return@doIfValidRoomId
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                isLoadingAfter = true
                submitPaginationProgress(MessagesPaginationProgress.AFTER)
                this@ChatPaginationManager.needUnreadSeparator = needUnreadSeparator
                val messages = getMessagesUseCase.invoke(
                    roomId = roomId,
                    timeStamp = getLastMessageUpdatedTime(roomId),
                    direction = MessagePaginationDirection.AFTER,
                    isChatRoomRequest = isChatRoomRequest,
                    needToShowUnreadMessagesSeparator = needUnreadSeparator,
                    isInitialRequest = false
                )
                unreadMessageTimeStamp = 0
                messageWithSeparator = messages.find { it.isShowUnreadDivider }
                isBottomPage = messages.isEmpty()
                isLoadingAfter = false
                submitPaginationProgress(MessagesPaginationProgress.NONE)
            }.onFailure {
                submitPaginationProgress(MessagesPaginationProgress.NONE)
                isLoadingAfter = false
                Timber.e(it)
            }
        }
    }

    private fun getLastMessageUpdatedTime(roomId: Long?): Long {
        return if (unreadMessageTimeStamp == 0L) {
            dataStore.messageDao().getLastMessageUpdatedTime(roomId)
        } else {
            unreadMessageTimeStamp
        }
    }

    private suspend fun submitPaginationProgress(progress: MessagesPaginationProgress) {
        _messagesProgressFlow.emit(progress)
    }

    private inline fun doIfValidRoomId(action: () -> Unit) {
        if (roomId != INVALID_ROOM_ID) action.invoke()
        else Timber.e("Invalid room id for action, please check if roomId was set up correctly")
    }

    //TODO https://nomera.atlassian.net/browse/BR-28700 Переделать сравнение репоста в сообщении
    private fun isRepostDataSame(oldMsg: MessageEntity, newMsg: MessageEntity): Boolean {
        val gson = Gson()
        val oldMetadataString = gson.toJson((oldMsg.attachment.metadata))
        val newMetadataString = gson.toJson((newMsg.attachment.metadata))

        return oldMetadataString == newMetadataString
    }

    companion object {
        const val INVALID_ROOM_ID = -1L
        const val MAX_DB_MESSAGE_COUNT = 200
        const val FIRST_DATA_LIMIT = 20
        const val MESSAGE_LAST_POSITION = 100
    }
}
