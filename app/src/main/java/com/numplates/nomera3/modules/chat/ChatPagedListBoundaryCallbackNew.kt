package com.numplates.nomera3.modules.chat

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.isTrue
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.DataStore
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.RequestDirection
import com.meera.db.models.message.ResponseData
import com.numplates.nomera3.GIPHY_BRAND_NAME
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.newmessenger.response.ResponseMessages
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.chat.ChatViewModel.Companion.CHAT_MESSAGES_PAGE_SIZE
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.helpers.ExpandBtnVoiceMessageStorage
import com.numplates.nomera3.modules.chat.helpers.resendmessage.attachmentsSaver
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.helpers.toParentMessage
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.model.enums.ScrollDirection
import com.numplates.nomera3.presentation.utils.parseUniquename
import com.numplates.nomera3.presentation.view.adapter.newpostlist.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors

@Deprecated("Unused. For example")
class ChatPagedListBoundaryCallbackNew(private val roomId: Long?,
                                       private val room: DialogEntity?,
                                       private val ownUserId: Long,
                                       private val webSocketMain: WebSocketMainChannel,
                                       private val dataStore: DataStore,
                                       private val gson: Gson,
                                       private var unreadMessageTs: Long,
                                       private val coroutineScope: CoroutineScope,
                                       private val messageCountResult: (ResponseData) -> Unit) :
        PagedList.BoundaryCallback<MessageEntity>() {


    private val helper = PagingRequestHelper(Executors.newSingleThreadExecutor())

    val networkState: MutableLiveData<ChatNetworkState> = MutableLiveData()
    private var isSetUnreadDivider = false
    private var onZeroItemLoadedWasCalled = false

    private var isAfterRequestCalled = false

    val voiceMessagesBtnStateStorage = ExpandBtnVoiceMessageStorage
    var birthdayTextUtils: BirthdayTextUtil? = null
    var userBirthdayInfo: UserBirthdayUtils? = null

    data class ChatNetworkState(
        val direction: ScrollDirection = ScrollDirection.TOP,
        val networkState: NetworkState.Status
    )

    // Set all vars by default
    fun clearStates(){
        isAfterRequestCalled = false
    }

    fun updateChatRequestData(status: DialogApproved) {
        room?.approved = status.key
    }

    override fun onZeroItemsLoaded() {
        // Timber.e("On ZERO message item loaded: TS:$unreadMessageTs")
        onZeroItemLoadedWasCalled = true
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            roomId?.let { id ->
                val payload = hashMapOf<String, Any>(
                        "room_id" to id,
                        "user_type" to "UserChat",
                        // !!! payload - должен быть без ts
                        // "ts" to 0  -- С включенным ts=0 происходит баг => возвращаются старые значения
                )

                // If unread messages exists
                if (unreadMessageTs > 0) {
                    payload["direction"] = "before"
                    payload["ts"] = unreadMessageTs
                }
                getMessagesFromNetworkCoroutine(
                        payload,
                        RequestDirection.ZERO,
                        {
                            networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.RUNNING))
                        },
                        { rows ->
                            messageCountResult.invoke(ResponseData(RequestDirection.ZERO, rows))
                            networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.SUCCESS))

                            helperCallback.recordSuccess()
                            if (rows == 0) {
                                payload["ts"] = unreadMessageTs - 1 //так как с текущем тс теряем первое сообщение
                                forceRequestAfter(payload, messageCountResult)
                            }
                        },
                        { error ->
                            networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.FAILED))
                            helperCallback.recordFailure(error)
                        }
                )
            }
        }
    }

    private fun forceRequestAfter(payload: HashMap<String, Any>,
                                  messageCountResult: (ResponseData) -> Unit) {
        // Timber.e("FORCE After")
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            payload["direction"] = "after"
            getMessagesFromNetworkCoroutine(
                    payload,
                    RequestDirection.ZERO,
            {
                networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.RUNNING))
            }, { rows ->
                messageCountResult.invoke(ResponseData(RequestDirection.ZERO, rows))
                networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.SUCCESS))
                helperCallback.recordSuccess()
            }, { error ->
                networkState.postValue(ChatNetworkState(ScrollDirection.TOP, NetworkState.Status.FAILED))
                helperCallback.recordFailure(error)
            })
        }
    }


    override fun onItemAtEndLoaded(itemAtEnd: MessageEntity) {
        // Timber.e("On item at END: updatedAt: ${itemAtEnd.updatedAt} MSG: ${itemAtEnd.content}")
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            roomId?.let { id ->
                val createdAt = itemAtEnd.createdAt
                if (createdAt > 0) {
                    val payload = hashMapOf<String, Any>(
                            "room_id" to id,
                            "ts" to createdAt,
                            "direction" to "before",
                            "user_type" to "UserChat"
                    )

                    // Timber.d("AT End get messages: $createdAt Message: ${itemAtEnd.content}")
                    getMessagesFromNetworkCoroutine(payload,
                            RequestDirection.BEFORE,
                            {
                                // running
                                networkState.postValue(ChatNetworkState(
                                    ScrollDirection.TOP,
                                    NetworkState.Status.RUNNING)
                                )
                            },
                            { rows ->
                                // success
                                messageCountResult.invoke(ResponseData(RequestDirection.BEFORE, rows))
                                networkState.postValue(ChatNetworkState(
                                    ScrollDirection.TOP,
                                    NetworkState.Status.SUCCESS)
                                )
                                helperCallback.recordSuccess()
                            },
                            { error ->
                                // failure
                                networkState.postValue(ChatNetworkState(
                                    ScrollDirection.TOP,
                                    NetworkState.Status.FAILED)
                                )
                                helperCallback.recordFailure(error)
                            })

                }
            }
        }
    }


    override fun onItemAtFrontLoaded(itemAtFront: MessageEntity) {
        // Timber.d("On item at FRONT: updatedAt: ${itemAtFront.updatedAt} MSG content:
        // ${itemAtFront.content} UnreadMessageTS:$unreadMessageTs")
        onItemAtFrontCoroutine()
    }


    private fun onItemAtFrontCoroutine(){
        helper.runIfNotRunning(PagingRequestHelper.RequestType.BEFORE) { helperCallback ->
            coroutineScope.launch(Dispatchers.IO){
                // running
                networkState.postValue(ChatNetworkState(ScrollDirection.BOTTOM, NetworkState.Status.RUNNING))

                val id = roomId ?: 0L
                val payload = hashMapOf(
                        "room_id" to id,
                            //"ts" to updatedAt,
                        "direction" to "after",
                        "user_type" to "UserChat"
                )

                if (unreadMessageTs == 0L) {
                    payload["ts"] = dataStore.messageDao().getLastMessageUpdatedTime(roomId)
                } else {
                    payload["ts"] = unreadMessageTs
                }

                // Network request
                try {
                    // Timber.d("REQUEST => Network [AFTER] Req:$payload
                    // LastMSG:${itemAtFront.content} => UpdatedAt:${itemAtFront.updatedAt}")
                    val response = webSocketMain.pushGetMessagesSuspend(payload)
                    val json = gson.toJson(response.payload)
                    //Timber.e("get_messages REQUEST[After]:(PAYLOAD:$payload) RESPONSE:$json")
                    val messagesResponse = gson.fromJson<ResponseMessages>(json)
                    val responseMessages = messagesResponse.response.messages
                    val messages = handleRequestResult(RequestDirection.AFTER, responseMessages)

                    // Set 0 for unread message
                    unreadMessageTs = 0

                    messageCountResult.invoke(ResponseData(RequestDirection.AFTER, responseMessages.size))
                    insertWithoutReplace(messages)

                    // Timber.d("[After] PROGRESS => uuu SUCCESS:")
                    networkState.postValue(ChatNetworkState(ScrollDirection.BOTTOM, NetworkState.Status.SUCCESS))
                    helperCallback.recordSuccess()
                }catch (e: Exception){
                    Timber.e("Error get_messages: ${e.message}")
                    networkState.postValue(ChatNetworkState(ScrollDirection.BOTTOM, NetworkState.Status.FAILED))
                    helperCallback.recordFailure(e)
                }
            }
        }
    }


    @Suppress("LocalVariableName")
    private fun insertWithoutReplace(_messages: List<MessageEntity>){
        val messages = _messages.map { msg ->
            msg.copy(
                itemType = resolveMessageType(
                    creatorId = msg.creator?.userId,
                    attachment = msg.attachment,
                    attachments = msg.attachments,
                    deleted = msg.deleted,
                    eventCode = msg.eventCode,
                    type = msg.type,
                    myUid = ownUserId
                )
            )
        }
        val insertResult = dataStore.messageDao().insertWithoutReplace(messages)
        val updateList = mutableListOf<MessageEntity>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(messages[i])
            }
        }

        if (updateList.isNotEmpty()) {
            updateList.forEach { msg ->
                val dbMessage = dataStore.messageDao().getMessageById(msg.msgId)
                msg.isShowUnreadDivider = dbMessage?.isShowUnreadDivider ?: false
                msg.attachments = attachmentsSaver(
                    dbMessage?.attachments ?: emptyList(),
                    msg.attachments
                )
                msg.isShowImageBlurChatRequest = dbMessage?.isShowImageBlurChatRequest ?: false
                msg.birthdayRangesList = dbMessage?.birthdayRangesList
                dataStore.messageDao().update(msg)
            }
        }
    }



    private fun getMessagesFromNetworkCoroutine(payload: HashMap<String, Any>,
                                                requestDirection: RequestDirection,
                                                actionRunning: () -> Any,
                                                actionSuccess: (Int) -> Any,
                                                actionFailure: (error: Throwable) -> Any){
        coroutineScope.launch(Dispatchers.IO){
            // Network request
            actionRunning.invoke()
            try {
                val response = webSocketMain.pushGetMessagesSuspend(payload)
                val json = gson.toJson(response.payload)
                // Timber.e("get_messages REQUEST:(PAYLOAD:$payload) RESPONSE:$json")
                val messagesResponse = gson.fromJson<ResponseMessages>(json)
                val responseMessages = messagesResponse.response.messages
                val messages = handleRequestResult(requestDirection, responseMessages)
                //val rows = dataStore.messageDao().insert(messages)
                actionSuccess.invoke(0)
            }catch (e: Exception){
                Timber.e("Error get_messages:${e.message}")
                actionFailure.invoke(e)
            }
        }
    }

    private var isSetFirstMessageResponseData: Boolean = false


    /**
     * Set request data to every message
     */
    private fun handleRequestResult(
        requestDirection: RequestDirection,
        messages: List<MessageEntity>
    ): List<MessageEntity> {

        //Timber.d("REQ Messages size:${messages.size}")
        // Check unread divider exists
        val countUnreadDivider = dataStore.messageDao().getCountShowUnreadDivider(roomId)
        messages.forEach { msg ->
            // Set response data to every message
            msg.responseData = ResponseData(requestDirection, messages.size)

            // Set unread divider in first unread message
            // && onZeroItemLoadedWasCalled - todo с этим флагом не работает плашка, если мы вошли через профиль
            if (!msg.readed && !isSetUnreadDivider && countUnreadDivider == 0 && !isAfterRequestCalled) {
                isSetUnreadDivider = true
                msg.isShowUnreadDivider = true
            }

            // Resolve itemType
            msg.itemType = resolveMessageType(
                creatorId = msg.creator?.userId,
                attachment = msg.attachment,
                attachments = msg.attachments,
                deleted = msg.deleted,
                eventCode = msg.eventCode,
                type = msg.type,
                myUid = ownUserId
            )
            msg.parent?.itemType = resolveMessageType(
                creatorId = msg.parent?.creator?.userId,
                attachment = msg.parent?.attachment,
                attachments = msg.parent?.attachments.orEmpty(),
                deleted = msg.parent?.deleted.isTrue(),
                eventCode = msg.parent?.eventCode,
                type = msg.parent?.type,
                myUid = ownUserId
            )

            // Parse unique name in message
            msg.tagSpan = parseUniquename(msg.content, msg.tags)
            msg.parent?.tagSpan = parseUniquename(msg.parent?.content, msg.parent?.tags)
            // облегченная модель, которая записывается в бд parent игнорируется и не пишется в бд
            msg.parentMessage = msg.parent?.toParentMessage()
            createBirthdayRanges(msg)

            // Parse unique name in repost
            (msg.attachment.metadata["post"] as? LinkedTreeMap<String, Any>)?.let {
                val post = Gson().fromJson<Post?>(it)
                post?.tagSpan = parseUniquename(post?.text, post?.tags)
                post?.event?.tagSpan = parseUniquename(post?.event?.title, post?.event?.tags)
                val json = gson.toJson(post)
                msg.attachment.metadata["post"] = gson.fromJson(json, Map::class.java)
            }

            val attachmentUrl = msg.attachment.url
            if (attachmentUrl.isNotEmpty() && GIPHY_BRAND_NAME in attachmentUrl) {
                msg.isShowGiphyWatermark = true
            }

            if (msg.attachment.audioRecognizedText.isNotEmpty()
                && voiceMessagesBtnStateStorage.messages.contains(msg.msgId).not()) {
                msg.isExpandedRecognizedText = false
                voiceMessagesBtnStateStorage.messages.add(msg.msgId)
            }

            checkIsBlurMessage(msg, ownUserId)
        }

        // For prevent unread divider appearance
        if (requestDirection == RequestDirection.AFTER) {
            isAfterRequestCalled = true
        }

        // Set response data message count=0 in first message (BEFORE)
        if (messages.isNotEmpty() &&  messages.size <  CHAT_MESSAGES_PAGE_SIZE * 2) {     // Prefetch distance
            messages.first().responseData = ResponseData(requestDirection, 0)
        }
        // Set datetime divider on a first message
        else if(messages.isEmpty()
                && requestDirection == RequestDirection.BEFORE
                && !isSetFirstMessageResponseData){
            isSetFirstMessageResponseData = true
            roomId?.let { id ->
                val firstMessage = dataStore.messageDao().getFirstMessage(id)
                firstMessage?.responseData = ResponseData(requestDirection, 0)
                firstMessage?.let {
                    // dataStore.messageDao().update(firstMessage)
                    dataStore.messageDao().refreshMessageItem(id, firstMessage.msgId)
                }
            }
        }
        return messages
    }

    private fun checkIsBlurMessage(message: MessageEntity, myUid: Long) {
        if ((room?.approved == DialogApproved.NOT_DEFINED.key || room?.approved == DialogApproved.FORBIDDEN.key)
            && message.creator?.userId != myUid) {
            message.isShowImageBlurChatRequest = true
        }
    }

    private fun createBirthdayRanges(message: MessageEntity) {
        message.birthdayRangesList = birthdayTextUtils?.getBirthdayTextListRanges(
            birthdayText = message.tagSpan?.text ?: ""
        )
    }
}
