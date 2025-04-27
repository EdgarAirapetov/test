package com.numplates.nomera3.modules.redesign.fragments.main.map.events.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemMapEventTypeBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemColorSchemeUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel

class MeeraEventTypeAdapter(
    private val isOnboarding: Boolean,
    val onItemSelected: (EventTypeItemUiModel) -> Unit
) : ListAdapter<EventTypeItemUiModel, MeeraEventTypeAdapter.EventTypeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventTypeViewHolder {
        return if (isOnboarding){
            EventTypeViewHolder(parent.inflate(R.layout.meera_onboarding_item_map_event_type))
        } else {
            EventTypeViewHolder(parent.inflate(R.layout.meera_item_map_event_type))
        }
    }

    override fun onBindViewHolder(holder: EventTypeViewHolder, position: Int) {
        holder.bind(currentList[position], onItemSelected)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class EventTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(itemUiModel: EventTypeItemUiModel, onItemSelected: (EventTypeItemUiModel) -> Unit) {
            val binding = MeeraItemMapEventTypeBinding.bind(itemView)
            val textColor = ResourcesCompat.getColor(
                binding.root.resources,
                if (itemUiModel.selected) itemUiModel.selectedColorScheme.textColorResId else R.color.ui_gray_80,
                null
            )
            binding.tvItemMapEventTypeTitle.setText(itemUiModel.titleResId)
            binding.tvItemMapEventTypeTitle.setTextColor(textColor)
            binding.ivItemMapEventTypeImage.setImageResource(itemUiModel.imgResId)
            val context = binding.root.context
            val backgroundDrawable = if (itemUiModel.selected) {
                getSelectedTypeItemBackgroundDrawable(context = context, colorScheme = itemUiModel.selectedColorScheme)
            } else {
                AppCompatResources.getDrawable(context, R.drawable.bg_item_map_event_configuration)
            }
            binding.root.background = backgroundDrawable
            binding.root.setOnClickListener { onItemSelected(itemUiModel) }
        }

        private fun getSelectedTypeItemBackgroundDrawable(
            context: Context,
            colorScheme: EventTypeItemColorSchemeUiModel
        ): Drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE;
            setColor(ContextCompat.getColor(context, colorScheme.backgroundColorResId))
            setStroke(OUTLINE_WIDTH_DP.dp, ContextCompat.getColor(context, colorScheme.outlineColorResId))
            cornerRadius = OUTLINE_RADIUS_DP.dp.toFloat()
        }

        companion object {
            private const val OUTLINE_WIDTH_DP = 1
            private const val OUTLINE_RADIUS_DP = 12
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EventTypeItemUiModel>() {
        override fun areItemsTheSame(oldItem: EventTypeItemUiModel, newItem: EventTypeItemUiModel): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: EventTypeItemUiModel, newItem: EventTypeItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}


