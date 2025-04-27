package com.numplates.nomera3.modules.maps.ui.events.navigation.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMapEventNavigationBinding
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationItemUiModel

class EventNavigationItemAdapter(
    private val onItemSelected: (EventNavigationItemUiModel) -> Unit
) : ListAdapter<EventNavigationItemUiModel, EventNavigationItemAdapter.EventNavigationItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventNavigationItemViewHolder {
        return EventNavigationItemViewHolder(parent.inflate(R.layout.item_map_event_navigation))
    }

    override fun onBindViewHolder(holder: EventNavigationItemViewHolder, position: Int) {
        holder.bind(currentList[position], onItemSelected)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class EventNavigationItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(itemUiModel: EventNavigationItemUiModel, onItemSelected: (EventNavigationItemUiModel) -> Unit) {
            val binding = ItemMapEventNavigationBinding.bind(itemView)
            binding.tvItemMapEventNavigationTitle.text = binding.root.resources.getString(itemUiModel.titleResId)
            binding.ivItemMapEventNavigationIcon.setImageResource(itemUiModel.iconResId)
            binding.root.setOnClickListener { onItemSelected(itemUiModel) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EventNavigationItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: EventNavigationItemUiModel,
            newItem: EventNavigationItemUiModel
        ): Boolean {
            return oldItem.appName == newItem.appName
        }

        override fun areContentsTheSame(
            oldItem: EventNavigationItemUiModel,
            newItem: EventNavigationItemUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}


