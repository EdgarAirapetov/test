package com.numplates.nomera3.modules.services.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraSearchRecentUserItemBinding
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.ServicesCommunityUiModel
import com.numplates.nomera3.modules.services.ui.viewholder.MeeraServicesCommunityViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraServicesCommunitiesAdapter(
    private val actionListener: (MeeraServicesUiAction) -> Unit
) : ListAdapter<ServicesCommunityUiModel, MeeraServicesCommunityViewHolder>(ServicesCommunityDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraServicesCommunityViewHolder {
        val binding = parent.inflateBinding(MeeraSearchRecentUserItemBinding::inflate)
        return MeeraServicesCommunityViewHolder(binding, actionListener)
    }

    override fun onBindViewHolder(holder: MeeraServicesCommunityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ServicesCommunityDiffUtil : DiffUtil.ItemCallback<ServicesCommunityUiModel>() {

        override fun areItemsTheSame(oldItem: ServicesCommunityUiModel, newItem: ServicesCommunityUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ServicesCommunityUiModel, newItem: ServicesCommunityUiModel): Boolean {
            return oldItem == newItem
        }
    }

}
