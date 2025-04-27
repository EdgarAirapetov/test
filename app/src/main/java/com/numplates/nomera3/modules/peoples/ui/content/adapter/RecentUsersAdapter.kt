package com.numplates.nomera3.modules.peoples.ui.content.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.SearchRecentUserItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.holder.RecentUserItemViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class RecentUsersAdapter(
    private val actionListener: (FriendsContentActions) -> Unit
) : ListAdapter<RecentUserUiModel, RecentUserItemViewHolder>(RecentUserDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentUserItemViewHolder {
        val binding = parent.inflateBinding(SearchRecentUserItemBinding::inflate)
        return RecentUserItemViewHolder(binding, actionListener)
    }

    override fun onBindViewHolder(holder: RecentUserItemViewHolder, position: Int) {
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
