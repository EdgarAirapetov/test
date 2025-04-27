package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraSearchRecentUserItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.holder.MeeraRecentUserItemViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraRecentUsersAdapter(
    private val actionListener: (FriendsContentActions) -> Unit
) : ListAdapter<RecentUserUiModel, MeeraRecentUserItemViewHolder>(RecentUserDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraRecentUserItemViewHolder {
        val binding = parent.inflateBinding(MeeraSearchRecentUserItemBinding::inflate)
        return MeeraRecentUserItemViewHolder(binding, actionListener)
    }

    override fun onBindViewHolder(holder: MeeraRecentUserItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class RecentUserDiffUtil : DiffUtil.ItemCallback<RecentUserUiModel>() {

        override fun areItemsTheSame(oldItem: RecentUserUiModel, newItem: RecentUserUiModel): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: RecentUserUiModel, newItem: RecentUserUiModel): Boolean {
            return oldItem == newItem
        }
    }

}
