package com.numplates.nomera3.modules.audioswitch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemSwitchDeviceBinding
import com.twilio.audioswitch.AudioDevice

class AudioSwitchAdapter(
    private val itemClicked: (device: AudioDevice) -> Unit
) : ListAdapter<AudioSwitchUiModel, AudioSwitchAdapter.ViewHolder>(DIFF_CALLBACK) {

    @Suppress("MoveLambdaOutsideParentheses")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_switch_device, parent, false),
            { position -> itemClicked(getItem(position).device) },
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        itemClicked: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemSwitchDeviceBinding.bind(itemView)

        init {
            binding.root.setOnClickListener { itemClicked(absoluteAdapterPosition) }
        }

        fun bind(item: AudioSwitchUiModel) {
            binding.icon.setImageResource(item.iconRes)
            binding.checkmark.isInvisible = !item.isSelected
            binding.title.text = if (item.device is AudioDevice.BluetoothHeadset)
                item.device.name else itemView.context.getString(item.titleRes)
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
