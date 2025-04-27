package com.numplates.nomera3.modules.chatrooms.ui.viewholder

import android.view.View
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.db.models.userprofile.UserRole
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraRoomItemListBinding
import com.numplates.nomera3.modules.chatrooms.ui.gestures.SwipeViewHolder

open class MeeraRoomsViewHolder(
    itemView: View,
    @Suppress("detekt:UnusedPrivateMember") onBellOffClicked: (position: Int) -> Unit,
    onRemoveClicked: (position: Int) -> Unit,
    onRoomClickListener: (position: Int) -> Unit,
) : RecyclerView.ViewHolder(itemView), SwipeViewHolder {

    private val binding = MeeraRoomItemListBinding.bind(itemView)

    init {
        // TODO: Закоментировал т.к. это надо будет вернуть во 2й итерации
        // binding.bsBellOff.setThrottledClickListener { onBellOffClicked.invoke(bindingAdapterPosition) }
        binding.bsDelete.setThrottledClickListener { onRemoveClicked.invoke(bindingAdapterPosition) }
        binding.ukRoomCell.setThrottledClickListener { onRoomClickListener.invoke(bindingAdapterPosition) }
        binding.ukRoomCell.setOnLongClickListener { true }
    }

    override fun canSwipe(): Boolean {
        val adapter = bindingAdapter as? PagedListAdapter<*, *>
        val item = adapter?.currentList?.get(bindingAdapterPosition) as? UiKitRoomCellConfig
        return item?.role != UserRole.SUPPORT_USER.value
    }

    fun bind(item: UiKitRoomCellConfig) {
        binding.ukRoomCell.setMessageCellConfig(item)
        configureMuteButton(item)
    }

    private fun configureMuteButton(item: UiKitRoomCellConfig) {
        val (imageRes, stringRes) = if (!item.isMuted) {
            R.drawable.ic_outlined_bell_off_m to R.string.general_bell_off
        } else {
            R.drawable.ic_outlined_bell_m to R.string.general_bell_on
        }
        // TODO: Закоментировал т.к. это надо будет вернуть во 2й итерации
        // binding.bsBellOff.setImageResource(imageRes)
        // binding.bsBellOff.setText(itemView.context.getString(stringRes))
    }
}
