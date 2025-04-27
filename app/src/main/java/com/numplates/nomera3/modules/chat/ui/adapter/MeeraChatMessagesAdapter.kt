package com.numplates.nomera3.modules.chat.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.meera.uikit.widgets.chat.call.UiKitCallView
import com.meera.uikit.widgets.chat.gift.UiKitGiftView
import com.meera.uikit.widgets.chat.media.UiKitMediaView
import com.meera.uikit.widgets.chat.moment.UiKitMomentView
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileView
import com.meera.uikit.widgets.chat.repost.UiKitRepostView
import com.meera.uikit.widgets.chat.sticker.UiKitStickersView
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListener
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.viewholder.BaseMessageViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.DateSeparatorViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.DefaultMessageViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.EmptyViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerAudioViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerCallViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerDeletedViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerGiftViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerMediaViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerMomentViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerRepostViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerShareCommunityViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerShareProfileViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.MessengerStickerViewHolder
import com.numplates.nomera3.modules.chat.ui.viewholder.ServiceMessageViewHolder
import timber.log.Timber

/**
 * EXAMPLE OF USAGE
 *
 * Add code sample below to Fragment class:
 *
 * private val uiKitAdapter by lazy {
 *     UIKitChatMessagesAdapter()
 * }
 *
 * private fun observeUiKitMessagesFlow() {
 *     chatViewModel.uiKitMessagesListFlow()
 *         .flowWithLifecycle(lifecycle)
 *         .filterNotNull()
 *         .onEach { listUiMessages ->
 *             uiKitAdapter.submitList(listUiMessages)
 *         }
 *         .launchIn(lifecycleScope)
 * }
 *
 * Add code sample below to ViewModel class:
 *
 * @Inject
 * private val messageUiMapper: MessageUiMapper,
 * private val uiKitMessageMapper: UiKitMessageMapper
 *
 * fun uiKitMessagesListFlow(): Flow<List<UiKitChatMessageData>> = paginationUtil.messagesFlow
 *     .map { it?.withInsertedDateDividers() }
 *     .map { messageEntities ->
 *         messageUiMapper.mapToMessagesUi(messageEntities)
 *     }
 *     .map { messagesUiModel ->
 *         uiKitMessageMapper.mapToUiKitChatMessagesData(messagesUiModel)
 *     }
 *     .catch { Timber.e("FLOW Error messages:$it") }
 *     .flowOn(Dispatchers.Default)
 */

class MeeraChatMessagesAdapter(
    context: Context,
    val messagesListener: MeeraMessagesListener,
) : ListAdapter<ChatMessageDataUiModel, RecyclerView.ViewHolder>(diffCallback) {

    private val listenersMapper = MeeraMessagesListenersMapper(
        messagesListener = messagesListener,
    )

    private val meeraSpanListenersMapper = MeeraSpanListenersMapper(
        context = context.applicationContext,
        messagesListener = messagesListener,
    )

    override fun getItemViewType(position: Int): Int {
        return currentList[position].messageData.messageType.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (MessageType[viewType]) {
            MessageType.DATE_TIME -> DateSeparatorViewHolder(
                itemView = parent.inflate(R.layout.meera_chat_item_service_message)
            )

            MessageType.CALL -> MessengerCallViewHolder(
                itemView = UiKitCallView(parent.context, null),
                listenersMapper = listenersMapper,
            )

            MessageType.TEXT -> DefaultMessageViewHolder(
                itemView = parent.inflate(R.layout.meera_chat_item_default),
                listenersMapper = listenersMapper,
                meeraSpanListenersMapper = meeraSpanListenersMapper,
            )

            MessageType.AUDIO -> MessengerAudioViewHolder(
                itemView = UiKitVoiceView(parent.context, null),
                listenersMapper = listenersMapper,
                messagesListener = messagesListener
            )

            MessageType.GREETING,
            MessageType.STICKER -> MessengerStickerViewHolder(
                itemView = UiKitStickersView(parent.context, null),
                listenersMapper = listenersMapper,
            )

            MessageType.REPOST -> MessengerRepostViewHolder(
                itemView = UiKitRepostView(parent.context, null),
                listenersMapper = listenersMapper,
                meeraSpanListenersMapper = meeraSpanListenersMapper,
            )

            MessageType.GIFT -> MessengerGiftViewHolder(
                itemView = UiKitGiftView(parent.context, null),
                listenersMapper = listenersMapper,
                meeraSpanListenersMapper = meeraSpanListenersMapper,
            )

            MessageType.MOMENT -> MessengerMomentViewHolder(
                itemView = UiKitMomentView(parent.context, null),
                listenersMapper = listenersMapper,
                meeraSpanListenersMapper = meeraSpanListenersMapper,
            )

            MessageType.DELETED -> MessengerDeletedViewHolder(
                itemView = parent.inflate(R.layout.meera_chat_item_deleted),
            )

            MessageType.IMAGE,
            MessageType.LIST,
            MessageType.VIDEO,
            MessageType.GIF -> MessengerMediaViewHolder(
                itemView = UiKitMediaView(parent.context, null),
                listenersMapper = listenersMapper,
                meeraSpanListenersMapper = meeraSpanListenersMapper,
            )

            MessageType.SHARE_PROFILE -> MessengerShareProfileViewHolder(
                itemView = UiKitShareProfileView(parent.context, null),
                listenersMapper = listenersMapper,
            )

            MessageType.SHARE_COMMUNITY -> MessengerShareCommunityViewHolder(
                itemView = UiKitShareProfileView(parent.context, null),
                listenersMapper = listenersMapper,

                )

            MessageType.EVENT -> ServiceMessageViewHolder(
                itemView = parent.inflate(R.layout.meera_chat_item_service_message)
            )

            MessageType.OTHER -> EmptyViewHolder(
                itemView = parent.inflate(R.layout.item_chat_empty_view)
            )

            else -> EmptyViewHolder(
                itemView = parent.inflate(R.layout.item_chat_empty_view)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EmptyViewHolder -> holder.bind(currentList[position])
            is DateSeparatorViewHolder -> holder.bind(currentList[position])
            is ServiceMessageViewHolder -> holder.bind(currentList[position])
            is BaseMessageViewHolder -> holder.bind(currentList[position])
            else -> error("Unknown view holder type.")
        }
    }

    /**
     * Retrieves the message ID associated with the item at the specified absolute position in the adapter.
     * This function retrieves the message ID from the `ChatMessageDataUiModel` contained within the item
     * at the given position in the adapter.
     *
     * @param position The absolute position of the item in the adapter.
     *
     * @return The message ID as a String, or an empty string if the item at the given position is not found or if
     * the message data is null.
     */
    fun messageIdByAbsolutePosition(position: Int): String {
        return getItem(position).messageData.id
    }

    fun getMessageItem(position: Int): MessageUiModel? {
        return try {
            getItem(position).messageData
        } catch (e: Exception) {
            Timber.e("Get message item error ${e.message}")
            null
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ChatMessageDataUiModel>() {
            override fun areItemsTheSame(oldItem: ChatMessageDataUiModel, newItem: ChatMessageDataUiModel): Boolean {
                return oldItem.messageData.id == newItem.messageData.id
            }

            override fun areContentsTheSame(oldItem: ChatMessageDataUiModel, newItem: ChatMessageDataUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
