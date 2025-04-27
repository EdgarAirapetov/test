package com.numplates.nomera3.presentation.view.adapter.newchat

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.applyRoundedOutline
import com.numplates.nomera3.R
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.visible
import com.numplates.nomera3.presentation.viewmodel.viewevents.SettingsPrivateMessagesState


class ChatSettingsAdapter(
    private val messagesSettingClickListener: (state: SettingsPrivateMessagesState?) -> Unit,
    private val chatRequestClickListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int =
        asyncDiffer.currentList[position].itemType.key

    override fun getItemCount() = asyncDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageSettingsItemType.SETTINGS.key -> ChatSettingVH(
                parent.inflate(R.layout.chat_rooms_private_messages_disallow_item),
                messagesSettingClickListener
            )
            MessageSettingsItemType.CHAT_REQUEST.key -> ChatRequestVH(
                parent.inflate(R.layout.chat_rooms_request),
                chatRequestClickListener
            )
            else -> throw IllegalArgumentException("Incorrect view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = asyncDiffer.currentList[position]
        when (item.itemType) {
            MessageSettingsItemType.SETTINGS ->
                (holder as ChatSettingVH).bind(item)
            MessageSettingsItemType.CHAT_REQUEST ->
                (holder as ChatRequestVH).bind(item.chatRequestData)
        }
    }

    fun submitList(list: List<MessagesSettings?>, submitReady: () -> Unit = {}) {
        asyncDiffer.submitList(list, submitReady)
    }

    private val asyncDiffer = AsyncListDiffer(this,
        object : DiffUtil.ItemCallback<MessagesSettings>() {
            override fun areItemsTheSame(
                oldItem: MessagesSettings,
                newItem: MessagesSettings
            ) = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: MessagesSettings,
                newItem: MessagesSettings
            ) = oldItem == newItem

        })
}

class ChatSettingVH(
    itemView: View,
    private val messagesSettingClickListener: (state: SettingsPrivateMessagesState?) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val messageSettings = itemView.findViewById<TextView>(R.id.tvChatDisallowMessageChange)

    fun bind(chatSettings: MessagesSettings) {
        itemView.applyRoundedOutline(R.dimen.material8)
        messageSettings.setOnClickListener { messagesSettingClickListener(chatSettings.settingState) }
    }
}

class ChatRequestVH(
    itemView: View,
    private val chatRequestClickListener: () -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val countBadge: TextView = itemView.findViewById(R.id.tv_request_counter_badge)

    fun bind(data: ChatRequestItemData?) {
        data?.requestCount?.let { count ->
            if (count > 0) countBadge.visible() else countBadge.gone()
            countBadge.text = count.toString()
        }
        data?.isMuteCounter?.let { isMute ->
            countBadge.background =
                if (isMute) drawable(R.drawable.room_item_circle_badge_grey)
                else drawable(R.drawable.room_item_circle_badge)
        }
        itemView.click { chatRequestClickListener() }
    }

    private fun drawable(@DrawableRes drawableRes: Int) =
        ContextCompat.getDrawable(itemView.context, drawableRes)
}

data class MessagesSettings(
    val itemType: MessageSettingsItemType = MessageSettingsItemType.SETTINGS,
    val settingState: SettingsPrivateMessagesState? = null,
    val chatRequestData: ChatRequestItemData? = null
)

enum class MessageSettingsItemType(val key: Int) {
    SETTINGS(0), CHAT_REQUEST(1)
}

data class ChatRequestItemData(
    val requestCount: Int,
    var isMuteCounter: Boolean? = null
)
