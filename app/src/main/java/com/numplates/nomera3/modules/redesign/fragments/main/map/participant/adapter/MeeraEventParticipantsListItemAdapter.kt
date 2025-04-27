package com.numplates.nomera3.modules.redesign.fragments.main.map.participant.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemParticipantsListBinding
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiAction

class MeeraEventParticipantsListItemAdapter(
    val onAction: (EventParticipantsListUiAction) -> Unit
) : ListAdapter<EventParticipantsListItemUiModel,
    MeeraEventParticipantsListItemAdapter.MeeraEventParticipantsListItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraEventParticipantsListItemViewHolder {
        return MeeraEventParticipantsListItemViewHolder(parent.inflate(R.layout.meera_item_participants_list))
    }

    override fun onBindViewHolder(holder: MeeraEventParticipantsListItemViewHolder, position: Int) {
        holder.bind(currentList[position], onAction)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class MeeraEventParticipantsListItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bind(itemUiModel: EventParticipantsListItemUiModel, onAction: (EventParticipantsListUiAction) -> Unit) {
            val binding = MeeraItemParticipantsListBinding.bind(itemView)
            binding.tvItemParticipantsListName.text = itemUiModel.name
            with(itemUiModel) {

                val text = if (isHost) {
                    view.context.getString(R.string.map_events_participants_host)
                } else if (isFriend) {
                    view.context.getString(R.string.map_events_participants_friend)
                } else if (isSubscribed) {
                    view.context.getString(R.string.map_events_participants_subscribed)
                } else {
                    null
                }
                if (isHost || isFriend || isSubscribed) {
                    binding.tvItemParticipantsListHostLabel.text = text
                    binding.tvItemParticipantsListHostLabel.isVisible = true
                }
            }
//            binding.tvItemParticipantsListHostLabel.isVisible = itemUiModel.isHost
            binding.tvItemParticipantsListUniquename.text = "@${itemUiModel.uniqueName}"
            binding.tvItemParticipantsListAgeLocation.text = itemUiModel.ageLocation
            binding.ivItemParticipantsListAvatar.setConfig(
                UserpicUiModel(
                    userAvatarUrl = itemUiModel.avatarUrl,
                    storiesState = UserpicStoriesStateEnum.NO_STORIES
                )
            )

            binding.ivItemParticipantsListOptions.setThrottledClickListener {
                onAction.invoke(EventParticipantsListUiAction.ParticipantOptionsClicked(itemUiModel))
            }
            binding.root.setThrottledClickListener {
                onAction.invoke(EventParticipantsListUiAction.ParticipantClicked(itemUiModel.userId))
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EventParticipantsListItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: EventParticipantsListItemUiModel,
            newItem: EventParticipantsListItemUiModel
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: EventParticipantsListItemUiModel,
            newItem: EventParticipantsListItemUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
