package com.numplates.nomera3.modules.redesign.fragments.main.map.events

import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMapEventDateBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel

class MeeraEventDateAdapter(
    val onItemSelected: (EventDateItemUiModel) -> Unit
) : ListAdapter<EventDateItemUiModel, MeeraEventDateAdapter.EventDateViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventDateViewHolder {
        return EventDateViewHolder(parent.inflate(R.layout.item_map_event_date))
    }

    override fun onBindViewHolder(holder: EventDateViewHolder, position: Int) {
        holder.bind(currentList[position], onItemSelected)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class EventDateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(itemUiModel: EventDateItemUiModel, onItemSelected: (EventDateItemUiModel) -> Unit) {
            val binding = ItemMapEventDateBinding.bind(itemView)
            val textColor = ResourcesCompat.getColor(
                binding.root.resources,
                if (itemUiModel.selected) R.color.soft_black else R.color.ui_gray_80,
                null
            )
            binding.tvItemMapEventDate.text = itemUiModel.dateString
            binding.tvItemMapEventDate.setTextColor(textColor)
            binding.tvItemMapEventDateDay.text = itemUiModel.dayOfWeek
            binding.tvItemMapEventDateDay.setTextColor(textColor)
            val background = if (itemUiModel.selected) {
                R.drawable.bg_item_map_event_configuration_selected
            } else {
                R.drawable.bg_item_map_event_configuration
            }
            binding.root.setBackgroundResource(background)
            binding.root.setOnClickListener { onItemSelected(itemUiModel) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EventDateItemUiModel>() {
        override fun areItemsTheSame(oldItem: EventDateItemUiModel, newItem: EventDateItemUiModel): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: EventDateItemUiModel, newItem: EventDateItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}


