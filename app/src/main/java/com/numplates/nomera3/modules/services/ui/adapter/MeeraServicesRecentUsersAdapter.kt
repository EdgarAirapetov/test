package com.numplates.nomera3.modules.services.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraSearchRecentUserItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesRecentUserViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraServicesRecentUsersAdapter(
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ListAdapter<RecentUserUiModel, MeeraServicesRecentUserViewHolder>(RecentUserDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraServicesRecentUserViewHolder {
        val binding = parent.inflateBinding(MeeraSearchRecentUserItemBinding::inflate)
        return MeeraServicesRecentUserViewHolder(binding, actionListener)
    }

    override fun onBindViewHolder(holder: MeeraServicesRecentUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class RecentUserDiffUtil : DiffUtil.ItemCallback<RecentUserUiModel>() {
        override fun areItemsTheSame(oldItem: RecentUserUiModel, newItem: RecentUserUiModel): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: RecentUserUiModel, newItem: RecentUserUiModel): Boolean {
            return oldItem == newItem
        }
    }

}
