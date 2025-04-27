package com.numplates.nomera3.modules.chatrooms.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chatrooms.ui.viewholder.MeeraRoomsViewHolder

class MeeraRoomsAdapter(
    private val roomCellListener: RoomCellListener
) : PagedListAdapter<UiKitRoomCellConfig, MeeraRoomsViewHolder>(diffCallback) {

    interface RoomCellListener {
        fun onRoomClicked(item: UiKitRoomCellConfig)
        fun onChangeMuteClicked(item: UiKitRoomCellConfig)
        fun onDeleteRoomClicked(item: UiKitRoomCellConfig)
    }

    @Suppress("MoveLambdaOutsideParentheses")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraRoomsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.meera_room_item_list, parent, false)
        return MeeraRoomsViewHolder(
            itemView = itemView,
            onBellOffClicked = { getItemAt(it)?.let(roomCellListener::onChangeMuteClicked) },
            onRemoveClicked = { getItemAt(it)?.let(roomCellListener::onDeleteRoomClicked) },
            onRoomClickListener = { getItemAt(it)?.let(roomCellListener::onRoomClicked) },
        )
    }

    override fun onBindViewHolder(holder: MeeraRoomsViewHolder, position: Int) {
        currentList?.get(position)?.let(holder::bind)
    }

    fun getItemAt(position: Int): UiKitRoomCellConfig? {
        return if (currentList?.isNotEmpty() == true) {
            currentList?.get(position)
        } else {
            null
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<UiKitRoomCellConfig>() {
            override fun areItemsTheSame(oldItem: UiKitRoomCellConfig, newItem: UiKitRoomCellConfig): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UiKitRoomCellConfig, newItem: UiKitRoomCellConfig): Boolean {
                return oldItem == newItem
            }
        }
    }
}
