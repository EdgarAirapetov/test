package com.numplates.nomera3.modules.audioswitch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemSwitchDeviceBinding
import com.twilio.audioswitch.AudioDevice

class MeeraAudioSwitchAdapter(
    private val itemClicked: (device: AudioDevice) -> Unit
): ListAdapter<AudioSwitchUiModel, MeeraAudioSwitchAdapter.ViewHolder>(DIFF_CALLBACK) {

    @Suppress("MoveLambdaOutsideParentheses")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.meera_item_switch_device, parent, false),
            { position -> itemClicked(getItem(position).device) },
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isLastItem = currentList.lastIndex == position
        holder.bind(getItem(position), position, isLastItem)
    }

    class ViewHolder(
        itemView: View,
        itemClicked: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = MeeraItemSwitchDeviceBinding.bind(itemView)

        init {
            binding.root.setThrottledClickListener { itemClicked(absoluteAdapterPosition) }
        }

        fun bind(item: AudioSwitchUiModel, position: Int, isLastItem: Boolean) {
            val cell = binding.root
            cell.setLeftIcon(item.iconRes)
            val titleText = if (item.device is AudioDevice.BluetoothHeadset)
                item.device.name else itemView.context.getString(item.titleRes)
            cell.setTitleValue(titleText)
            if (item.isSelected) {
                cell.setRightIcon(R.drawable.ic_outlined_check_m)
                cell.setRightColorIcon(R.color.uiKitColorForegroundLink)
            }

            cell.cellPosition = when {
                position == 0 -> CellPosition.TOP
                isLastItem -> CellPosition.BOTTOM
                else -> CellPosition.MIDDLE
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AudioSwitchUiModel>() {
    override fun areContentsTheSame(oldItem: AudioSwitchUiModel, newItem: AudioSwitchUiModel): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: AudioSwitchUiModel, newItem: AudioSwitchUiModel): Boolean {
        return oldItem == newItem
    }
}
